package de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.ITopologyChangedEvent;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

public class TopologyPatternMatcher_Impl implements TopologyPatternMatcher
{

   private static final long serialVersionUID = -3547232394940554943L;

   private TopologyPattern pattern;

   public TopologyPatternMatcher_Impl()
   {
      this.setPattern(null);
   }

   public TopologyPatternMatcher_Impl(final TopologyPattern pattern)
   {
      this.setPattern(pattern);
   }

   @Override
   public void setPattern(final TopologyPattern pattern)
   {
      this.pattern = pattern;
   }

   @Override
   public TopologyPattern getPattern()
   {
      return this.pattern;
   }

   @Override
   public Iterable<TopologyPatternMatch> match(final Graph graph)
   {
      final List<Iterable<TopologyPatternMatch>> iterables = new ArrayList<>();
      for (final INode node : graph.getNodes())
      {
         iterables.add(this.match(node.getId(), graph));
      }
      return Iterables.concat(iterables);
   }

   @Override
   public Iterable<TopologyPatternMatch> match(final INodeID localNode, final Graph graph)
   {
      return this.match(localNode, graph, new VariableAssignment());
   }

   @Override
   public int countMatches(final INodeID localNode, final Graph graph)
   {
      return Iterables.size(this.match(localNode, graph));
   }

   @Override
   public boolean hasMatch(final INodeID localNode, final Graph graph, final VariableAssignment variableAssignment)
   {
      return this.match(localNode, graph, variableAssignment).iterator().hasNext();
   }

   @Override
   public boolean hasMatch(final INodeID localNode, final Graph graph)
   {
      return this.hasMatch(localNode, graph, new VariableAssignment());
   }

   @Override
   public Iterable<TopologyPatternMatch> match(final INodeID localNode, final Graph graph, final VariableAssignment inputVariableAssignment)
   {

      final INodeID origin = pattern.getOrigin();

      final Map<INodeID, Collection<INodeID>> bindingCandidates = calculateBindingCandidates(localNode, graph, inputVariableAssignment);

      if (hasNodeWithoutBindingCandidate(bindingCandidates))
      {
         return new ArrayList<>();
      }

      /*
       * Match node variables
       */
      final Deque<INodeID> unboundVariables = new LinkedList<>(Lists.newArrayList(this.pattern.getVariables()));
      final Deque<INodeID> variablesWithCandidates = new LinkedList<>();
      final Map<INodeID, Iterator<INodeID>> candidatesMap = new HashMap<>();

      final VariableAssignment variableAssignment = new VariableAssignment();

      unboundVariables.remove(origin);
      variablesWithCandidates.push(origin);
      candidatesMap.put(origin, bindingCandidates.get(origin).iterator());

      final List<TopologyPatternMatch> matches = new ArrayList<>();

      while (!variablesWithCandidates.isEmpty())
      {
         final INodeID variable = variablesWithCandidates.peek();
         final Iterator<INodeID> candidateIterator = candidatesMap.get(variable);
         if (!candidateIterator.hasNext())
         {
            // All candidates processed -> go one level up
            variablesWithCandidates.pop();
            candidatesMap.put(variable, null);
            unboundVariables.push(variable);
            variableAssignment.unbindNodeVariable(variable);
         } else
         {
            INodeID candidate = candidateIterator.next();
            while (variableAssignment.isBindingForSomeVariable(graph.getNode(candidate)) && candidateIterator.hasNext())
            {
               candidate = candidateIterator.next();
            }
            if (!variableAssignment.isBindingForSomeVariable(graph.getNode(candidate)))
            {
               variableAssignment.bindNodeVariable(variable, graph.getNode(candidate));
               if (!unboundVariables.isEmpty())
               {
                  final INodeID nextVariable = unboundVariables.pop();
                  candidatesMap.put(nextVariable, bindingCandidates.get(nextVariable).iterator());
                  variablesWithCandidates.push(nextVariable);
               } else
               {
                  if (validateThatAllNodeVariablesAreBound(variableAssignment))
                  {
                     for (final VariableAssignment variableAssignmentsWithLinks : complementWithLinkAssignments(variableAssignment, localNode, graph))
                     {
                        matches.add(TopologyPatternMatch.create(this.pattern, variableAssignmentsWithLinks));
                     }
                  }
               }
            }
         }

      }

      return matches;
   }

