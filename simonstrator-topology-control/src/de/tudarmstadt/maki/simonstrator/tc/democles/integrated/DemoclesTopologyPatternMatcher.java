package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gervarro.democles.common.runtime.ListOperationBuilder;
import org.gervarro.democles.common.runtime.SearchPlanOperation;
import org.gervarro.democles.common.runtime.SpecificationExtendedVariableRuntime;
import org.gervarro.democles.interpreter.InterpreterPatternMatcherModule;
import org.gervarro.democles.interpreter.InterpreterSearchPlanAlgorithm;
import org.gervarro.democles.interpreter.OperationCachingPattern;
import org.gervarro.democles.operation.RelationalOperationBuilder;
import org.gervarro.democles.plan.WeightedOperation;
import org.gervarro.democles.plan.combiner.InterpreterCombiner;
import org.gervarro.democles.plan.common.CombinedSearchPlanOperationBuilder;
import org.gervarro.democles.plan.common.DefaultAlgorithm;
import org.gervarro.democles.plan.common.RelationalSearchPlanOperationBuilder;
import org.gervarro.democles.plan.common.RelationalWeightedOperationBuilder;
import org.gervarro.democles.plan.common.SearchPlanOperationBuilder;
import org.gervarro.democles.runtime.BatchInterpreterSearchPlanOperation;
import org.gervarro.democles.runtime.DepthFirstTraversalStrategy;
import org.gervarro.democles.runtime.GenericOperationBuilder;
import org.gervarro.democles.runtime.InterpretableAdornedOperation;
import org.gervarro.democles.runtime.InterpretedDataFrame;
import org.gervarro.democles.specification.Pattern;
import org.gervarro.democles.specification.impl.DefaultPattern;
import org.gervarro.democles.specification.impl.DefaultPatternBody;
import org.gervarro.democles.specification.impl.DefaultPatternBuilder;
import org.gervarro.democles.specification.impl.DefaultPatternFactory;

import com.google.common.collect.Iterables;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.ITopologyChangedEvent;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

/**
 * 
 * @author Lukas Neumann - Initial implementation
 * @author Roland Kluge - Refactoring and maintenance
 */
public class DemoclesTopologyPatternMatcher implements TopologyPatternMatcher
{

   private static final long serialVersionUID = -6797749312891613414L;

   public static final String PROPERTY_KEY_USE_LIGHTNING = "democles.useLightning";

   public static final String PROPERTY_KEY_PATTERN = "topologyPattern";

   private TopologyPattern topologyPattern;

   private SimonstratorPatternToDemoclesIntegratedPatternMapping patternMapping;

   private DefaultPatternBuilder<DefaultPattern, DefaultPatternBody> patternBuilder;

   private InterpreterPatternMatcherModule patternMatcherModule;

   private OperationCachingPattern patternRuntime;

   private boolean shallUseLightning;

   public DemoclesTopologyPatternMatcher()
   {
      this(new HashMap<String, Object>());
   }

   public DemoclesTopologyPatternMatcher(final TopologyPattern pattern)
   {
      this(createConfigurationFromPattern(pattern));
   }

   private static Map<String, Object> createConfigurationFromPattern(TopologyPattern pattern)
   {
      final Map<String, Object> configuration = new HashMap<>();
      if (pattern != null)
         configuration.put(PROPERTY_KEY_PATTERN, pattern);
      return configuration;
   }

   public DemoclesTopologyPatternMatcher(final Map<String, Object> configuration)
   {
      if (configuration.containsKey(PROPERTY_KEY_USE_LIGHTNING))
      {
         this.shallUseLightning = Boolean.parseBoolean(configuration.get(PROPERTY_KEY_USE_LIGHTNING).toString());
      } else
      {
         this.shallUseLightning = false;
      }
      
      if (configuration.containsKey(PROPERTY_KEY_PATTERN))
      {
         this.setPattern(TopologyPattern.class.cast(configuration.get(PROPERTY_KEY_PATTERN)));
      }
   }

   /**
    * Sets the graph pattern of this matcher
    */
   public void setPattern(TopologyPattern topologyPattern)
   {
      this.topologyPattern = topologyPattern;
      this.patternMapping = null;
   }

   @Override
   public TopologyPattern getPattern()
   {
      return this.topologyPattern;
   }

