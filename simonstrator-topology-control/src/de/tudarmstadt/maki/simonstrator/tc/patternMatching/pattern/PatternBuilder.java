package de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.TopologyPatternMatchingException;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ArithmeticOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.LinkWeightConstraintWithArithmeticOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.LinkWeightConstraintWithScalarValue;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.LinkWeightConstraintWithTwoLinks;

/**
 * This class produces topology patterns from a textual representation of the
 * pattern.
 *
 * Its interface is fluent, i.e., the pattern is built by a sequence of calls:
 *
 * <pre>
 * PatternBuilder.create().setLocalNode(&quot;n1&quot;).addDirectedEdge(&quot;e12&quot;, &quot;n1&quot;, &quot;n2&quot;, 1.1).addDirectedEdge(&quot;e23&quot;, &quot;n2&quot;, &quot;n3&quot;,
 * 		1.5);
 * </pre>
 *
 */
public class PatternBuilder
{

   public enum PatternBuilderFlags {
      NO_LOCAL_NODE_CHECK;
   }

   private INodeID localNode;

   private final Map<EdgeID, IEdge> idToLink;

   private final Set<INodeID> nodeIds;

   private final Collection<GraphElementConstraint> constraints;

   private final Collection<TopologyPattern> nacs;

   /** Contains additional meta-data for building the pattern */
   private final Collection<PatternBuilderFlags> flags;

   private boolean isDone;

   public static PatternBuilder create()
   {
      return new PatternBuilder();
   }

   private PatternBuilder()
   {
      this.isDone = false;
      this.localNode = null;
      this.idToLink = new HashMap<>();
      this.nodeIds = new HashSet<>();
      this.constraints = new ArrayList<>();
      this.nacs = new ArrayList<>();
      this.flags = new HashSet<>();
   }

   public PatternBuilder setLocalNode(final String id)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      if (id == null)
      {
         throw new NullPointerException();
      }

      if (this.localNode != null && !this.localNode.equals(INodeID.get(id)))
      {
         throw new TopologyPatternMatchingException("Local node may not be re-assigned!");
      }

      this.localNode = this.getOrCreateNode(id);