   private boolean hasNodeWithoutBindingCandidate(final Map<INodeID, Collection<INodeID>> bindingCandidates)
   {
      for (final INodeID candidate : bindingCandidates.keySet())
      {
         if (bindingCandidates.get(candidate).isEmpty())
         {
            return true;
         }
      }
      return false;
   }

   /*
    * Determines binding candidates for each node variable: Starting at the
    * local node, the given graph is traversed using BFS. The depth of a link
    * variable and its binding candidate have to be identical.
    */
   private Map<INodeID, Collection<INodeID>> calculateBindingCandidates(final INodeID localNode, final Graph graph,
         final VariableAssignment inputVariableAssignment)
   {
      final Map<INodeID, Integer> variableDepths = GraphUtil.calculateNodeDepths(this.pattern.getOrigin(), this.pattern.getGraph());

      final Map<INodeID, Collection<INodeID>> bindingCandidates = new HashMap<>();
      for (final INodeID variable : variableDepths.keySet())
      {
         final Collection<INodeID> candidates;

         // If the variable is already bound in the input variable
         // assignment, use this assignment as sole candidate.
         if (inputVariableAssignment.isBound(variable))
         {
            candidates = Arrays.asList(inputVariableAssignment.getNodeVariableBinding(variable));
         } else
         {
            if (variableDepths.get(variable) == 0)
            {
               candidates = new HashSet<>(Arrays.asList(localNode));
            } else
            {
               candidates = new HashSet<>(Lists.newArrayList(graph.getNodeIds()));
            }
            // final Integer depth = variableDepths.get(variable);
            // final List<Node> graphNodes = hostGraphBFSLevels.get(depth);
            // if (graphNodes != null) {
            // candidates = graphNodes;
            // } else {
            // candidates = createEmptyIterator();
            // }
         }
         bindingCandidates.put(variable, candidates);
      }
      return bindingCandidates;
   }

   /**
    * Returns whether the all constraints of the pattern are satisfied for the
    * given variable assignment and the passed (variable,candidate) pair.
    */
   private Collection<VariableAssignment> complementWithLinkAssignments(final VariableAssignment variableAssignment, final INodeID localNode, final Graph graph)
   {
      final Collection<VariableAssignment> variableAssignmentsWithLinks = complementVariableAssignmentWithEdgeVariableBindings(variableAssignment, graph);

      return variableAssignmentsWithLinks.stream()//
            .filter(variableAssignmentWithLinks -> validateThatAllLinkConstraintsAreFulfilled(variableAssignmentWithLinks, graph)
                  && !hasMatchesForNACs(variableAssignmentWithLinks, localNode, graph)) //
            .collect(Collectors.toList());
   }

