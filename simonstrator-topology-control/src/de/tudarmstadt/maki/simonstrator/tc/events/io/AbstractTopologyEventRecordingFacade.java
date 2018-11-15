package de.tudarmstadt.maki.simonstrator.tc.events.io;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.AddEdgeEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.AddNodeEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EndOfContextEventHandlingPhaseEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EndOfSimulationEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EndOfTopologyControlPhaseEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.Event;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EventLog;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.RemoveEdgeEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.RemoveNodeEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.UpdateEdgeEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.UpdateNodeEvent;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacade_ImplBase;

/**
 * Implementation base class for all event-recording facades
 *
 * @author Roland Kluge - Initial implementation
 */
public abstract class AbstractTopologyEventRecordingFacade extends TopologyControlFacade_ImplBase
{
   private final EventLog eventLog;

   public AbstractTopologyEventRecordingFacade()
   {
      this.eventLog = new EventLog();
   }

   /**
    * Does nothing. This facade just records
    */
   @Override
   public void run(TopologyControlAlgorithmParamters parameters)
   {
      // Nop
   }

   @Override
   public INode addNode(INode prototype)
   {
      final AddNodeEvent event = new AddNodeEvent(prototype);

      registerEvent(event);
      for (final SiSType<?> property : prototype.getProperties().keySet()) {
         updateNodeAttributeInternal(prototype, property);
      }
      return super.addNode(prototype);
   }

   @Override
   public void removeNode(INodeID node)
   {
      final RemoveNodeEvent event = new RemoveNodeEvent(this.getGraph().getNode(node));

      registerEvent(event);
      super.removeNode(node);
   }

   @Override
   public IEdge addEdge(IEdge prototype)
   {
      final AddEdgeEvent event = new AddEdgeEvent(prototype);

      registerEvent(event);
      for (final SiSType<?> property : prototype.getProperties().keySet()) {
         updateEdgeAttributeInternal(prototype, property);
      }
      return super.addEdge(prototype);
   }

   @Override
   public void removeEdge(IEdge edge)
   {
      final RemoveEdgeEvent event = new RemoveEdgeEvent(edge);

      registerEvent(event);
      super.removeEdge(edge);
   }

   @Override
   public <T> void updateNodeAttribute(INode node, SiSType<T> property)
   {
      updateNodeAttributeInternal(node, property);
      super.updateNodeAttribute(node, property);
   }

   @Override
   public <T> void updateEdgeAttribute(IEdge edge, SiSType<T> property)
   {
      updateEdgeAttributeInternal(edge, property);
      super.updateEdgeAttribute(edge, property);
   }

   @Override
   public void endContextEventSequence()
   {
      this.registerEvent(new EndOfContextEventHandlingPhaseEvent());
      super.endContextEventSequence();
   }

   @Override
   public void endTopologyControlSequence()
   {
      this.registerEvent(new EndOfTopologyControlPhaseEvent());
      super.endTopologyControlSequence();
   }

   @Override
   public void shutdown()
   {
      this.registerEvent(new EndOfSimulationEvent());
      super.shutdown();
   }

   public EventLog getEventLog()
   {
      return this.eventLog;
   }

   public void clearEventLog()
   {
      this.getEventLog().clear();
   }

   public <T> void updateNodeAttributeInternal(INode node, SiSType<T> property)
   {
      final UpdateNodeEvent event = new UpdateNodeEvent(node, property);

      registerEvent(event);
   }

   private <T> void updateEdgeAttributeInternal(IEdge edge, SiSType<T> property)
   {
      final UpdateEdgeEvent event = new UpdateEdgeEvent(edge, property);
      registerEvent(event);
   }

   /**
    * Sets the creation timestamp of the given Event and adds it to the event log
    * @param event the event to register
    */
   protected void registerEvent(final Event event)
   {
      configureTime(event);
      this.eventLog.addEvent(event);
   }

   protected void configureTime(Event event)
   {
      event.setTimestamp(Time.getCurrentTime());
      event.setFormattedTime(Time.getFormattedTime());
   }

}
