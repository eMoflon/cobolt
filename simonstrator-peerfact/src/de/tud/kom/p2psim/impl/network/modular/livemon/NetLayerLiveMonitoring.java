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


package de.tud.kom.p2psim.impl.network.modular.livemon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.MultiSet;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.toolkits.StringToolkit;
import de.tud.kom.p2psim.impl.util.toolkits.TimeToolkit;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class NetLayerLiveMonitoring {

	static final boolean DBG_TRAF_CTRL_DROP = false;
	
	static MsgDrop trafCtrlMsgDrop = new MsgDrop("Traffic Ctrl");
	static MsgDrop subnetMsgDrop = new MsgDrop("Subnet");
	static MsgDrop offlineMsgDrop = new MsgDrop("Offline");
	static MsgDrop routingMsgDrop = new MsgDrop("Routing");
	
	private static Writer bufWr;
	
	public static void register() {
		LiveMonitoring.addProgressValue(trafCtrlMsgDrop);
		LiveMonitoring.addProgressValue(subnetMsgDrop);
		LiveMonitoring.addProgressValue(offlineMsgDrop);
		LiveMonitoring.addProgressValue(routingMsgDrop);
		if (DBG_TRAF_CTRL_DROP) {
			try {
				bufWr = new BufferedWriter(new FileWriter(new File("outputs/trafCtrlPacketLossDump")));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static MsgDrop getTrafCtrlMsgDrop() {
		return trafCtrlMsgDrop;
	}
	public static MsgDrop getSubnetMsgDrop() {
		return subnetMsgDrop;
	}
	public static MsgDrop getOfflineMsgDrop() {
		return offlineMsgDrop;
	}
	public static MsgDrop getRoutingMsgDrop() {
		return routingMsgDrop;
	}

	public static void droppedMessageTrafCtrl(NetMessage netMsg) {
		if (DBG_TRAF_CTRL_DROP) {
			try {
				Message msg = netMsg.getPayload().getPayload();
				//bufWr.write("Dropped " + msg.getClass().getSimpleName() + ", s=" + netMsg.getSender() + ", r=" + netMsg.getReceiver() + ", msg='" + msg + "'\n");
				
				Tuple<Class<? extends Message>, Tuple<NetID, NetID>> msgTpl = new Tuple<Class<? extends Message>, Tuple<NetID,NetID>>(msg.getClass(), new Tuple<NetID, NetID>(netMsg.getSender(), netMsg.getReceiver()));
				msgAccu.addOccurrence(msgTpl);
				msgs++;
				if (msgs >= 10000) {
					dumpMsgs();
					msgAccu.clear();
					msgs = 0;
				}
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		getTrafCtrlMsgDrop().droppedMessage();
	}
	
	private static void dumpMsgs() throws IOException {
		Map<Tuple<Class<? extends Message>, Tuple<NetID, NetID>>, Integer> msgList = msgAccu.getUnmodifiableMap();
		
		bufWr.write("=================== Msgs dropped until " + TimeToolkit.getPFSDefaultTimeToolkit().timeStringFromLong(Time.getCurrentTime()) + "============\n");
		
		for (Entry<Tuple<Class<? extends Message>, Tuple<NetID, NetID>>, Integer> tpl : msgList.entrySet()) {
			bufWr.write(StringToolkit.padFixed(tpl.getKey().getA().getSimpleName() + ", s=" + tpl.getKey().getB().getA() + ", r=" + tpl.getKey().getB().getB() + ": ", 100) + tpl.getValue() + "\n");
		}
	}

	static MultiSet<Tuple<Class<? extends Message>, Tuple<NetID, NetID>>> msgAccu = new MultiSet<Tuple<Class<? extends Message>,Tuple<NetID,NetID>>>();
	static long msgs = 0;
}
