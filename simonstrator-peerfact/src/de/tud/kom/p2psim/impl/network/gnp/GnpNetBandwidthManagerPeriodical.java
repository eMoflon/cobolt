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


package de.tud.kom.p2psim.impl.network.gnp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;

public class GnpNetBandwidthManagerPeriodical extends
		AbstractGnpNetBandwidthManager {

	private HashSet<NetLayer> changedSenders;

	private HashSet<NetLayer> changedReceivers;

	private Set<GnpNetBandwidthAllocation> changedAllocations; // within last

	// realocation

	public GnpNetBandwidthManagerPeriodical() {
		super();
		changedSenders = new HashSet<NetLayer>();
		changedReceivers = new HashSet<NetLayer>();
		changedAllocations = new HashSet<GnpNetBandwidthAllocation>();
	}

	@Override
	public GnpNetBandwidthAllocation addConnection(AbstractNetLayer sender,
			AbstractNetLayer receiver, double bandwidth) {
		changedSenders.add(sender);
		changedReceivers.add(receiver);
		// sender.setCurrentUpBandwidth(sender.getMaxUploadBandwidth());
		// receiver.setCurrentDownBandwidth(receiver.getMaxDownloadBandwidth());
		GnpNetBandwidthAllocation ba = super.addConnection(sender, receiver,
				bandwidth);
		return ba;
	}

	@Override
	public GnpNetBandwidthAllocation removeConnection(AbstractNetLayer sender,
			AbstractNetLayer receiver, double bandwidth) {
		GnpNetBandwidthAllocation ba = super.removeConnection(sender, receiver,
				bandwidth);
		if (connectionsSenderToReceiver.containsKey(sender))
			changedSenders.add(sender);
		else
			changedSenders.remove(sender);
		if (connectionsReceiverToSender.containsKey(receiver))
			changedReceivers.add(receiver);
		else
			changedReceivers.remove(receiver);
		return ba;
	};

	@Override
	public void allocateBandwidth() {
		HashSet<AbstractNetLayer> chSenders = (HashSet<AbstractNetLayer>) changedSenders
				.clone();
		HashSet<AbstractNetLayer> chReceivers = (HashSet<AbstractNetLayer>) changedReceivers
				.clone();
		changedSenders.clear();
		changedReceivers.clear();
		changedAllocations.clear();
		for (AbstractNetLayer host : chSenders) {
			host.getCurrentBandwidth().setUpBW(host.getMaxBandwidth().getUpBW());
			Set<GnpNetBandwidthAllocation> temp = new HashSet<GnpNetBandwidthAllocation>();
			temp.addAll(connectionsSenderToReceiver.get(host).values());
			fairShare(temp, true);
		}
		for (AbstractNetLayer host : chReceivers) {
			host.getCurrentBandwidth().setDownBW(host.getMaxBandwidth().getDownBW());
			Set<GnpNetBandwidthAllocation> temp = new HashSet<GnpNetBandwidthAllocation>();
			temp.addAll(connectionsReceiverToSender.get(host).values());
			fairShare(temp, false);
		}
	}

	private void fairShare(Collection<GnpNetBandwidthAllocation> unassigned,
			boolean isSender) {

		if (unassigned.isEmpty())
			return;

		double x = Double.POSITIVE_INFINITY;

		GnpNetBandwidthAllocation min = null;
		for (GnpNetBandwidthAllocation ba : unassigned) {

			double bandwidth = 0;
			if (isSender)
				bandwidth = Math.min(ba.getReceiver()
						.getCurrentBandwidth().getDownBW()
						+ ba.getAllocatedBandwidth(), ba.getBandwidthNeeds());
			else
				bandwidth = Math.min(ba.getSender().getCurrentBandwidth().getUpBW()
						+ ba.getAllocatedBandwidth(), ba.getBandwidthNeeds());

			if (bandwidth < x) {
				x = bandwidth;
				min = ba;
			}
		}

		double bw = 0;
		if (isSender)
			bw = min.getSender().getCurrentBandwidth().getUpBW()
					/ unassigned.size();
		else
			bw = min.getReceiver().getCurrentBandwidth().getDownBW()
					/ unassigned.size();

		if (x < bw) {
			if (min.getAllocatedBandwidth() != x) {
				changedAllocations.add(min);
				min.setAllocatedBandwidth(x);
				if (isSender)
					changedReceivers.add(min.getReceiver());
				else
					changedSenders.add(min.getSender());
			}

			if (isSender) {
				BandwidthImpl curbw = (BandwidthImpl) min.getSender()
						.getCurrentBandwidth();
				curbw.setUpBW((long)(curbw.getUpBW() - min.getAllocatedBandwidth()));
			} else {
				BandwidthImpl curbw = (BandwidthImpl) min.getReceiver()
						.getCurrentBandwidth();
				curbw.setDownBW((long)(curbw.getDownBW() - min.getAllocatedBandwidth()));
			}
			unassigned.remove(min);
			fairShare(unassigned, isSender);
		} else {
			for (GnpNetBandwidthAllocation ba : unassigned) {
				if (ba.getAllocatedBandwidth() != bw) {
					changedAllocations.add(ba);
					ba.setAllocatedBandwidth(bw);
					if (isSender)
						changedReceivers.add(ba.getReceiver());
					else
						changedSenders.add(ba.getSender());
				}
				if (isSender)
					ba.getSender().getCurrentBandwidth().setUpBW(0);
				else
					ba.getReceiver().getCurrentBandwidth().setDownBW(0);
			}

		}
	}

	@Override
	public BandwidthAllocation getBandwidthAllocationType() {
		return AbstractGnpNetBandwidthManager.BandwidthAllocation.PERIODICAL;
	}

	@Override
	public Set<GnpNetBandwidthAllocation> getChangedAllocations() {
		return changedAllocations;
	}
}