   private Collection<VariableAssignment> complementVariableAssignmentWithEdgeVariableBindings(VariableAssignment nodeBindingVariableAssignment, Graph graph)
   {
      final List<VariableAssignment> resultingVariableAssignments = new ArrayList<>();
      resultingVariableAssignments.add(new VariableAssignment(nodeBindingVariableAssignment));
      for (final IEdge linkVariable : this.pattern.getLinkVariables())
      {
         final INodeID sourceVariable = linkVariable.fromId();
         final INodeID targetVariable = linkVariable.toId();
         final INodeID bindingNodeIdOfSourceVariable = nodeBindingVariableAssignment.getNodeVariableBinding(sourceVariable);
         final INodeID bindingNodeIdOfTargetVariable = nodeBindingVariableAssignment.getNodeVariableBinding(targetVariable);
         final Collection<IEdge> connectingEdges = graph.getEdges(bindingNodeIdOfSourceVariable, bindingNodeIdOfTargetVariable);
         final EdgeID linkVariableId = linkVariable.getId();
         if (connectingEdges.isEmpty())
         {
            return new ArrayList<VariableAssignment>();
         } else
         {
            final List<VariableAssignment> newVariableAssignments = new ArrayList<>(connectingEdges.size() - 1);
            for (final VariableAssignment resultingVariableAssignment : resultingVariableAssignments)
            {
               final Iterator<IEdge> connectingEdgesIterator = connectingEdges.iterator();
               resultingVariableAssignment.bindLinkVariable(linkVariableId, connectingEdgesIterator.next());
               for (int i = 1; i < connectingEdges.size(); ++i) {
                  final VariableAssignment clonedVariableAssignment = new VariableAssignment(resultingVariableAssignment);
                  newVariableAssignments.add(clonedVariableAssignment);
                  clonedVariableAssignment.bindLinkVariable(linkVariableId, connectingEdgesIterator.next());
               }
            }
            resultingVariableAssignments.addAll(newVariableAssignments);
         }
      }
      return resultingVariableAssignments;
   }

   /**
    * Returns whether all link variable constraints can be fulfilled by the
    * given variable assignment.
    */
   private boolean validateThatAllLinkConstraintsAreFulfilled(final VariableAssignment variableAssignment, final Graph graph)
   {
      for (final GraphElementConstraint constraint : this.pattern.getConstraints())
      {
         final List<? extends IElement> bindingElementList = getBindingCandidatesForAffectedElements(constraint, variableAssignment, graph);
         if (!constraint.isFulfilled(bindingElementList))
         {
            return false;
         }
      }
      return true;
   }

   /**
    * Extracts the retrieves for each variable of the given constraint the
    * appropriate graph element in the variable binding
    */
   private List<? extends IElement> getBindingCandidatesForAffectedElements(final GraphElementConstraint constraint,
         final VariableAssignment variableAssignment, final Graph graph)
   {
      final List<IElement> result = new ArrayList<>();
      for (final UniqueID variable : constraint.getVariables())
      {
         final UniqueID bindingElementOfVariable = variableAssignment.getVariableBinding(variable);
         if (variable instanceof EdgeID)
            result.add(graph.getEdge((EdgeID) bindingElementOfVariable));
         else if (variable instanceof INodeID)
            result.add(graph.getNode((INodeID) bindingElementOfVariable));
      }
      return result;
   }

   private boolean validateThatAllNodeVariablesAreBound(final VariableAssignment variableAssignment)
   {
      for (final INodeID variable : this.pattern.getVariables())
      {
         if (variableAssignment.isUnbound(variable))
         {
            return false;
         }
      }
      return true;
   }

   /**
    * Returns true if any of the NAC patterns matches
    */
   private boolean hasMatchesForNACs(final VariableAssignment variableAssignment, final INodeID localNode, final Graph graph)
   {
      for (final TopologyPattern nacPattern : this.pattern.getNegativeApplicationConstraints())
      {
         final TopologyPatternMatcher nacPatternMatcher = new TopologyPatternMatcher_Impl(nacPattern);
         if (nacPatternMatcher.hasMatch(localNode, graph, variableAssignment))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public Iterable<TopologyPatternMatch> handleEvent(final ITopologyChangedEvent topologyChangedEvent)
   {
      final TopologyID topologyIdentifier = topologyChangedEvent.getTopologyIdentifier();
      final INode localNode = topologyChangedEvent.getTopologyProvider().getNode(topologyIdentifier);
      final Graph graph = topologyChangedEvent.getTopologyProvider().getLocalView(topologyIdentifier);
      return this.match(localNode.getId(), graph);
   }

}
