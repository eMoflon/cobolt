package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;

/**
 * Default implementation base class for {@link ITopologyControlReconfigurationComponent}
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public abstract class AbstractTopologyControlReconfigurationComponent implements ITopologyControlReconfigurationComponent
{
   private double activationTimeInMinutes = 0.0;
   private boolean ignoreDecision = false;

   /**
    * Empty default implementation
    */
   @Override
   public void initialize(final TopologyControlComponent topologyControlComponent)
   {
      // nop
   }

   /**
    * Sets the activation time
    * @param activationTimeInMinutes activation time
    * @see #getActivationTimeInMinutes()
    */
   public void setActivationTimeInMinutes(final double activationTimeInMinutes)
   {
      this.activationTimeInMinutes = activationTimeInMinutes;
   }

   @Override
   public double getActivationTimeInMinutes()
   {
      return this.activationTimeInMinutes;
   }

   public void setIgnoreDecision(final boolean ignoreDecision)
   {
      this.ignoreDecision = ignoreDecision;
   }

   public boolean shallIgnoreDecision() {
      return this.ignoreDecision;

   }
}
