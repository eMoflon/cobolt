package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gervarro.democles.constraint.CoreConstraintModule;
import org.gervarro.democles.constraint.CoreConstraintType;
import org.gervarro.democles.constraint.PatternInvocationConstraintType;
import org.gervarro.democles.specification.impl.Constant;
import org.gervarro.democles.specification.impl.Constraint;
import org.gervarro.democles.specification.impl.ConstraintVariable;
import org.gervarro.democles.specification.impl.DefaultPattern;
import org.gervarro.democles.specification.impl.DefaultPatternBody;
import org.gervarro.democles.specification.impl.DefaultPatternFactory;
import org.gervarro.democles.specification.impl.Variable;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.EdgeVariableType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.GraphVariableType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.NodeVariableType;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern.SimonstratorConstraintTypeModule;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

/**
 * This class constructs a {@link SimonstratorPatternToDemoclesIntegratedPatternMapping}
 * @author lneumann
 *
 */
public class SimonstratorPatternToDemoclesIntegratedPatternTransformer
{

   private DefaultPattern democlesPattern;

   private DefaultPatternFactory patternFactory;

   private TopologyPattern topologyPattern;

   private SimonstratorPatternToDemoclesIntegratedPatternMapping mapping;

   private int counter;

   private int constraintCounter;

   private Variable graphVariable;

   private List<Variable> nodeVariables;

   private List<Variable> edgeVariables;

   private Map<INodeID, Variable> nodes;

   private Map<EdgeID, Variable> edges;

   private SimonstratorConstraintTypeModule constraintTypeModule;

   private Variable[] symbolicParameters;

   private Constraint[] constraints;

   private Variable[] localVariables;

   private ArrayList<String> nacNames;

   public static SimonstratorPatternToDemoclesIntegratedPatternTransformer getInstance()
   {
      return new SimonstratorPatternToDemoclesIntegratedPatternTransformer();
   }

   /**
    * Transforms the given pattern into a Democles-compatible pattern specification.
    * @param topologyPattern
    * @param name pattern name
    * @return mapping
    */
   public SimonstratorPatternToDemoclesIntegratedPatternMapping transform(TopologyPattern topologyPattern, String name, ArrayList<String> nacNames)
   {
      this.topologyPattern = topologyPattern;
      this.nacNames = nacNames;
      this.mapping = new SimonstratorPatternToDemoclesIntegratedPatternMapping();
      this.patternFactory = new DefaultPatternFactory();
      this.constraintTypeModule = SimonstratorConstraintTypeModule.getInstance();
      
      createSymbolicParameters();
      
      this.democlesPattern = patternFactory.createPattern(name, symbolicParameters);
      
      setConstraints();
      
      final DefaultPatternBody body = patternFactory.createPatternBody(localVariables, constraints, new Constant[0]);
      this.patternFactory.setBodies(democlesPattern, new DefaultPatternBody[] { body });
      
      setNacPatterns();
      
      mapping.setDemoclesPattern(democlesPattern);
      
      return mapping;
   }

   private void setNacPatterns()
   {
      int i = 1;
      for (TopologyPattern tp : topologyPattern.getNegativeApplicationConstraints())
      {
         //calculate NAC-Parameters
         ArrayList<Variable> nacParameters = new ArrayList<Variable>();
         ArrayList<String> nacNames = new ArrayList<String>();
         Graph tpGraph = topologyPattern.getGraph();
         Graph nacGraph = tp.getGraph();
         nacParameters.add(graphVariable);
         for (INodeID nodeNacId : nacGraph.getNodeIds())
         {
            for (INodeID nodePatternId : tpGraph.getNodeIds())
            {
               if (nodePatternId.toString().equals(nodeNacId.toString()))
               {
                  nacParameters.add(nodes.get(nodePatternId));
                  nacNames.add(nodePatternId.toString());
               }
            }
         }
         for (EdgeID edgeNacId : nacGraph.getEdgeIds())
         {
            for (EdgeID edgePatternId : tpGraph.getEdgeIds())
            {
               if (edgePatternId.toString().equals(edgeNacId.toString()))
               {
                  nacParameters.add(edges.get(edgePatternId));
                  nacNames.add(edgePatternId.toString());
               }
            }
         }

         SimonstratorPatternToDemoclesIntegratedPatternMapping nacMapping = getInstance().transform(tp, "NacPattern" + i++, nacNames);
         mapping.setNacPatterns(nacMapping.getDemoclesPattern());

         ConstraintVariable[] constraintVariablesAsArray = nacParameters.toArray(new ConstraintVariable[nacParameters.size()]);

         constraints[constraintCounter++] = new Constraint(constraintVariablesAsArray,
               new PatternInvocationConstraintType(nacMapping.getDemoclesPattern(), false));
      }
   }