      return this;
   }

   public PatternBuilder addDirectedEdge(final String sourceId, final String targetId)
   {
      return this.addDirectedEdge(sourceId, sourceId + "-" + targetId, targetId);
   }

   public PatternBuilder addDirectedEdge(final String sourceId, final String id, final String targetId)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      final INodeID sourceNode = getOrCreateNode(sourceId);
      final INodeID targetNode = getOrCreateNode(targetId);
      final EdgeID edgeId = EdgeID.get(id);
      if (idToLink.containsKey(edgeId))
      {
         throw new TopologyPatternMatchingException(
               "You are trying to add a redundant link variable with conflicting source and/or target node variable ID: Existing: " + idToLink.get(edgeId)
                     + " - New: " + new DirectedEdge(sourceNode, targetNode));
      } else
      {
         idToLink.put(edgeId, new DirectedEdge(sourceNode, targetNode, id));
      }
      return this;

   }

   public PatternBuilder addUndirectedEdge(final String sourceId, final String targetId)
   {
      return this.addUndirectedEdge(sourceId, sourceId + "-" + targetId, targetId);
   }

   public PatternBuilder addUndirectedEdge(final String sourceId, final String id, final String targetId)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.addDirectedEdge(sourceId, id, targetId);
      this.addDirectedEdge(targetId, id + "_R", sourceId);

      return this;
   }

   /**
    * Adds a {@link LinkWeightConstraintWithTwoLinks}
    */
   public PatternBuilder addLinkWeightConstraint(final String lhsEdge, final ComparisonOperator operator, final String rhsEdge)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.constraints.add(new LinkWeightConstraintWithTwoLinks(EdgeID.get(lhsEdge), EdgeID.get(rhsEdge), operator));
      return this;
   }

   public PatternBuilder addPropertyComparisonConstraint(final String lhsLinkVariable, final ComparisonOperator greaterOrEqual,
         final GraphElementProperty<Double> property, final String rhsLinkVariable)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.constraints.add(new GraphElementConstraint(Arrays.asList(EdgeID.get(lhsLinkVariable), EdgeID.get(rhsLinkVariable))) {

         private static final long serialVersionUID = 5247878268476050550L;

         @Override
         protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates)
         {
            final Iterator<? extends IElement> iter = bindingCandidates.iterator();
            final IEdge link1 = (IEdge) iter.next();
            final IEdge link2 = (IEdge) iter.next();
            greaterOrEqual.evaluate(link1.getProperty(property), link2.getProperty(property));
            return false;
         }
      });

      return this;
   }

   public PatternBuilder addBinaryConstraint(final GraphElementConstraint constraint)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.constraints.add(constraint);

      return this;
   }

   /**
    * Adds a {@link LinkWeightConstraintWithScalarValue}
    */
   public PatternBuilder addLinkWeightConstraint(final String lhsEdge, final double value, final ComparisonOperator operator)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.constraints.add(new LinkWeightConstraintWithScalarValue(EdgeID.get(lhsEdge), value, operator));

      return this;
   }

   /**
    * Adds a {@link LinkWeightConstraintWithArithmeticOperator}
    */
   public PatternBuilder addLinkWeightConstraint(final String lhsEdge, final ComparisonOperator comparisonOperator, final String rhsEdge,
         final ArithmeticOperator arithmeticOperator, final double value)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.constraints
            .add(new LinkWeightConstraintWithArithmeticOperator(EdgeID.get(lhsEdge), EdgeID.get(rhsEdge), value, comparisonOperator, arithmeticOperator));

      return this;
   }

   /**
    * Adds a negative application condition to the pattern.
    */
   public PatternBuilder addNAC(final TopologyPattern negativeApplicationCondition)
   {
      this.checkThatPatternConstructionIsNotDoneYet();

      this.nacs.add(negativeApplicationCondition);

      return this;
   }

   /**
    * Constructs the pattern.
    * 
    * <strong>Note:</strong> After invoking {@link #done()}, none of the pattern-modifying methods (e.g., {@link #addDirectedEdge(String, String, String)}) may be invoked.
    *
    * @throws TopologyPatternMatchingException
    *             if the pattern cannot be constructed
    */
   public TopologyPattern done()
   {
      this.checkConsistency();

      this.isDone = true;
      return new TopologyPattern(localNode, buildPatternGraph(), constraints, buildNACPatterns());
   }

   public TopologyPattern doneWithoutLocalNode()
   {
      this.flags.addAll(Arrays.asList(PatternBuilderFlags.NO_LOCAL_NODE_CHECK));
      return this.done();
   }

   private INodeID getOrCreateNode(final String id)
   {
      final INodeID iNodeID = INodeID.get(id);
      this.nodeIds.add(iNodeID);
      return iNodeID;
   }

   /**
    * This method ensures that {@link #done()} has not been called, yet.
    */
   private void checkThatPatternConstructionIsNotDoneYet() throws IllegalStateException
   {
      if (this.isDone)
         throw new IllegalStateException("The pattern construction has already been completed. Please create a new PatternBuilder to create another pattern");
   }

   private Graph buildPatternGraph()
   {
      return GraphUtil.createGraph(nodeIds, new HashSet<>(idToLink.values()));
   }

   private Collection<TopologyPattern> buildNACPatterns()
   {
      final ArrayList<TopologyPattern> nacPatterns = new ArrayList<>();
      for (final TopologyPattern nac : this.nacs)
      {
         nacPatterns.add(createNACPattern(nac));
      }
      return nacPatterns;
   }

   /**
    * Glues the NAC to the 'main' pattern, that is, creates a pattern that
    * contains the union of nodes, links, and constraints.
    */
   private TopologyPattern createNACPattern(final TopologyPattern nac)
   {
      final Set<INodeID> nodeVariables = new HashSet<>(nodeIds);
      nodeVariables.addAll(Lists.newLinkedList(nac.getNodeVariables()));
      final Set<IEdge> linkVariables = new HashSet<>(this.idToLink.values());
      linkVariables.addAll(Lists.newLinkedList(nac.getLinkVariables()));
      final Set<GraphElementConstraint> constraints = new HashSet<>(this.constraints);
      constraints.addAll(nac.getConstraints());
      final TopologyPattern nacPattern = new TopologyPattern(localNode, GraphUtil.createGraph(nodeVariables, linkVariables), constraints);
      return nacPattern;
   }

   private void checkConsistency()
   {
      final Graph graph = buildPatternGraph();
      if (needLocalNodeCheck() && this.localNode == null)
      {
         throw new TopologyPatternMatchingException("Pattern must contain a 'local node' that stands for the current node");
      }

      if (graph.getNodes().isEmpty())
      {
         throw new TopologyPatternMatchingException("Graph must not be empty.");
      }

      if (this.needLocalNodeCheck())
      {
         final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(this.localNode, graph, false);
         for (final Integer depth : nodeDepths.values())
         {
            if (depth == GraphUtil.INFINITE_DISTANCE)
            {
               throw new TopologyPatternMatchingException("Pattern must be connected.");
            }
         }
      }

      for (final GraphElementConstraint constraint : this.constraints)
      {
         for (final UniqueID variable : constraint.getVariables())
         {
            if (!idToLink.keySet().contains(variable) && !this.nodeIds.contains(variable))
            {
               throw new TopologyPatternMatchingException("Variable " + variable + " of constraint " + constraint + " is not part of the pattern");
            }
         }
      }
   }

   /**
    * Returns whether the generated pattern should contain a local node. For
    * instance, patterns intended to be NAC patterns need no local node.
    */
   private boolean needLocalNodeCheck()
   {
      return !this.flags.contains(PatternBuilderFlags.NO_LOCAL_NODE_CHECK);
   }

}
