/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.network.routed;

import java.util.HashMap;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * An instance of this class is always passed with each fragment of a
 * partitioned message.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 23.03.2012
 */
public class FragmentReceivedInfo {

	/**
	 * For the broadcast case
	 */
	private HashMap<NetID, FragmentReceivedMap> receiverMaps = new HashMap<NetID, FragmentReceivedInfo.FragmentReceivedMap>();

	/**
	 * For the unicast case
	 */
	private FragmentReceivedMap unicastMap;

	private boolean notifiedListener = false;

	/**
	 * Received another Fragment. Is the message complete now? Use this for
	 * broadcasts.
	 * 
	 * @param receiver
	 * @param fragmentNumber
	 * @param totalNumberOfFragments
	 * @return
	 */
	public boolean receivedFragment(NetID receiver, int fragmentNumber,
			int totalNumberOfFragments) {
		if (fragmentNumber == 1 && totalNumberOfFragments == 1) {
			return true;
		}
		if (!receiverMaps.containsKey(receiver)) {
			receiverMaps.put(receiver, new FragmentReceivedMap(
					totalNumberOfFragments));
		}
		FragmentReceivedMap map = receiverMaps.get(receiver);
		return map.isCompleteNow(fragmentNumber);
	}

	/**
	 * Received another fragment, is the message complete now? Use this for
	 * unicasts.
	 * 
	 * @param fragmentNumber
	 * @param totalNumberOfFragments
	 * @return
	 */
	public boolean receivedFragment(int fragmentNumber,
			int totalNumberOfFragments) {
		if (unicastMap == null) {
			unicastMap = new FragmentReceivedMap(totalNumberOfFragments);
		}
		return unicastMap.isCompleteNow(fragmentNumber);
	}

	public boolean isListenerNotified() {
		return notifiedListener;
	}

	public void setListenerNotified() {
		notifiedListener = true;
	}

	/**
	 * Returns true, if the given segment has been received already by the
	 * broadcast-receiver.
	 * 
	 * @param receiver
	 * @param fragmentNumber
	 * @return
	 */
	public boolean isReceived(NetID receiver, int fragmentNumber) {
		FragmentReceivedMap map = receiverMaps.get(receiver);
		if (map == null) {
			return false;
		}
		return map.isReceived(fragmentNumber);
	}

	/**
	 * Returns true, if the given segment has already been received by the
	 * unicast receiver.
	 * 
	 * @param fragmentNumber
	 * @return
	 */
	public boolean isReceived(int fragmentNumber) {
		if (unicastMap == null) {
			return false;
		}
		return unicastMap.isReceived(fragmentNumber);
	}

	/**
	 * Returns true, if all fragments up (and including) the given number have
	 * been received.
	 * 
	 * @param fragmentNumber
	 * @return
	 */
	public boolean isAllReceivedUpTo(int fragmentNumber) {
		if (unicastMap == null) {
			throw new AssertionError("This is not supposed to happen...");
		}
		return unicastMap.isAllReceivedUpTo(fragmentNumber);
	}

	/**
	 * Map for one receiver, as broadcasts could be partitioned as well.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 23.03.2012
	 */
	private class FragmentReceivedMap {

		private boolean[] received;

		private int numberOfFragements;

		private int numberOfUniqueReceivedFragments = 0;

		public FragmentReceivedMap(int numberOfFragments) {
			this.received = new boolean[numberOfFragments];
			this.numberOfFragements = numberOfFragments;
			this.numberOfUniqueReceivedFragments = 0;
			for (int i = 0; i < received.length; i++) {
				received[i] = false;
			}
		}

		/**
		 * This is called whenever a fragment arrives - it returns true, as soon
		 * as the message arrived completely
		 * 
		 * @param receivedFragment
		 * @return
		 */
		public boolean isCompleteNow(int receivedFragment) {
			if (!isReceived(receivedFragment)) {
				received[receivedFragment - 1] = true;
				this.numberOfUniqueReceivedFragments++;
			}

			return this.numberOfUniqueReceivedFragments == this.numberOfFragements;
		}

		/**
		 * Returns true, if all segments up to and including fragmentNumber have
		 * been received
		 * 
		 * @param fragmentNumber
		 * @return
		 */
		public boolean isAllReceivedUpTo(int fragmentNumber) {
			boolean complete = true;
			int maxFragments = Math.min(fragmentNumber, received.length);
			for (int i = 0; i < maxFragments; i++) {
				complete = complete & received[i];
			}
			return complete;
		}

		/**
		 * True, if the given fragment (starting from 1) is already marked as
		 * received
		 * 
		 * @param fragment
		 * @return
		 */
		public boolean isReceived(int fragment) {
			return received[fragment - 1];
		}

		@Override
		public String toString() {
			String print = "";
			for (int i = 0; i < received.length; i++) {
				print += (received[i] ? 'X' : '#');
			}
			return print;
		}

	}

}