   private void createSymbolicParameters()
   {
      nodeVariables = new ArrayList<Variable>();
      edgeVariables = new ArrayList<Variable>();
      nodes = new HashMap<INodeID, Variable>();
      edges = new HashMap<EdgeID, Variable>();
      counter = 0;
      int symbolicParamtersCounter = 0;
      int localVariablesCounter = 0;
      Graph graph = topologyPattern.getGraph();
      if (graph != null)
      {
         if (nacNames == null)
         {
            symbolicParameters = new Variable[graph.getEdgeCount() + graph.getNodeCount() + 1];
            localVariables = new Variable[0];
         } else
         {
            symbolicParameters = new Variable[nacNames.size() + 1];
            localVariables = new Variable[graph.getEdgeCount() + graph.getNodeCount() + 1 - (nacNames.size() + 1)];
         }
         int nodeCount = graph.getNodeCount();
         int edgeCount = graph.getEdgeCount();
         constraints = new Constraint[(edgeCount * 2) + nodeCount + edgeCount + calculateCoreConstraintSize(nodeCount, edgeCount)];
         constraintCounter = 0;
         NodeVariableType nodeVariableType = NodeVariableType.getInstance();
         EdgeVariableType edgeVariableType = EdgeVariableType.getInstance();
         graphVariable = new Variable("Graph", GraphVariableType.getInstance());
         symbolicParameters[counter++] = graphVariable;
         symbolicParamtersCounter++;
         for (INodeID nodeId : graph.getNodeIds())
         {
            Variable n = new Variable(nodeId.valueAsString(), nodeVariableType);
            if (nacNames != null)
            {
               if (nacNames.contains(n.getName()))
               {
                  symbolicParameters[symbolicParamtersCounter++] = n;
               } else
               {
                  localVariables[localVariablesCounter++] = n;
               }
            } else
            {
               symbolicParameters[symbolicParamtersCounter++] = n;
            }
            if (topologyPattern.getOrigin().equals(nodeId))
            {
               mapping.setOriginMapping(counter);
            }
            mapping.setNodeMapping(counter++, nodeId);
            nodeVariables.add(n);
            nodes.put(nodeId, n);
            constraints[constraintCounter++] = new Constraint(new ConstraintVariable[] { graphVariable, n }, SimonstratorConstraintTypeModule.GRAPH_NODES);
         }
         for (EdgeID edgeId : graph.getEdgeIds())
         {
            Variable e = new Variable(edgeId.valueAsString(), edgeVariableType);
            if (nacNames != null)
            {
               if (nacNames.contains(e.getName()))
               {
                  symbolicParameters[symbolicParamtersCounter++] = e;
               } else
               {
                  localVariables[localVariablesCounter++] = e;
               }
            } else
            {
               symbolicParameters[symbolicParamtersCounter++] = e;
            }
            mapping.setEdgeMapping(counter++, edgeId);
            edgeVariables.add(e);
            edges.put(edgeId, e);
            IEdge edge = graph.getEdge(edgeId);
            Variable from = nodes.get(edge.fromId());
            Variable to = nodes.get(edge.toId());
            constraints[constraintCounter++] = new Constraint(new ConstraintVariable[] { graphVariable, e }, SimonstratorConstraintTypeModule.GRAPH_EDGES);
            constraints[constraintCounter++] = new Constraint(new ConstraintVariable[] { graphVariable, from, e },
                  SimonstratorConstraintTypeModule.OUTGOING_EDGE);
            constraints[constraintCounter++] = new Constraint(new ConstraintVariable[] { graphVariable, to, e },
                  SimonstratorConstraintTypeModule.INCOMING_EDGE);
         }
         uniqueSymbols();
      }
   }

