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


package de.tud.kom.p2psim.impl.network.bandwidthDetermination;


/**
 * The implemented bandwidth distribution is taken from the latest OECD
 * broadband report (For further information, please see Oecd broadband portal,
 * http://www.oecd.org/sti/ict/broadband). Values are provided in bit/s
 * 
 * @author Leo Nobach (additional changes: Dominik Stingl)
 * 
 */
public class OECDReportBandwidthDetermination extends PickFromRandomGroupBandwidthDetermination {

	@Override
	public void addBandwidthGroups() {
		addNewBandwidth(7000 * 8, 7000 * 8, 600); // Modem 15%
		addNewBandwidth(8000 * 8, 8000 * 8, 600); // ISDN
		addNewBandwidth(87355 * 8, 1202843 * 8, 1601); // DSL 41%
		addNewBandwidth(89000 * 8, 236111 * 8, 47); // Wireless 1%
		addNewBandwidth(158001 * 8, 1856966 * 8, 758); // Cable 19%
		addNewBandwidth(4280818 * 8, 8165793 * 8, 258); // FTTx 6%
		// 3864
	}


}
