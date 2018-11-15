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

import java.util.Random;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetLatencyModel;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.gnp.topology.CountryLookup;
import de.tud.kom.p2psim.impl.network.gnp.topology.PingErLookup;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import umontreal.iro.lecuyer.probdist.LognormalDist;

public class GnpLatencyModel implements NetLatencyModel {

	private Random rnd = Randoms.getRandom(GnpLatencyModel.class);

	public static final int MSS = PhyType.ETHERNET.getDefaultMTU()
			- NetProtocol.IPv4.getHeaderSize()
			- TransProtocol.TCP.getHeaderSize();

	private static PingErLookup pingErLookup;

	private static CountryLookup countryLookup;

	private boolean usePingErInsteadOfGnp = false;

	private boolean useAnalyticalFunctionInsteadOfGnp = false;

	private boolean usePingErJitter = false;

	private boolean usePingErPacketLoss = false;

	public void init(PingErLookup pingErLookup, CountryLookup countryLookup) {
		GnpLatencyModel.pingErLookup = pingErLookup;
		GnpLatencyModel.countryLookup = countryLookup;
	}

	private double getMinimumRTT(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double minRtt = 0.0;
		if (usePingErInsteadOfGnp) {
			minRtt = pingErLookup.getMinimumRtt(ccSender, ccReceiver, countryLookup);
		} else if (useAnalyticalFunctionInsteadOfGnp) {
			double distance = GeoLocationOracle.getGeographicalDistance(sender.getNetID(), receiver.getNetID());
			minRtt = 62 + (0.02 * distance);
		} else {
			Location senderPos = sender.getNetPosition();
			Location receiverPos = receiver.getNetPosition();
			minRtt = senderPos.distanceTo(receiverPos);
		}
		return minRtt;
	}

	private double getPacketLossProbability(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double twoWayLossRate = 0.0;
		double oneWayLossRate = 0.0;
		if (usePingErPacketLoss) {
			twoWayLossRate = pingErLookup.getPacktLossRate(ccSender, ccReceiver, countryLookup);
			twoWayLossRate /= 100;
			oneWayLossRate = 1 - Math.sqrt(1 - twoWayLossRate);
		}
		return oneWayLossRate;

	}

	private double getNextJitter(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double randomJitter = 0.0;
		if (usePingErJitter) {
			LognormalDist distri = pingErLookup.getJitterDistribution(ccSender, ccReceiver, countryLookup);
			randomJitter = distri.inverseF(rnd.nextDouble());
		}
		return randomJitter;

	}

	private double getAverageJitter(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double jitter = 0.0;
		if (usePingErJitter) {
			jitter = pingErLookup.getAverageRtt(ccSender, ccReceiver, countryLookup) - pingErLookup.getMinimumRtt(ccSender, ccReceiver, countryLookup);
		}
		return jitter;
	}

	public double getUDPerrorProbability(GnpNetLayer sender, GnpNetLayer receiver, IPv4Message msg) {
		if (msg.getPayload().getSize() > 65507)
			throw new IllegalArgumentException("Message-Size ist too big for a UDP-Datagramm (max 65507 byte)");
		double lp = getPacketLossProbability(sender, receiver);
		double errorProb = 1 - Math.pow(1 - lp, msg.getNoOfFragments());
		return errorProb;
	}

	public double getTcpThroughput(GnpNetLayer sender, GnpNetLayer receiver) {
		double minRtt = getMinimumRTT(sender, receiver);
		double averageJitter = getAverageJitter(sender, receiver);
		double packetLossRate = getPacketLossProbability(sender, receiver);
		double mathisBW = ((MSS * 1000) / (minRtt + averageJitter)) * Math.sqrt(1.5 / packetLossRate);
		return mathisBW;
	}

	public long getTransmissionDelay(double bytes, double bandwidth) {
		double messageTime = bytes / bandwidth;
		long delay = Math.round((messageTime * Time.SECOND));
		return delay;
	}

	public long getPropagationDelay(GnpNetLayer sender, GnpNetLayer receiver) {
		double minRtt = getMinimumRTT(sender, receiver);
		double randomJitter = getNextJitter(sender, receiver);
		double receiveTime = (minRtt + randomJitter) / 2.0;
		long latency = Math.round(receiveTime * Time.MILLISECOND);
		return latency;
	}

	public long getLatency(NetLayer sender, NetLayer receiver) {
		return getPropagationDelay((GnpNetLayer) sender, (GnpNetLayer) receiver);
	}

	public void setUsePingErRttData(boolean pingErRtt) {
		usePingErInsteadOfGnp = pingErRtt;
	}

	public void setUseAnalyticalRtt(boolean analyticalRtt) {
		useAnalyticalFunctionInsteadOfGnp = analyticalRtt;
	}

	public void setUsePingErJitter(boolean pingErRtt) {
		usePingErJitter = pingErRtt;
	}

	public void setUsePingErPacketLoss(boolean pingErPacketLoss) {
		usePingErPacketLoss = pingErPacketLoss;
	}

}