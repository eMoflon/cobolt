package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run;

/**
 * Subclasses of this interface are meant to run at specific lifecycle phases of
 * a {@link TaskExecutor} (e.g., at termination).
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public interface TaskExecutorLifecycleHook {

	/**
	 * Runs the logic of this hook
	 */
	void run();

}
