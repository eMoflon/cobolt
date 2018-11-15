package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.filtering.EdgeFilter;

/**
 * This facade provides centralized access to one or more topology control
 * algorithms.
 *
 * Whenever the topology of the network changes, the facade needs to be notified
 * (e.g. via {@link #updateAttribute(IElement, GraphElementProperty)}).
 *
 * @author Roland Kluge - Initial API
 */
public interface ITopologyControlFacade
{

   /**
    * Sets this facade to use the given algorithm (by ID).
    */
   void configureAlgorithm(TopologyControlAlgorithmID algorithmID);

   /**
    * Runs the topology algorithm without parameters
    */
   void run();

   /**
    * Runs the topology algorithm with the given set of parameters
    */
   void run(TopologyControlAlgorithmParamters parameters);

   /**
    * (Re-)Creates the stored graph.
    */
   Graph getGraph();

   /**
    * Creates a new node in the stored graph using the given node as prototype.
    *
    * **Important:** The returned node is the node of the stored graph of this
    * facade.
    *
    * All properties (apart from those referring to {@link IEdge} and
    * {@link INode}) are copied to the new node.
    *
    * @param prototype
    *            the node that serves as prototype
    * @return the newly created node in the graph of this facade
    */
   INode addNode(INode prototype);

   /**
    * Creates a new edge in the stored graph using the given edge as prototype.
    *
    * **Important:** The returned edge is the edge of the stored graph of this
    * facade.
    *
    * All properties (apart from those referring to {@link IEdge} and
    * {@link INode}) are copied to the new edge.
    *
    * @param prototype
    *            the edge that serves as prototype
    * @return the newly created edge in the contained graph
    */
   IEdge addEdge(IEdge prototype);

   /**
    * Notifies the facade about a changed attribute of the given node
    *
    * @param element
    * @param property
    */
   <T> void updateNodeAttribute(INode node, SiSType<T> property);

   /**
    * Notifies the facade about a changed attribute of the given edge
    *
    * @param element
    * @param property
    */
   <T> void updateEdgeAttribute(IEdge edge, SiSType<T> property);

   /**
    * Notifies the facade about a removal of the given edge from the topology
    *
    * @param element
    */
   void removeEdge(IEdge element);

   /**
    * Notifies the facade about a removal of the given node from the topology
    *
    * @param element
    */
   void removeNode(INodeID node);

   /**
    * Returns all registered {@link ILinkStateListener}s
    */
   Collection<ILinkStateListener> getLinkStateListeners();

   /**
    * Registers a {@link ILinkStateListener}.
    */
   void addLinkStateListener(final ILinkStateListener listener);

   /**
    * Unregisters the give {@link ILinkStateListener}.
    */
   void removeLinkStateListener(final ILinkStateListener listener);

   /**
    * Returns all registered {@link IContextEventListener}s
    */
   Collection<IContextEventListener> getContextEventListeners();

   /**
    * Registers a {@link IContextEventListener}
    *
    * @see #addNode(INodeID, double)
    * @see #removeNode(INodeID)
    * @see #addEdge(INodeID, INodeID, double, double)
    * @see #removeEdge(IEdge)
    * @see #updateNodeAttribute(INode, SiSType)
    * @see #updateEdgeAttribute(IEdge, SiSType)
    */
   void addContextEventListener(final IContextEventListener listener);

   /**
    * Unregisters an {@link IContextEventListener}
    */
   void removeContextEventListener(final IContextEventListener listener);

   /**
    * Marks that a sequence of context events begins
    *
    * The handling of context events that occur in a context event sequence
    * should be deferred until {@link #endContextEventSequence()} is called.
    */
   void beginContextEventSequence();

   /**
    * Marks that a sequence of context events ends
    *
    * The context events that occurred in the context event sequence should be
    * performed now.
    */
   void endContextEventSequence();

   /**
    * Marks that a sequence of topology control steps ends
    */
   void endTopologyControlSequence();

   /**
    * Resets the constraint violation counter
    */
   void resetConstraintViolationCounter();

   /**
    * Returns the number of constraint violations since the last reset
    *
    * @return
    *
    * @see #resetConstraintViolationCounter()
    */
   int getConstraintViolationCount();

   /**
    * Checks all constraints that should hold after executing topology control
    */
   void checkConstraintsAfterTopologyControl();

   /**
    * Checks all constraints that should hold after handling a context event
    */
   void checkConstraintsAfterContextEvent();

   /**
    * Sets whether to suspend (true) or not (false) the notification of the
    * registered {@link IContextEventListener}s about context events
    *
    * @param muted
    *            true if muted, false otherwise
    */
   void setContextEventListenersMuted(boolean muted);

   /**
    * Returns true if this facade would like to be notified about context events
    */
   boolean areContextEventListenersMuted();

   /**
    * Sets whether to suspend (true) or not (false) the notification of the
    * registered {@link ILinkStateListener}s about context events
    *
    * @param muted
    *            true if muted, false otherwise
    */
   void setLinkStateModificationListenersMuted(boolean muted);

   /**
    * Returns true if this facade would like to be notified about link-state modifications
    */
   boolean areLinkStateModificationListenersMuted();

   /**
    * Marks the given {@link IEdge}s are each other's opposite
    * @param fwdEdge
    * @param bwdEdge
    */
   void connectOppositeEdges(IEdge fwdEdge, IEdge bwdEdge);

   /**
    * Unclassifies all links.
    * This method is more efficient than manually unclassifying all links because no context event handling is required.
    */
   void unclassifyAllLinks();

   /**
    * Returns true if this facade supports the given operation mode for the given algorithm
    * @param algorithmId TODO
    */
   boolean supportsOperationMode(TopologyControlAlgorithmID algorithmId, TopologyControlOperationMode mode);

   /**
    * Sets the given operation mode for the respective topology control
    * algorithm
    */
   void setOperationMode(TopologyControlOperationMode mode);

   /**
    * Sets the (assumed) size of the local view of the algorithm.
    *
    * The local view size specifies how large the known neighborhood of a node is.
    * @param localViewSize the local view size
    */
   void setLocalViewSize(int localViewSize);

   /**
    * Registers an {@link EdgeFilter}
    */
   void addEdgeFilter(final EdgeFilter filter);

   /**
    * Unregisters an {@link EdgeFilter}
    */
   void removeEdgeFilter(final EdgeFilter filter);

   /**
    * Unregisters all {@link EdgeFilter}s
    */
   void clearEdgeFilters();

   /**
    * Returns all registered {@link EdgeFilter}s
    * @return
    */
   Collection<EdgeFilter> getEdgeFilters();

   /**
    * Called when the containing component is being initalized.
    */
   void initalize();

   /**
    * Called when the containing component is going to shutdown.
    */
   void shutdown();

   /**
    * Returns the ID of the algorithm of this facade
    * @return the algorithm ID
    */
   TopologyControlAlgorithmID getConfiguredAlgorithm();

   /**
    * Registers the given {@link TopologyControlComponent} at this facade
    * @param topologyControlComponent the {@link TopologyControlComponent}
    */
   void setTopologyControlComponent(TopologyControlComponent topologyControlComponent);

}
