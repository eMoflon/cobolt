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


package de.tud.kom.p2psim.impl.util.scheduling;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.operation.AbstractOperation;
import de.tudarmstadt.maki.simonstrator.api.operation.Operations;

/**
 * Scheduler using the Start-Time Fair Queueing scheduling algorithm as proposed by
 * Pawan Goyal, Harrick M. Vin, and Haichen Cheng, using the IScheduler interface.
 * <br>
 * I have implemented this algorithm originally for use in Gia, where it enables
 * flow control.
 * <br><br>
 * 
 * For more on that, see Pawan Goyal, Harrick M. Vin, and Haichen Cheng -
 * "Start-Time Fair Queueing: A Scheduling Algorithm for Integrated Services Packet Switching Networks",
 * IEEE/ACM TRANSACTIONS ON NETWORKING, VOL. 5, NO. 5, OCTOBER 1997
 * 
 * @author Leo Nobach
 *
 * @param <TScheduleObject>
 */
public class StartTimeFairQueueingScheduler<TScheduleObject> implements IScheduler<TScheduleObject> {

	private ISchedulableResource<TScheduleObject> res;
	
	private QueueEntry packetInService = null;
	
	private long maximumFinishTagServiced = 0;
	
	Set<SFQSchedQ> queues = new HashSet<SFQSchedQ>();

	HostComponent owner;

	private boolean waitForService = false;
	
	/**
	 * Creates a new Start-time Fair Queueing scheduler with the given owner (needed by the simulator
	 * to schedule properly) and the given resource where the scheduled objects shall be delivered.
	 * @param owner
	 * @param res
	 */
	public StartTimeFairQueueingScheduler(HostComponent owner,
			ISchedulableResource<TScheduleObject> res) {
		this.res=res;
		this.owner=owner;
	}
	
	void arrive(SFQSchedQ q, TScheduleObject obj, long serviceTime) {
		QueueEntry lastElem = q.getLastElementEnqueued();
		
		long startTag = Math.max(getVirtualTime(), (lastElem==null)?0:lastElem.getEndTag());
		long weightedServiceTime = (long)(serviceTime/q.getWeight());
		long endTag = startTag + weightedServiceTime;
		
		QueueEntry arrivedObj = new QueueEntry(obj, startTag, endTag, serviceTime);
		
		if (packetInService == null) {
			service(arrivedObj);
			return;
		}
		//QueryDebugger.getInstance().write(owner + ": Enqueueing element " + arrivedObj + ". Queue length: " + q.getSize());
		q.enqueue(arrivedObj);
	}
	
	@Override
	public ISchedQueue<TScheduleObject> createNewSchedQueue(double weight) {
		SFQSchedQ newQ = new SFQSchedQ(createNewQueue(), weight);
		queues.add(newQ);
		return newQ;
	}
	
	Queue<QueueEntry> createNewQueue() {
		return new LinkedBlockingQueue<QueueEntry>();	//may be overridden if you think you have queues that fit better.
	}

	void service(QueueEntry entryToService) {
		packetInService = entryToService;
		if (res.service(entryToService.getObject()))
			new WaitForNextService().scheduleWithDelay(entryToService.getServiceTime());
		else
			waitForService  = true;
	}
	
	public void poke() {
		if (waitForService) {
			if (res.service(packetInService.getObject())) {
				new WaitForNextService().scheduleWithDelay(packetInService.getServiceTime());
				waitForService = false;
			}
		}
	}

	void serviceNext() {
		long lowestStartTag = Long.MAX_VALUE;
		Queue<QueueEntry> lowestStartTagQ = null;
		for (SFQSchedQ info : queues) {
			Queue<QueueEntry> q = info.getQueue();
			if (!q.isEmpty()) {
				long elemStartTag = q.element().getStartTag();
				if (elemStartTag < lowestStartTag) {
					lowestStartTag = elemStartTag;
					lowestStartTagQ = q;
				}
			}
		}
		if (lowestStartTagQ != null) {
			QueueEntry element2service = lowestStartTagQ.remove();
			//QueryDebugger.getInstance().write(owner + ": Taking element " + element2service + " from queue " 
			//		+ lowestStartTagQ + " for servicing. Q length: " + lowestStartTagQ.size());
			service(element2service);
		} else {
			maximumFinishTagServiced = packetInService.getEndTag();
			packetInService = null;
		}
	}
	
	long getVirtualTime() {
		if (packetInService == null) return maximumFinishTagServiced;
		else return packetInService.getStartTag();
	}
	
	
	class SFQSchedQ implements ISchedQueue<TScheduleObject> {
		
		public SFQSchedQ(Queue<QueueEntry> queue, double weightOfOwner) {
			this.queue = queue;
			this.weightOfOwner = weightOfOwner;
		}
		
		Queue<QueueEntry> queue;
		QueueEntry lastElementEnqueued = null;
		private double weightOfOwner;
		
		public Queue<QueueEntry> getQueue() {
			return queue;
		}
		
		public QueueEntry getLastElementEnqueued() {
			return lastElementEnqueued;
		}
		
		public void enqueue(QueueEntry entry) {
			queue.add(entry);
			lastElementEnqueued = entry;
		}
		
		@Override
		public double getWeight() {
			return weightOfOwner;
		}
		
		@Override
		public void setWeight(double weightOfOwner) {
			this.weightOfOwner = weightOfOwner;
		}
		
		@Override
		public void arrive(TScheduleObject obj, long serviceTime) {
			StartTimeFairQueueingScheduler.this.arrive(this, obj, serviceTime);
		}

		@Override
		public void disconnect() {
			queues.remove(this);
		}
		
		public int getSize() {
			return queue.size();
		}
		
	}
	
	class QueueEntry {
		
		public TScheduleObject getObject() {
			return obj;
		}

		public long getStartTag() {
			return startTag;
		}

		public long getEndTag() {
			return endTag;
		}
		
		public long getServiceTime() {
			return serviceTime;
		}

		private TScheduleObject obj;
		private long startTag;
		private long endTag;
		private long serviceTime;

		public QueueEntry(TScheduleObject obj, long startTag, long endTag, long serviceTime) {
			this.obj = obj;
			this.startTag = startTag;
			this.endTag = endTag;
			this.serviceTime = serviceTime;
		}
		
		public String toString() {
			return "(" + obj + ", sT=" + startTag + ", " + ", eT=" + endTag + ")";
		}	
		
	}
	
	class WaitForNextService extends AbstractOperation<HostComponent, Object> {

		protected WaitForNextService() {
			super(owner, Operations.getEmptyCallback());
		}

		@Override
		protected void execute() {
			serviceNext();
		}

		@Override
		public Object getResult() {
			return null;
		}
		
	}
	
}
