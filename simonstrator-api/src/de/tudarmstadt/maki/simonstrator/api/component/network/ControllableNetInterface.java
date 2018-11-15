package de.tudarmstadt.maki.simonstrator.api.component.network;

import de.tudarmstadt.maki.simonstrator.api.component.LifecycleComponent;

/**
 * Extending the NetInterface, this interface can be turned on and off by the
 * app/overlay. The respective ConnectivityListener will be notified.
 *
 * @author Bjoern Richerzhagen
 *
 */
public interface ControllableNetInterface
		extends NetInterface, LifecycleComponent {

	/*
	 * This interface is just a marker for ChurnEnabledComponents
	 */

    /**
	 * Activates the NetInterface, if it was previously disabled.
	 * 
	 * @deprecated use methods provided by the {@link LifecycleComponent}
	 *             instead.
	 */
	@Deprecated
    public void turnOn();

    /**
	 * Deactivates the NetInterface, if it was previously enabled.
	 * 
	 * @deprecated use methods provided by the {@link LifecycleComponent}
	 *             instead.
	 */
    public void turnOff();

}