   /**
    * Ensures injectivity for all symbols 
    */
   private void uniqueSymbols()
   {
      for (int i = 0; i < nodeVariables.size() - 1; i++)
      {
         for (int j = i + 1; j < nodeVariables.size(); j++)
         {
            createCoreConstraint(nodeVariables.get(i), nodeVariables.get(j), ComparisonOperator.NOT_EQUAL);
         }
      }
      for (int i = 0; i < edgeVariables.size() - 1; i++)
      {
         for (int j = i + 1; j < edgeVariables.size(); j++)
         {
            createCoreConstraint(edgeVariables.get(i), edgeVariables.get(j), ComparisonOperator.NOT_EQUAL);
         }
      }
   }

   private int calculateCoreConstraintSize(int nodes, int edges)
   {
      int i = nodes - 1;
      int nodeCombinations = 0;
      while (i > 0)
      {
         nodeCombinations = nodeCombinations + i--;
      }
      int j = edges - 1;
      int edgeCombinations = 0;
      while (j > 0)
      {
         edgeCombinations = edgeCombinations + j--;
      }

      int constraints = topologyPattern.getConstraints().size();
      if (topologyPattern.getNegativeApplicationConstraints() != null)
      {
         constraints += topologyPattern.getNegativeApplicationConstraints().size();
      }
      return (nodeCombinations + edgeCombinations + constraints);

   }

   private void createCoreConstraint(ConstraintVariable variable, ConstraintVariable variable2, ComparisonOperator o)
   {
      CoreConstraintType coreConstraint = null;
      if (o == ComparisonOperator.EQUAL)
      {
         coreConstraint = CoreConstraintModule.EQUAL;
      } else if (o == ComparisonOperator.NOT_EQUAL)
      {
         coreConstraint = CoreConstraintModule.UNEQUAL;
      } else if (o == ComparisonOperator.LESS)
      {
         coreConstraint = CoreConstraintModule.SMALLER;
      } else if (o == ComparisonOperator.LESS_OR_EQUAL)
      {
         coreConstraint = CoreConstraintModule.SMALLER_OR_EQUAL;
      } else if (o == ComparisonOperator.GREATER)
      {
         coreConstraint = CoreConstraintModule.LARGER;
      } else if (o == ComparisonOperator.GREATER_OR_EQUAL)
      {
         coreConstraint = CoreConstraintModule.LARGER_OR_EQUAL;
      }
      constraints[constraintCounter++] = new Constraint(new ConstraintVariable[] { variable, variable2 }, coreConstraint);
   }

   /**
    * Creates for every Sim-Constraint one corresponding Democles-Constraint 
    * @return true if minimum one constraint exists
    */
   private void setConstraints()
   {
      if (!topologyPattern.getConstraints().isEmpty())
      {
         for (GraphElementConstraint c : topologyPattern.getConstraints())
         {
            createGraphElementConstraint(c);
         }
      }
   }

   /**
    * Transforms a Sim-Constraints into Democles-Constraint
    * @param GraphElementConstraint
    */
   private void createGraphElementConstraint(GraphElementConstraint c)
   {
      ArrayList<Variable> constraintVariables = new ArrayList<Variable>();
      for (UniqueID id : c.getVariables())
      {
         if (id instanceof EdgeID)
         {
            EdgeID edgeId = (EdgeID) id;
            constraintVariables.add(edges.get(edgeId));
         } else if (id instanceof INodeID)
         {
            INodeID nodeId = (INodeID) id;
            constraintVariables.add(nodes.get(nodeId));
         }
      }
      ConstraintVariable[] constraintVariablesAsArray = constraintVariables.toArray(new ConstraintVariable[constraintVariables.size()]);

      constraints[constraintCounter++] = new Constraint(constraintVariablesAsArray, constraintTypeModule.getConstraintType(c));
   }

}