   /**
    * Returns all matches of the stored pattern in the given graph that map the given local node to the origin of the pattern.
    *
    * Preconditions: pattern has to be set.
    *
    * @param localNode the ID of the node in the graph that will be mapped to the origin of the stored pattern in all matches
    * @param graph the graph in which the matches shall be identified
    * @return an iterator over the matches
    */
   public Iterable<TopologyPatternMatch> match(INodeID localNode, Graph graph)
   {
      initialize();
      final InterpretedDataFrame frame = patternRuntime.createDataFrame();
      frame.setValue(0, graph);
      frame.setValue(patternMapping.getOriginMapping(), graph.getNode(localNode));

      Iterable<InterpretedDataFrame> democlesMatches = patternRuntime.matchAll(frame);
      Iterable<TopologyPatternMatch> topologyMatches = DemoclesMatchesToSimonstratorMatchesTransformation.getInstance().transform(democlesMatches,
            patternMapping);
      return topologyMatches;
   }

   /**
    * Returns all matches of the stored pattern in the given graph.
    *
    * Preconditions: pattern has to be set.
    *
    * @param graph the graph in which the matches shall be identified
    * @return an iterator over the matches
    */
   public Iterable<TopologyPatternMatch> match(Graph graph)
   {
      initialize();
      final InterpretedDataFrame frame = patternRuntime.createDataFrame();
      frame.setValue(0, graph);

      Iterable<InterpretedDataFrame> democlesMatches = patternRuntime.matchAll(frame);
      Iterable<TopologyPatternMatch> topologyMatches = DemoclesMatchesToSimonstratorMatchesTransformation.getInstance().transform(democlesMatches,
            patternMapping);
      return topologyMatches;
   }

   @Override
   public Iterable<TopologyPatternMatch> handleEvent(ITopologyChangedEvent topologyChangedEvent)
   {
      throw new UnsupportedOperationException(); // Incremental operation mode in supported, yet.
   }

   /**
    * Finds matches for the stored pattern in the given graph, using the given node as 'origin'.
    * The given variable assignment may contain bindings for node variables.
    */
   public Iterable<TopologyPatternMatch> match(INodeID localNode, Graph graph, VariableAssignment inputVariableAssignment)
   {
      initialize();
      final InterpretedDataFrame frame = patternRuntime.createDataFrame();
      frame.setValue(0, graph);
      frame.setValue(patternMapping.getOriginMapping(), graph.getNode(localNode));

      for (Entry<INodeID, INode> entry : inputVariableAssignment.getNodeBindingEntrySet())
      {
    	  if(patternMapping.hasNodeMapping(entry.getKey()))    	  
    		  frame.setValue(patternMapping.getNodeMapping(entry.getKey()), entry.getValue());
      }
      for (Entry<EdgeID, IEdge> entry : inputVariableAssignment.getLinkBindingEntrySet())
      {
    	  if(patternMapping.hasEdgeMapping(entry.getKey()))
    		  frame.setValue(patternMapping.getEdgeMapping(entry.getKey()), entry.getValue());
      }
      Iterable<InterpretedDataFrame> democlesMatches = patternRuntime.matchAll(frame);
      Iterable<TopologyPatternMatch> topologyMatches = DemoclesMatchesToSimonstratorMatchesTransformation.getInstance().transform(democlesMatches,
            patternMapping);
      return topologyMatches;
   }

   @Override
   public boolean hasMatch(INodeID localNode, Graph graph, VariableAssignment variableAssignment)
   {
      return match(localNode, graph, variableAssignment).iterator().hasNext();
   }

   @Override
   public boolean hasMatch(INodeID localNode, Graph graph)
   {
      return this.match(localNode, graph).iterator().hasNext();
   }

   @Override
   public int countMatches(INodeID localNode, Graph graph)
   {
      return Iterables.size(this.match(localNode, graph));
   }

