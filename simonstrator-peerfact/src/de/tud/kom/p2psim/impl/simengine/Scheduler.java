/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.simengine;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.SchedulerComponent;
import de.tudarmstadt.maki.simonstrator.api.component.core.TimeComponent;

/**
 * The Scheduler enables the insertion of the active components into the
 * simulator that can generate events. It ensures the correct execution of those
 * generated events in order to provide valid experiments. The duration of each
 * experiment is controlled by the scheduler and the parameters defined by the
 * Application.
 * 
 * @author Sebastian Kaune
 */
public class Scheduler implements EventHandler,
		SchedulerComponent, TimeComponent {

	// Flag to allow the compiler to remove the unneeded debug code
	private static final boolean DEBUG_CODE = false;

	// private static final boolean DEBUG_CODE = true;

	private static final long SCHEDULER_WAKEUP_INTERVAL_IN_REALTIME = 5 * Time.SECOND;

	private static final long SCHEDULER_WAKEUP_INTERVAL_IN_VIRTUALTIME = 10 * Time.MINUTE;

	private long statusInterval = SCHEDULER_WAKEUP_INTERVAL_IN_VIRTUALTIME;

	private static final int INITIAL_QUEUE_CAPACITY = 5000;

	private long processedEventCounter;

	private long endTime;

	private long currentTime;

	private long newRoundsCurrentTime;

	private final PriorityQueue<SchedulerEvent> eventQueue;

	private final ConcurrentLinkedQueue<SchedulerEvent> outOfOrderQueue;

	private final boolean statusEvent;

	private boolean processEvents = true;

	private boolean realTime = false;

	private double timeSkew = 0.0;

	private boolean simulationSpeedLocked = false;

	private List<SimulationEventListener> listeners = new LinkedList<SimulationEventListener>();

	private long debugTransLayerMessageCounter = 0;

	protected static final int TYPE_NONE = 0;

	protected static final int TYPE_STATUS = 1;

	protected static final int TYPE_START = 2;

	protected static final int TYPE_END = 3;

	/**
	 * Constructs a new scheduler instance using a calendar queue. If desired,
	 * status events about the progress of the simulation will be plotted.
	 * 
	 * @param statusEvent
	 *            the flag which speficies if status events will be plotted
	 */
	public Scheduler(boolean statusEvent) {
		this.eventQueue = new PriorityQueue<SchedulerEvent>(
				INITIAL_QUEUE_CAPACITY, new EventComparator());
		this.outOfOrderQueue = new ConcurrentLinkedQueue<SchedulerEvent>();
		this.endTime = -1;
		this.processedEventCounter = 0;
		this.currentTime = 0;
		this.newRoundsCurrentTime = 0;
		this.statusEvent = statusEvent;

		if (DEBUG_CODE) {
			LiveMonitoring.addProgressValue(new LiveMonitoring.ProgressValue() {
				@Override
				public String getName() {
					return "Messages to be processed in trans layer";
				}

				@Override
				public String getValue() {
					return "" + debugTransLayerMessageCounter;
				}
			});
		}
	}

	@Override
	public void scheduleIn(long time, EventHandler handler, Object content,
			int type) {
		assert time >= 0 : "event " + content + " has time " + time;
		SchedulerEvent event = new SchedulerEvent(content, getCurrentTime()
				+ time, handler, type);
		synchronized (this.eventQueue) {
			this.eventQueue.add(event);
			assert (this.eventQueue.peek().getSimulationTime() >= getCurrentTime());
		}
	}

	protected void scheduleOwnIn(long time, Object content, int type) {
		assert time >= 0 : "event " + content + " has time " + time;
		SchedulerEvent event = new SchedulerEvent(content, getCurrentTime()
				+ time, this, type, type);
		synchronized (this.eventQueue) {
			this.eventQueue.add(event);
			assert (this.eventQueue.peek().getSimulationTime() >= getCurrentTime());
		}
	}

	public void reset() {
		eventQueue.clear();
		outOfOrderQueue.clear();
		this.endTime = -1;
		this.processedEventCounter = 0;
		this.currentTime = 0;
		this.newRoundsCurrentTime = 0;
	}

	/**
	 * Wake up thread safe and insert event immediately.
	 * 
	 * @param content
	 *            the content
	 * @param handler
	 *            the handler
	 * @param eventType
	 *            the event type
	 */
	// synchronized public void wakeUpThreadSafeAndInsertEventImmediately(
	// Object content, EventHandler handler,
	// SimulationEvent.Type eventType) {
	//
	// /* Add to queue. */
	// this.outOfOrderQueue.add(new SchedulerEventImpl(eventType, content,
	// Long.MIN_VALUE, handler));
	//
	// /* Scheduler wakes up. */
	// this.notifyAll();
	//
	// }

	/**
	 * Starts the scheduler and begins processing the events
	 */
	public void start() {
		if (this.endTime == -1) {
			throw new IllegalStateException("No end time configured");
		}

		if (this.statusEvent) {
			scheduleOwnIn(statusInterval, null, TYPE_STATUS);
		}

		while (!this.eventQueue.isEmpty()) {
			if (!processNextEvent()) {
				break;
			}
		}
		Monitor.log(Scheduler.class, Level.INFO,
				"Simulated realtime: " + Time.getFormattedTime()
						+ " - End of simulation.\n Scheduler processed in total "
						+ this.processedEventCounter + " events with "
						+ this.eventQueue.size()
						+ " unprocessed events still in queue");
	}

	/**
	 * Sets the end time at which the simulation framework will finish at the
	 * latest the simulation , irrespective if there are still unprocessed
	 * events in the event queue.
	 * 
	 * @param endTime
	 *            point in time at which the simulator will finish at the latest
	 */
	void setFinishAt(long endTime) {
		if (endTime < 0)
			throw new IllegalArgumentException("Negative end time");
		this.endTime = endTime;
		// this is a small hack to assure that all other events are processed
		// before
		scheduleOwnIn(endTime + 1, null, TYPE_END);
	}

	/**
	 * Process the next event from the event queue.
	 * 
	 * @return whether an event was processed
	 */
	synchronized private boolean processNextEvent() {

		if (!processEvents) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/*
		 * In this context, real-time does not allow for additional time skew
		 */
		if (realTime) {
			assert timeSkew == 0;
			/* First: Handle all real events. */
			boolean unhandledRegularEventsInPast = true;
			while (unhandledRegularEventsInPast) {
				unhandledRegularEventsInPast = false;

				final SchedulerEvent nextEvent;
				synchronized (this.eventQueue) {
					nextEvent = this.eventQueue.peek();
				}

				assert (nextEvent.getSimulationTime() >= currentTime) : "Next event: "
						+ nextEvent.getSimulationTime()
						+ ", but current "
						+ currentTime + ".";

				if (nextEvent.getSimulationTime() <= newRoundsCurrentTime) {

					/* Ok process right now. Too late anyway. */

					final SchedulerEvent realEvent;
					synchronized (this.eventQueue) {
						realEvent = this.eventQueue.remove();
					}

					processedEventCounter++;
					currentTime = realEvent.getSimulationTime();
					EventHandler handler = realEvent.handler;
					handler.eventOccurred(realEvent.data, realEvent.type);
					notifyListeners(realEvent, realEvent.handler);

					if (realEvent.schedulerType == TYPE_END)
						return false;
					
					synchronized (this.eventQueue) {
						if (this.eventQueue.peek().getSimulationTime() < newRoundsCurrentTime) {
							unhandledRegularEventsInPast = true;
						}
					}

				}
			}

			/* Second: Actually move time forward. */
			assert (currentTime <= newRoundsCurrentTime);
			currentTime = newRoundsCurrentTime;

			/*
			 * Third: Handle all events which happend while we were away. RIGHT
			 * NOW.
			 */
			while (!outOfOrderQueue.isEmpty()) {
				SchedulerEvent realEvent = outOfOrderQueue.remove();

				// Override, simTime is 0 anyway.
				assert (realEvent.simTime == Long.MIN_VALUE);
				realEvent.simTime = currentTime;

				EventHandler handler = realEvent.handler;
				handler.eventOccurred(realEvent.data, realEvent.type);
				notifyListeners(realEvent, realEvent.handler);
			}

			/* Third: All done, go to sleep and set newCurrentTime. */
			final SchedulerEvent peekedEvent;
			synchronized (this.eventQueue) {
				peekedEvent = this.eventQueue.peek();
			}

			assert (peekedEvent.getSimulationTime() >= currentTime) : "Next event: "
					+ peekedEvent.getSimulationTime()
					+ ", but current "
					+ currentTime + ".";

			if (peekedEvent.getSimulationTime() > currentTime) {

				/* Ok we have some time to sleep now. */

				// log.debug("Scheduler next: " +
				// this.eventQueue.peek().getSimulationTime() + " time");
				final long virtualTime_timeToWait = peekedEvent
						.getSimulationTime() - currentTime;
				final long realTime_timeToWaitMs = (virtualTime_timeToWait / Time.MILLISECOND);

				/*
				 * ATTENTION: Magic happening here!
				 * 
				 * Correct for uncertainties of going to real sleep.
				 */
				final long SLEEP_CORRECTION_FACTOR = 400;
				final long realTime_errorMs = System.currentTimeMillis()
 - 0 /* Starttime */
						- (currentTime / Time.MILLISECOND);
				final long realTime_correctedTimeToWaitMs = realTime_timeToWaitMs
						- (realTime_errorMs / SLEEP_CORRECTION_FACTOR);
				/* End: Correct for uncertainties of going to real sleep. */

				if (realTime_correctedTimeToWaitMs > 0
						&& peekedEvent.schedulerType != TYPE_END) {

					// log.debug("Scheduler sleeping: " +
					// realTime_correctedTimeToWaitMs + " milliseconds");
					final long realTime_BeforeSleepNs = System.nanoTime();
					try {
						this.wait(realTime_correctedTimeToWaitMs);
					} catch (InterruptedException e) {
						/* Should not be thrown. */
					}

					/*
					 * Needs to be recalculated, as notifyAll() might have been
					 * called.
					 */
					final long realTime_ElapsedNs = System.nanoTime()
							- realTime_BeforeSleepNs;
					newRoundsCurrentTime += Time.NANOSECOND
							* realTime_ElapsedNs;

					// log.debug("Scheduler alive again, slept " +
					// (realTime_ElapsedNs / 1e6) + " milliseconds");

				} else {

					/* Ok we didn't sleep as resolution too low. Just skip. */
					newRoundsCurrentTime = peekedEvent.getSimulationTime();
				}

				return peekedEvent.schedulerType != TYPE_END;
			}
		}

		/*
		 * Actually remove Event from queue now. Might have changed as we were
		 * sleeping above.
		 */
		processedEventCounter++;
		final SchedulerEvent realEvent = this.eventQueue.remove();

		assert (realEvent.getSimulationTime() >= currentTime) : "Next event: "
				+ realEvent.getSimulationTime() + ", but current "
				+ currentTime + ".";

		if (timeSkew > 0) {
			/*
			 * Simulations without a distributed queue, but with time skew -
			 * used for local visualizations
			 */
			assert realTime == false;
			Long timeToWait = realEvent.getSimulationTime() - currentTime;
			if (timeToWait > 0) {
				Monitor.log(Scheduler.class, Level.DEBUG,
						"Scheduler sleeping: " + timeToWait / 1000
								+ " milliseconds");
				try {
					Thread.sleep((long) ((timeToWait / 1000) / timeSkew));

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		currentTime = realEvent.getSimulationTime();
		EventHandler handler = realEvent.handler;
		handler.eventOccurred(realEvent.data, realEvent.type);
		notifyListeners(realEvent, realEvent.handler);

		if (realEvent.schedulerType == TYPE_END) {
			currentTime = realEvent.getSimulationTime() - 1;
			// Here, we hide that the end event is scheduled at endTime+1
			return false;
		}
		else{
			return true;
		}
	}

	private void notifyListeners(SchedulerEvent event, EventHandler handler) {
		for (SimulationEventListener l : listeners)
			l.onSimulationEventOccurred(event, handler);
	}

	public void addSimulationEventListener(SimulationEventListener l) {
		listeners.add(l);
	}

	public void removeSimulationEventListener(SimulationEventListener l) {
		listeners.remove(l);
	}

	public static interface SimulationEventListener {
		public void onSimulationEventOccurred(SchedulerEvent event,
				EventHandler hdlr);
	}

	/**
	 * Return whether event queue is empty.
	 * 
	 * @return whether event queue is empty.
	 */
	public boolean isEmpty() {
		return this.eventQueue.isEmpty();
	}

	/**
	 * Returns the current time of the scheduler
	 * 
	 * @return current scheduler time
	 */
	public long getCurrentTime() {
		return currentTime;
	}

	/**
	 * Returns the end time of the scheduler
	 * 
	 * @return
	 */
	public long getEndTime() {
		return endTime;
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == TYPE_STATUS) {
			if (eventQueue.size() != 1) {
				scheduleOwnIn(statusInterval, null, TYPE_STATUS);
			}
			Monitor.log(Scheduler.class, Level.INFO, "Simulated Realtime: %s",
					Time.getFormattedTime());
		}
	}

	/**
	 * Method for JUnit tests in order to verify the correctness
	 * 
	 * @return number of events in event queue.
	 */
	public int getEventQueueSize() {
		return this.eventQueue.size();
	}

	protected static final class SchedulerEvent implements
			Comparable<SchedulerEvent> {

		protected final int schedulerType;

		protected final int type;

		protected final EventHandler handler;

		protected final Object data;

		protected long simTime;
		
		protected long globalOrderIdx;
		
		protected static long globalOrderCounter = 0;

		protected SchedulerEvent(Object data, long simTime,
				EventHandler handler, int type) {
			this(data, simTime, handler, type, Scheduler.TYPE_NONE);
		}

		protected SchedulerEvent(Object data, long simTime,
				EventHandler handler, int type, int schedulerType) {
			this.schedulerType = schedulerType;
			this.data = data;
			this.handler = handler;
			this.simTime = simTime;
			this.type = type;
			this.globalOrderIdx = ++globalOrderCounter;
		}

		public long getSimulationTime() {
			return simTime;
		}

		public int compareTo(SchedulerEvent o) {
			int comp = Double.compare(this.simTime, o.simTime);
			return comp == 0 ? Double.compare(this.globalOrderIdx, o.globalOrderIdx) : comp;
		}
	}

	void setStatusInterval(long statusInterval) {
		this.statusInterval = statusInterval;
	}

	/**
	 * Returns wheather the scheduler is running in realTime mode or not.
	 * 
	 * @return the real time
	 */
	public boolean getRealTime() {
		return this.realTime;
	}

	/**
	 * a flag for slowing down the simulation down to real time. This flag only
	 * makes sense for simulations which run faster than real time
	 * 
	 * @param realTime
	 *            flag for switching the scheduler to real time mode
	 */
	public void setRealTime(boolean realTime) {
		if (!simulationSpeedLocked) {
			assert timeSkew == 0;
			this.realTime = realTime;
			this.statusInterval = (realTime) ? SCHEDULER_WAKEUP_INTERVAL_IN_REALTIME
					: SCHEDULER_WAKEUP_INTERVAL_IN_VIRTUALTIME;
		}
	}

	/**
	 * method for setting the time skew. A time skew of 100 means, that the
	 * simulation runs 100 times faster than real time
	 * 
	 * @param timeSkew
	 *            the time skew
	 */
	public void setTimeSkew(double timeSkew) {
		if (!simulationSpeedLocked) {
			assert !realTime;
			this.timeSkew = timeSkew;
		}
	}

	/**
	 * Sets the simulation speed lock, changes in setRealTime or setTimeSkew
	 * will not apply while the lock is set.
	 * 
	 * @param locked
	 *            the new simulation speed locked
	 */
	public void setSimulationSpeedLocked(boolean locked) {
		this.simulationSpeedLocked = locked;
	}

	public void pause() {
		processEvents = false;
	}

	public boolean isPaused() {
		return !processEvents;
	}

	synchronized public void unpause() {
		processEvents = true;
		notifyAll();
	}

	/**
	 * The comparator
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 05.03.2012
	 */
	public class EventComparator implements Comparator<SchedulerEvent> {

		@Override
		public int compare(SchedulerEvent o1, SchedulerEvent o2) {
			int comp = Double.compare(o1.simTime, o2.simTime);
			return (comp == 0 ? Double.compare(o1.globalOrderIdx, o2.globalOrderIdx) : comp);
		}

	}
}