   private void initialize()
   {
      if (patternBuilder == null)
      {
         initializePatternBuilder();
      }
      if (patternMatcherModule == null)
      {
         initializePatternMatcherModule();
      }
      if (patternMapping == null)
      {
         patternMapping = SimonstratorPatternToDemoclesIntegratedPatternTransformer.getInstance().transform(topologyPattern, "SimonstratorPattern", null);

         if (patternMapping.getNacPatterns() == null)
         {
            DefaultPattern internalPatternSpecification = patternBuilder.build(patternMapping.getDemoclesPattern());
            patternRuntime = patternMatcherModule.build(internalPatternSpecification);
         } else
         {
            final List<Pattern> patterns = new ArrayList<>();
            patterns.add(patternMapping.getDemoclesPattern());
            patterns.addAll(patternMapping.getNacPatterns());
            final List<DefaultPattern> internalPatternSpecifications = patternBuilder.build(patterns);
            final DefaultPattern[] internalPatternSpecificationsArray = internalPatternSpecifications
                  .toArray(new DefaultPattern[internalPatternSpecifications.size()]);
            patternRuntime = patternMatcherModule.build(internalPatternSpecificationsArray).get(0);
         }
      }
   }

   private void initializePatternBuilder()
   {
      patternBuilder = new DefaultPatternBuilder<DefaultPattern, DefaultPatternBody>(new DefaultPatternFactory());
   }

   private void initializePatternMatcherModule()
   {

      final LinkedList<SearchPlanOperationBuilder<WeightedOperation<SearchPlanOperation<BatchInterpreterSearchPlanOperation>, Integer>, BatchInterpreterSearchPlanOperation>> builders = new LinkedList<>();
      builders
            .add(new CombinedSearchPlanOperationBuilder<WeightedOperation<SearchPlanOperation<BatchInterpreterSearchPlanOperation>, Integer>, SearchPlanOperation<BatchInterpreterSearchPlanOperation>, BatchInterpreterSearchPlanOperation>(
                  new SimonstratorSearchPlanOperationBuilder<BatchInterpreterSearchPlanOperation>(),
                  new SimonstratorWeightedOperationBuilder<SearchPlanOperation<BatchInterpreterSearchPlanOperation>, BatchInterpreterSearchPlanOperation>()));
      builders
            .add(new CombinedSearchPlanOperationBuilder<WeightedOperation<SearchPlanOperation<BatchInterpreterSearchPlanOperation>, Integer>, SearchPlanOperation<BatchInterpreterSearchPlanOperation>, BatchInterpreterSearchPlanOperation>(
                  new RelationalSearchPlanOperationBuilder<BatchInterpreterSearchPlanOperation>(),
                  new RelationalWeightedOperationBuilder<SearchPlanOperation<BatchInterpreterSearchPlanOperation>, BatchInterpreterSearchPlanOperation>()));

      final DefaultAlgorithm<InterpreterCombiner, SearchPlanOperation<BatchInterpreterSearchPlanOperation>, BatchInterpreterSearchPlanOperation> searchPlanAlgorithm = // 
            new DefaultAlgorithm<InterpreterCombiner, SearchPlanOperation<BatchInterpreterSearchPlanOperation>, BatchInterpreterSearchPlanOperation>(builders);

      final GenericOperationBuilder<SpecificationExtendedVariableRuntime> simonstratorOperationBuilder = //
            new GenericOperationBuilder<SpecificationExtendedVariableRuntime>(new SimonstratorOperationBuilder(),
                  DefaultSimonstratorAdornmentStrategy.INSTANCE);

      if (this.shallUseLightning)
      {
         //         final CategoryBasedQueueFactory<Task> categoryBasedQueueFactory = new CategoryBasedQueueFactory<>(DepthFirstTraversalStrategy.INSTANCE);
         //         patternMatcherModule = new InterpreterPatternMatcherModule(categoryBasedQueueFactory, DepthFirstTraversalStrategy.INSTANCE);
      } else
      {
         patternMatcherModule = new InterpreterPatternMatcherModule(DepthFirstTraversalStrategy.INSTANCE);
      }

      patternMatcherModule.setSearchPlanAlgorithm(new InterpreterSearchPlanAlgorithm(searchPlanAlgorithm));
      patternMatcherModule.addOperationBuilder(simonstratorOperationBuilder);
      patternMatcherModule.addOperationBuilder(new ListOperationBuilder<InterpretableAdornedOperation, SpecificationExtendedVariableRuntime>(
            new RelationalOperationBuilder<SpecificationExtendedVariableRuntime>()));
   }

}
