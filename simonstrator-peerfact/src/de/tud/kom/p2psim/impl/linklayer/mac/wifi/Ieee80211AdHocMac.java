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

package de.tud.kom.p2psim.impl.linklayer.mac.wifi;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.tud.kom.p2psim.api.analyzer.LinklayerAnalyzer;
import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacEventInformation;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.PropagationLossModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.Standard_802_11;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.WifiPreamble;
import de.tud.kom.p2psim.impl.linklayer.DefaultLinkMessageEvent;
import de.tud.kom.p2psim.impl.linklayer.mac.AbstractMacLayer;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.AbstractRateManager.RateManagerTypes;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.DcfManager.WifiState;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.wifi.WifiTopologyView;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.operation.AbstractOperation;

/**
 * This class implements the IEEE 802.11 MAC for the adHoc case. This is only a
 * simple implementation of the IEEE 802.11 standard, but has the same behavior. <br>
 * It supports the sending of Unicast message with and without RTS- and
 * CTS-Messages, plus the sending of messages as Broadcasts.<br>
 * The sending of RTS-, CTS- and ACK-Messages will be only simulated. This mean,
 * that we calculate the times, the energy consumption and as interferences, but
 * we send no message to the other MAC. This has the benefit that we have a
 * performance boost, but the drawback is, that this messages can not be
 * dropped.<br>
 * Additionally it handle no crypto or SSIDs. It can only exist one Network.
 * Different channels will be additionally not handled.<br>
 * Messages like Beacons are not available and will be not simulated.
 * <p>
 * The sending of the real data will be attended with the
 * {@link WifiTopologyView}. This is needed to calculate the drop of a
 * message.So it is important to add the RTS-,CTS-,ACK-Messages as interferences
 * and the real data as a transfer. Hence, this class works only with the
 * {@link WifiTopologyView}.
 * <p>
 * Additional the implementation works with a {@link DcfManager}, which will be
 * manipulated from this class during the sending and receiving of messages. The
 * DCF-Manager has the task to handle the state of the MAC and to calculate the
 * BackOffTimerEnd-Time.<br>
 * The BackOffTimer can be extended through other events. So we have a the
 * {@link GetSendSlotOperation} to get a send slot. The send slot will be ready,
 * if the BackOffTimer is 0. But through the extension of the BackOffTimer, is
 * the endTime not really fix. So it is possible that the
 * {@link GetSendSlotOperation} must be called multiple times. This can be a big
 * drawback, because it produce a many events, which are doing nothing.
 * <p>
 * <p>
 * This class inherit from {@link AbstractMacLayer}, but use few methods from
 * them. The main reason for the not using of few methods is, that the handling
 * of the drop is doing in the sender method, but this implementation needed it
 * in the receiver.
 * <p>
 * <b>NOTE:</b> If after the configuration a parameter is changed, you should
 * inform the underlay layer about the change.
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public class Ieee80211AdHocMac extends AbstractMacLayer {

	/*
	 * For Analyzing
	 */
	public static long _wifiBroadcastSent, _wifiUnicastSent, _wifiUnicastRcvd,
			_wifiBroadcastRcvd, _wifiUnicastDropped, _wifiResent,
			_wifiBroadcastDropped, _wifiBroadcastDataSent,
			_wifiBroadcastDataRcvd = 0;

	public static boolean _analyzersInitialized = false;

	/**
	 * Size of Frame Check Sequence in Bytes
	 */
	protected static int FCS = 4;

	/**
	 * CTS message size in Bytes
	 */
	protected static int CTS_MSG_SIZE = 10 + FCS; // macHeader + FCS

	/**
	 * RTS message size in Bytes
	 */
	protected static int RTS_MSG_SIZE = 16 + FCS; // macHeader + FCS

	/**
	 * ACK message size in Bytes
	 */
	protected static int ACK_MSG_SIZE = 10 + FCS; // macHeader + FCS

	/**
	 * Maximal Frame size of IEEE 802.11 packet, as specified in
	 * "802.11 Wireless Networks - Definitive Guide" from Matthew Gast
	 * (p.47,53).
	 */
	public static final long MAX_FRAME_SIZE = 2304;

	/**
	 * The {@link WifiTopologyView}, which will be informed about interferences.
	 */
	private WifiTopologyView topoView;

	/**
	 * Stores the corresponding IEEE802.11 standard, which is used by this MAC
	 * to communicate.
	 */
	private Standard_802_11 standard_802_11;

	/**
	 * Counts the retries of the ({@link Ieee80211AdHocMac#toSend}) actually
	 * message.
	 */
	private int retryCounter = 0;

	private DcfManager dcfManager;

	private AbstractRateManager rateManager;

	/**
	 * The txPower in watt
	 */
	private double txPowerW;

	/**
	 * The txPower in dBm
	 */
	private double txPowerDbm;

	private Random rand = Randoms.getRandom(Ieee80211AdHocMac.class);

	/**
	 * The minimum size of the message (with MAC-Header and FCS), at should be
	 * used RTS/CTS for the unicast-message.
	 */
	protected long rtsCtsThreshold;

	/**
	 * The channel number which is used.
	 */
	private int channel;

	private final static IEEE80211DummyACKMessage MSG_DUMMY_ACK = new IEEE80211DummyACKMessage();

	private final static IEEE80211DummyCTSMessage MSG_DUMMY_CTS = new IEEE80211DummyCTSMessage();

	private final static IEEE80211DummyRTSMessage MSG_DUMMY_RTS = new IEEE80211DummyRTSMessage();

	/**
	 * Contains the last acknowledged transmissions. Needed for Broadcasts, to
	 * identify multiple calls.
	 * <p>
	 * The maximal size of this cache is 10.
	 */
	private Map<MacEventInformation, Void> ackCache = new LinkedHashMap<MacEventInformation, Void>(
			100, 0.75f, true) {
		private final static int MAX_SIZE = 10;

		protected boolean removeEldestEntry(
				Map.Entry<MacEventInformation, Void> eldest) {
			return this.size() > MAX_SIZE;
		}
	};

	// don't change this. WIFI_PREAMBLE_SHORT is not practicable for long
	// ranges.
	private WifiPreamble preamble = WifiPreamble.WIFI_PREAMBLE_LONG;

	/**
	 * The type of the Rate Manager.
	 */
	private RateManagerTypes rateManagerType;
	
	/**
	 * The message, which is actually transfered.
	 */
	private QueueEntry toSend = null;

	/**
	 * The actually operation, which tries to get a send slot. It is the timer
	 * for the BackoffTimer, but because the backOffTimer can be extended, the
	 * end time is not fix! So this Operation tries to get a slot to send.
	 * <p>
	 * If no operation is running, this field should be <code>null</code>. So we
	 * know, that a new operation can be started.
	 */
	protected GetSendSlotOperation op = null;
	
	/**
	 * Creates the Ieee802.11 AdHoc MAC. Sets the given values. The class is not
	 * ready for using. After this should be called the initialize method, to
	 * create the missing components.
	 * 
	 * 
	 */
	public Ieee80211AdHocMac(SimHost host, MacAddress ownMacAddress,
			PhyType phy, Standard_802_11 standard, int maxQueueLength,
			long maxTimeInQueue, int maxRetransmissions, BandwidthImpl bandwidth,
			RateManagerTypes rateManagerType) {
		super(host, ownMacAddress, phy, maxQueueLength, maxTimeInQueue,
				maxRetransmissions, bandwidth);
		this.dcfManager = new DcfManager(this, standard);
		this.standard_802_11 = standard;
		this.rateManagerType = rateManagerType;
	}

	@Override
	public void initialize() throws ConfigurationException {
		super.initialize();

		// initialize the LiveMonitoring.
		if (!_analyzersInitialized) {
			LiveMonitoring
					.addProgressValueIfNotThere(new WifiUnicastProgress());
			LiveMonitoring
					.addProgressValueIfNotThere(new WifiBroadcastProgress());
			_analyzersInitialized = true;
		}

		// TODO: check for WifiTopologyView, otherwise throw
		// ConfigurationExcpetion
		topoView = (WifiTopologyView) getTopologyView();
		this.rateManager = AbstractRateManager.createRateManager(this,
				rateManagerType, standard_802_11);
		if (this.rateManager == null) {
			throw new ConfigurationException("The RateManager is not set!");
		}
		// TODO check configuration...
		// is the size of the netlayer acceptable?
	}

	@Override
	protected void handleNewQueueEntry() {
		if (this.toSend == null) {
			sendNextMessage();
		}
	}

	/**
	 * Take the first element from the Queue and starts the sending procedure.
	 * The message, which is to send, will be stored in
	 * {@link Ieee80211AdHocMac#toSend}. Additionally the retryCounter will be
	 * reseted to 0.
	 */
	protected void sendNextMessage() {
		toSend = getQueueHead();
		retryCounter = 0;
		if (toSend != null) {
			sendMessage();
		}
	}

	/**
	 * Start the procedure of the sending of the message in
	 * {@link Ieee80211MacMessage#toSend}.<br>
	 * The message will be insert in a new MAC-Message and then added to an
	 * event info. If a link is not available, the message will be aborted,
	 * otherwise it will be tried to get a SendSlot to transfer the message.
	 */
	protected void sendMessage() {
		if (toSend != null) {
			/*
			 * Here, we must scrape out the message from the netlayer, because
			 * the surrounding linklayer message does not contain all the
			 * information, which are required for the IEEE80211MacMessage.
			 */
			LinkLayerMessage dlm = toSend.getMessage();
			Ieee80211MacMessage msg = new Ieee80211MacMessage(dlm.getSender(),
					dlm.getReceiver(), dlm.getPayload());
			WifiMacEventInformation eventInfo = new WifiMacEventInformation(
					msg, this.getMacAddress(), toSend.getReceiver(),
					Time.getCurrentTime() - toSend.getTimeEntered());

			if (msg.getSize() >= MAX_FRAME_SIZE) {
				Monitor.log(
						Ieee80211AdHocMac.class,
						Level.WARN,
						"Try to send a Frame, which is to big ("
						+ msg.getSize()
						+ "Byte)! Please check the fragementation size in the Net-Layer. The message will be still send, with the side-effects (for example, higher Bandwidth)");
			}

			if (eventInfo.isBroadcast()) {
				tryToGetSendSlot(eventInfo);
			} else {
				// check for link connected
				Link link = topoView.getLinkBetween(eventInfo.getSender(),
						eventInfo.getReceiver());
				if (link.isConnected()) {
					tryToGetSendSlot(eventInfo);
				} else {
					messageDropped(DropReason.NO_LINK, msg);
					sendNextMessage();
				}
			}
		} else {
			throw new AssertionError(
					"sendMessage was called, but no Message exists");
		}
	}

	/**
	 * Tries to get a send slot. For that, it starts the BackoffTimer in the
	 * {@link DcfManager} and then must be scheduled to the end of the
	 * BackoffTimer.
	 * 
	 * @param eventInfo
	 *            The {@link MacEventInformation} with the containing
	 *            information to the message.
	 */
	private void tryToGetSendSlot(MacEventInformation eventInfo) {
		dcfManager.resetBackoffTimer();
		dcfManager.startBackoffTimer();
		long tryTime = dcfManager.getBackoffTimeEnd();
		if (op == null) {
			op = new GetSendSlotOperation(this, eventInfo);
			op.scheduleWithDelay(tryTime - Time.getCurrentTime());
		} else {
			// easy policy to check an failure. If two or more operation
			// running, then is op not null!
			throw new AssertionError(
					"Two or more GetSendSlotOperation are running! This should be not happen, because a station cannot send two or more Frames on the same time!");
		}
	}

	/**
	 * Executes the sending of the message as a unicast message with RTS/CTS as
	 * prequel. The RTS and CTS Message will be not send as a real message
	 * through the simulator. The RTS and CTS Message will be only simulated as
	 * interferences. It has be shown, that this is important to simulate this
	 * as interferences. Otherwise is the drop rate of other messages low.<br>
	 * Additionally, the DCF-Manager of the other MACs in the neighborhood will
	 * be manipulated.<br>
	 * The energy component will be informed about the sending and receiving.
	 * <p>
	 * If the link is not more connected, then will be dropped the message and a
	 * new Message will be started. If the receiver MAC is not in
	 * {@link WifiState#IDLE}, then we simulate only the RTS-Message.
	 * 
	 * @param eventInfo
	 *            The message which is to transfer.
	 */
	protected void mySendUnicast(WifiMacEventInformation eventInfo) {
		// Check online state over all possibilities to ensure that the device
		// is really online
		assert isOnline();
		Link link = topoView.getLinkBetween(eventInfo.getSender(),
				eventInfo.getReceiver());
		Ieee80211AdHocMac recMac = (Ieee80211AdHocMac) topoView
				.getMac(eventInfo.getReceiver());
		// Determine if there is a link for a unicast transmission between the
		// sender and the receiver
		if (link.isConnected()) {
			long current = Time.getCurrentTime();
			WifiState recState = recMac.getWifiState();
			// Check the state of the receiver and - depending on its state -
			// start the data transmission or only the transmission of RTS and
			// CTS
			if (recState == WifiState.IDLE && recMac.isOnline()) {
				// determine WifiMode for RTS, CTS, ACK
				WifiMode rtsMode = rateManager.getRtsMode(recMac
						.getMacAddress());
				WifiMode ctsMode = rateManager.getCtsMode(rtsMode);
				rateManager.reportRtsOk(recMac.getMacAddress(), rateManager
						.calculateActuallySNR(recMac.getPosition(),
								this.getPosition(), ctsMode, txPowerDbm),
						ctsMode);
				WifiMode dataMode = rateManager.getUnicastDataMode(recMac
						.getMacAddress());
				WifiMode ackMode = rateManager.getAckMode(dataMode);

				long rtsDuration = WifiPhy.calculateTxDuration(RTS_MSG_SIZE,
						rtsMode, preamble);
				long ctsDuration = WifiPhy.calculateTxDuration(CTS_MSG_SIZE,
						ctsMode, preamble);
				long dataDuration = WifiPhy.calculateTxDuration((int) eventInfo
						.getMessage().getSize(), dataMode, preamble);

				long ackDuration = WifiPhy.calculateTxDuration(ACK_MSG_SIZE,
						ackMode, preamble);

				eventInfo.setTransmissionDuration(dataDuration);
				eventInfo.setAckDuration(ackDuration);
				eventInfo.setAckMode(ackMode);
				eventInfo.setMode(dataMode);

				long startRts = current;
				long startCts = startRts + rtsDuration + getSifs();
				long startData = startCts + ctsDuration + getSifs();
				
				long duration = rtsDuration + getSifs() + ctsDuration
						+ getSifs() + dataDuration + getSifs() + ackDuration;

				// update DCF-Managers
				this.getDcfManager().notifyTxNow(duration);
				recMac.getDcfManager().notifyRxNow(duration);

				// Sender Neighbors only with CTS Timeout. If no CTS is
				// received, then will be stopped the NAV-Timer.
				// This is the update for the received RTS-Message
				updateNeighborsNavTimer(
						getTopologyView().getNeighbors(eventInfo.getSender()),
						getDcfManager().getCtsTimeout());
				// this the CTS-NAV update.
				updateNeighborsNavTimer(
						getTopologyView().getNeighbors(eventInfo.getReceiver()),
						duration);

				// Inform InterferenceHelper about Data Transfer
				topoView.addTransfer(startData, startData + dataDuration,
						this.getPosition(), this.getTxPowerDdm(),
						eventInfo.getMessage(), dataMode, preamble,
						this.getHost(), getMacAddress());
				topoView.addInterference(startRts, current + rtsDuration,
						this.getPosition(), this.getTxPowerDdm(), rtsMode,
						this.getMacAddress());
				topoView.addInterference(startCts, startCts + ctsDuration,
						recMac.getPosition(), recMac.getTxPowerDdm(), ctsMode,
						recMac.getMacAddress());

				/*
				 * This case represents a complete transmission of data
				 * including (i) the sending of a RTS, (ii) the receiving of a
				 * CTS, (iii) the sending and receiving of CTS, (iv) the
				 * transmission of data, and (v) the receiving of the ack.
				 */
				
				// (i) send the RTS
				this.getEnergyComponent().send(rtsDuration, MSG_DUMMY_RTS,
						false);
				_linkMsgEvent(MSG_DUMMY_RTS, getHost(), Reason.SEND);
				
				// (ii) Iterate over all listening devices in the communication
				// range, which receive the RTS
				for (MacAddress address : getTopologyView().getNeighbors(
						eventInfo.getSender())) {
					Ieee80211AdHocMac rtsRecMac = (Ieee80211AdHocMac) getTopologyView()
							.getMac(address);
					if (rtsRecMac.isOnline()
							&& (rtsRecMac.getWifiState() == WifiState.IDLE)) {
						rtsRecMac.getEnergyComponent().receive(rtsDuration,
								MSG_DUMMY_RTS, false, true);
						_linkMsgEvent(MSG_DUMMY_RTS, rtsRecMac.getHost(),
								Reason.RECEIVE);
					}
				}
				// (iiia) The intended receiver sends the CTS
				recMac.getEnergyComponent().send(ctsDuration, MSG_DUMMY_CTS,
						false);
				_linkMsgEvent(MSG_DUMMY_CTS, recMac.getHost(), Reason.SEND);
				
				// (iiib) The sender receives the CTS
				this.getEnergyComponent().receive(ctsDuration, MSG_DUMMY_CTS,
						false, true);
				_linkMsgEvent(MSG_DUMMY_CTS, getHost(), Reason.RECEIVE);
				
				// (iv) The transmission of the actual data
				this.getEnergyComponent().send(dataDuration,
						eventInfo.getMessage(), false);
				// schedule receiving of the message
				scheduleReceive(recMac, eventInfo, (startData + dataDuration)
						- current, false);

				_linkMsgEvent(eventInfo.getMessage(), getHost(),
						Reason.SEND);
				currentBandwidth.outgoingTransmission(eventInfo.getMessage()
						.getSize());

				_wifiUnicastSent++;
			} else {
				// Start the NAV-Timer. If no CTS is received, then will be
				// stopped after CTS-Timeout.
				updateNeighborsNavTimer(
						getTopologyView().getNeighbors(eventInfo.getSender()),
						getDcfManager().getCtsTimeout());

				rateManager.reportRtsFailed(recMac.getMacAddress());

				/*
				 *  Energy for (i) sending and (ii) receiving RTS
				 */
				
				// (i) Send the RTS message
				WifiMode rtsMode = rateManager.getRtsMode(recMac
						.getMacAddress());
				long rtsDuration = WifiPhy.calculateTxDuration(RTS_MSG_SIZE,
						rtsMode, preamble);
				this.getEnergyComponent().send(rtsDuration, MSG_DUMMY_RTS,
						false);
				_linkMsgEvent(MSG_DUMMY_RTS, getHost(), Reason.SEND);
				
				// (ii) Iterate over all listening devices in the communication
				// range, which receive the RTS message
				for (MacAddress address : getTopologyView().getNeighbors(
						eventInfo.getSender())) {
					Ieee80211AdHocMac rtsRecMac = (Ieee80211AdHocMac) getTopologyView()
							.getMac(address);
					if (rtsRecMac.isOnline()
							&& (rtsRecMac.getWifiState() == WifiState.IDLE)) {
						rtsRecMac.getEnergyComponent().receive(rtsDuration,
								MSG_DUMMY_RTS, false, true);
						_linkMsgEvent(MSG_DUMMY_RTS, rtsRecMac.getHost(),
								Reason.RECEIVE);
					}
				}

				topoView.addInterference(current, current + rtsDuration,
						this.getPosition(), this.getTxPowerDdm(), rtsMode,
						this.getMacAddress());

				// try again after CTS Timeout
				dcfManager.notifiyCTSTimeout();
				unsuccessfulTransmitted(eventInfo);
			}
		} else {
			messageDropped(DropReason.NO_LINK, eventInfo.getMessage());
			sendNextMessage();
		}
	}

	/**
	 * Updates for all neighbors the NAV-Timer.
	 * 
	 * @param neighbors
	 *            A list of MacAddresses, which should be updated.
	 * @param duration
	 *            The duration of the NAV.
	 */
	private void updateNeighborsNavTimer(List<MacAddress> neighbors,
			long duration) {
		for (MacAddress address : neighbors) {
			// If in Idle, NAV or CCA, it is possible to update the
			// NAV Timer.
			Ieee80211AdHocMac navMac = (Ieee80211AdHocMac) getTopologyView()
					.getMac(address);
			WifiState navMacState = navMac.getWifiState();
			if (navMacState != WifiState.TX && navMacState != WifiState.RX) {
				navMac.getDcfManager().notifyNavNow(duration);
			}
		}
	}

	/**
	 * 
	 * Executes the sending of the message as a unicast message without RTS/CTS
	 * as prequel. Additionally, the DCF-Manager of the other MACs in the
	 * neighborhood will be manipulated.<br>
	 * The energy component will be informed about the sending and receiving.
	 * <p>
	 * If the link is not more connected, then will be dropped the message and a
	 * new Message will be started.
	 * 
	 * @param eventInfo
	 *            The message, which should be send.
	 */
	protected void mySendUnicastWithoutRtsCts(WifiMacEventInformation eventInfo) {
		// Check online state over all possibilities to ensure that the device
		// is really online
		assert isOnline();
		Link link = topoView.getLinkBetween(eventInfo.getSender(),
				eventInfo.getReceiver());
		Ieee80211AdHocMac recMac = (Ieee80211AdHocMac) topoView
				.getMac(eventInfo.getReceiver());

		if (link.isConnected()) {

			WifiState recMacState = recMac.getWifiState();

			WifiMode dataMode = rateManager.getUnicastDataMode(recMac
					.getMacAddress());
			WifiMode ackMode = rateManager.getAckMode(dataMode);
			long current = Time.getCurrentTime();
			long dataDuration = WifiPhy.calculateTxDuration((int) eventInfo
					.getMessage().getSize(), dataMode, preamble);
			long ackDuration = WifiPhy.calculateTxDuration(ACK_MSG_SIZE,
					ackMode, preamble);

			eventInfo.setTransmissionDuration(dataDuration);
			eventInfo.setAckDuration(ackDuration);
			eventInfo.setAckMode(ackMode);
			eventInfo.setMode(dataMode);

			long startData = current;
			long duration = dataDuration + getSifs() + ackDuration;

			// Inform InterferenceHelper about Data Transfer
			topoView.addTransfer(startData, startData + dataDuration,
					this.getPosition(), getTxPowerDdm(),
					eventInfo.getMessage(), dataMode, preamble, this.getHost(),
					getMacAddress());

			// Energy for sending Data.
			this.getEnergyComponent().send(dataDuration,
					eventInfo.getMessage(), false);
					
			_linkMsgEvent(eventInfo.getMessage(), getHost(), Reason.SEND);
			currentBandwidth.outgoingTransmission(eventInfo.getMessage()
					.getSize());

			this.getDcfManager().notifyTxNow(duration);

			updateNeighborsNavTimer(
					getTopologyView().getNeighbors(eventInfo.getSender()),
					duration);

			// schedule receive
			if (recMacState != WifiState.TX && recMacState != WifiState.RX && recMac.isOnline()) {
				recMac.getDcfManager().notifyRxNow(duration);
				scheduleReceive(recMac, eventInfo, (startData + dataDuration)
						- current, false);
			} else {
				// schedule with drop, because receiver is not listen.
				scheduleReceive(recMac, eventInfo, (startData + dataDuration)
						- current, true);
			}

			_wifiUnicastSent++;

		} else {
			messageDropped(DropReason.NO_LINK, eventInfo.getMessage());
			sendNextMessage();
		}

	}

	/**
	 * Gets a copy of the real position of this host.
	 * 
	 * Clone <strong>ONLY</strong> here.
	 * 
	 * @return The position of this host.
	 */
	private PositionVector getPosition() {
		return this.getHost().getTopologyComponent().getRealPosition().clone();
	}

	/**
	 * Gets the SIFS (Short Interframe Spacing) of the {@link DcfManager} back.
	 * 
	 * @return The SIFS of the {@link DcfManager}.
	 */
	private long getSifs() {
		return dcfManager.getSifs();
	}

	/**
	 * Executes the sending of the message as a broadcast message.<br>
	 * All neighbors will be informed about the receiving of this message.
	 * Neighbors, which are in {@link WifiState#TX} or {@link WifiState#RX}
	 * receive the message as a drop (The neighbor will have a higher SNR during
	 * the sending of this broadcast).Additionally, the DCF-Manager of the other
	 * MACs in the neighborhood will be manipulated.<br>
	 * The energy component will be informed about the sending and receiving.
	 * 
	 * <p>
	 * After this sending, this MAC will be informed about a successful
	 * transmit, because a new sending procedure with a new message from the
	 * queue must be started.
	 * 
	 * @param eventInfo
	 *            The message which should be send as a broadcast.
	 */
	protected void mySendBroadcast(WifiMacEventInformation eventInfo) {
		// Check online state over all possibilities to ensure that the device
		// is really online
		assert isOnline();
		WifiMode dataMode = rateManager.getBroadcastDataMode();

		long duration = WifiPhy.calculateTxDuration((int) eventInfo
				.getMessage().getSize(), dataMode, preamble);

		eventInfo.setTransmissionDuration(duration);
		eventInfo.setAckDuration(0);
		eventInfo.setAckMode(null);
		eventInfo.setMode(dataMode);

		long startData = Time.getCurrentTime();
		long endData = startData + duration;

		// update DCF-Managers
		this.getDcfManager().notifyTxNow(duration);
		
		/*
		 * TODO MSt: Disabled during merge (ugly!)
		 * Michael Stein: Here, I'm introducing an ugly hack. Problem is that
		 * Topology Control changes the topology. Somehow, the nodes always need
		 * to be able to still extract the orginial topology. To allow for this,
		 * the messages sent by DistributedTopologyMonitoringComponent do not
		 * broadcasted only to the current neighbors but to all neighbors
		 */
		//Message msgPayload = eventInfo.getMessage().getPayload();
		Collection<MacAddress> neighbors = null;
		/*if ((msgPayload instanceof ViewMessage
				|| msgPayload instanceof EdgeOperationMessage)
				&& getTopologyView() instanceof LogicalWifiTopologyView) {
			neighbors = ((LogicalWifiTopologyView) getTopologyView())
					.getPhysicalNeighbors(eventInfo.getSender());
		} else { */
			neighbors = getTopologyView().getNeighbors(eventInfo.getSender());
		/* }*/
		
		for (MacAddress address : neighbors) {
			Ieee80211AdHocMac recMac = (Ieee80211AdHocMac) getTopologyView()
					.getMac(address);
			// If it is not in RX, TX and online then it is possible to receive
			// this message. In case NAV it is possible that exist more
			// interferences and the Messages will be dropped.
			WifiState recMacState = recMac.getWifiState();
			if (recMacState != WifiState.TX && recMacState != WifiState.RX && recMac.isOnline()) {
				recMac.getDcfManager().notifyRxNow(duration);
				// schedule receive
				scheduleReceive(recMac, eventInfo, duration, false);
			} else {
				// schedule with drop, because receiver is not listen.
				scheduleReceive(recMac, eventInfo, duration, true);
			}
		}

		// Inform InterferenceHelper about Data Transfer
		topoView.addTransfer(startData, endData, this.getPosition(),
				getTxPowerDdm(), eventInfo.getMessage(), dataMode, preamble,
				this.getHost(), getMacAddress());
		getEnergyComponent().send(duration, eventInfo.getMessage(), true);
		this.successfulTransmitted(eventInfo);

		_linkMsgEvent(eventInfo.getMessage(), getHost(),
				Reason.SEND);
		currentBandwidth.outgoingTransmission(eventInfo.getMessage().getSize());

		_wifiBroadcastSent++;
		_wifiBroadcastDataSent += eventInfo.getMessage().getSize();
	}

	/**
	 * Gets the DCF Manager.
	 * 
	 * @return The {@link DcfManager} of this MAC.
	 */
	public DcfManager getDcfManager() {
		return dcfManager;
	}

	@Override
	protected void handleReceivedMessage(MacEventInformation eventInfo) {
		// Check online state over all possibilities to ensure that the device
		// is really online
		assert isOnline();
		Message msg = eventInfo.getMessage();
		WifiMacEventInformation wifiEventInfo = (WifiMacEventInformation) eventInfo;
		Ieee80211AdHocMac senderMac = (Ieee80211AdHocMac) topoView
				.getMac(eventInfo.getSender());
		boolean successful;

		if (checkCollision()) {
			// we send and receive to the same time. But we cannot receive the
			// message, because the tx-power is to strong and will be destroy
			// the rx-signal.
			// So we know, that the receiving is unsuccessful!
			successful = false;
			this.dcfManager.increaseCw();
			// if the sender is in collision, then will be dropped the
			// receiving, but the sending is successful. He has no chance to
			// detect the rx-signal.
		} else {
			double per = topoView.calculatePer(msg, getPosition());
			successful = rand.nextDouble() > per;

			// Energy for receive
			this.getEnergyComponent().receive(
					wifiEventInfo.getTransmissionDuration(), msg,
					eventInfo.isBroadcast(), true);
		}

		if (successful) {
			notifyLinkLayer(new DefaultLinkMessageEvent(
					(LinkLayerMessage) eventInfo.getMessage(),
					getPhyType(), eventInfo.getSender(),
					eventInfo.isBroadcast()));
			senderMac.successfulTransmitted(eventInfo);

			// inform rateManager about the snr of the received packet
			double snr = rateManager.calculateActuallySNR(
					senderMac.getPosition(), this.getPosition(),
					wifiEventInfo.getMode(), txPowerDbm);
			rateManager.reportRxOk(senderMac.getMacAddress(), snr,
					wifiEventInfo.getMode());

			// Energy for Ack and add the ACK message as interference.
			if (!eventInfo.isBroadcast()) {
				long current = Time.getCurrentTime();
				long ackDuration = wifiEventInfo.getAckDuration();
				long startInterference = current + getSifs();
				topoView.addInterference(startInterference, startInterference
						+ ackDuration, this.getPosition(),
						this.getTxPowerDdm(), wifiEventInfo.getAckMode(),
						this.getMacAddress());
				
				// Simulate the sending of an ACK-message
				this.getEnergyComponent().send(ackDuration, MSG_DUMMY_ACK,
						false);
				_linkMsgEvent(MSG_DUMMY_ACK, getHost(), Reason.SEND);
				// Simulate the reception of an ACK-message
				if (senderMac.isOnline()
						&& (senderMac.getWifiState() == WifiState.IDLE)) {
					senderMac.getEnergyComponent().receive(ackDuration,
							MSG_DUMMY_ACK, false, true);
					_linkMsgEvent(MSG_DUMMY_ACK, senderMac.getHost(),
							Reason.RECEIVE);
				}
			}

		} else {
			// inform dcfManager for drop
			this.dcfManager.notifyRxDrop();
			senderMac.unsuccessfulTransmitted(eventInfo);
		}
	}

	/**
	 * Checks the start time of RX and TX-States of the {@link DcfManager}. If
	 * both times the same, then we know that we have a collision.<br>
	 * This works only, if no propagation delay is used!
	 * 
	 * @return <code>true</code> if the RX and TX StartTime is the same,
	 *         otherwise <code>false</code>.
	 */
	private boolean checkCollision() {
		long txStartTime = this.getDcfManager().getTxStartTime();
		long rxStartTime = this.getDcfManager().getRxStartTime();

		return txStartTime == rxStartTime;
	}

	@Override
	protected void handleEvent(Object data, int type) {
		if ((type == MESSAGE_RECEIVED && !isOnline())
				|| type == MESSAGE_DROPPED) {
			if (data instanceof MacEventInformation) {
				MacEventInformation eventInfo = (MacEventInformation) data;
				Ieee80211AdHocMac senderMac = (Ieee80211AdHocMac) topoView
						.getMac(eventInfo.getSender());
				senderMac.unsuccessfulTransmitted(eventInfo);
			}
		}
	}

	/**
	 * If a message is successful transmitted, then should be called this method
	 * of the sender MAC. In the broader sense, it is for the handling of the
	 * ACK-Message<br>
	 * The Contention Window of the DCF-Manager will be reseted. The next
	 * message will be triggered and the rate manager will be informed about the
	 * correct receiving of the message.
	 * 
	 * @param eventInfo
	 *            The message which is successfully transmitted with all meta
	 *            information.
	 */
	protected void successfulTransmitted(MacEventInformation eventInfo) {
		// for progress display
		if (eventInfo.isBroadcast() && ackCache.containsKey(eventInfo)) {
			_wifiBroadcastRcvd++;
			_wifiBroadcastDataRcvd += eventInfo.getMessage().getSize();
		}
		if (!eventInfo.isBroadcast() && !ackCache.containsKey(eventInfo)) {
			_wifiUnicastRcvd++;
		}

		// reset the CW, because the message was successful transmitted
		this.dcfManager.resetCw();

		// this check is used to handle broadcasts, because it can called
		// several times...
		if (!ackCache.containsKey(eventInfo)) {
			sendNextMessage();
			reportDataOk(eventInfo);
			ackCache.put(eventInfo, null);
		}
	}

	/**
	 * Inform the RateManager about the successful transmission of the data.<br>
	 * It will be only informed, if the message was not a broadcast.
	 * 
	 * @param eventInfo
	 *            The message which is successfully transmitted with all meta
	 *            information.
	 */
	private void reportDataOk(MacEventInformation eventInfo) {
		if (!eventInfo.isBroadcast()) {
			WifiMacEventInformation eInfo = (WifiMacEventInformation) eventInfo;

			// startPosition is from receiver (because ack come from this!)
			Location startPosition = ((Ieee80211AdHocMac) topoView
					.getMac(eventInfo.getReceiver())).getPosition();
			Location targetPosition = this.getPosition();
			double ackSnr = rateManager.calculateActuallySNR(startPosition,
					targetPosition, eInfo.getAckMode(), txPowerDbm);
			rateManager.reportDataOk(eventInfo.getReceiver(), ackSnr,
					eInfo.getAckMode());
		}
	}

	/**
	 * If a message is unsuccessful transmitted, then should be called this
	 * method of the sender MAC. In the broader sense, it is for the handling
	 * for the missing ACK-Message<br>
	 * The Contention Window of the DCF-Manager will be increased. The
	 * retryCounter will be increased and if the maximal retransmission is not
	 * reached, it will be start to send the same message again. Additionally
	 * the rate manager will be informed about the failing of the data
	 * transmission.
	 * 
	 * @param eventInfo
	 *            The message which is unsuccessfully transmitted with all meta
	 *            information.
	 */
	protected void unsuccessfulTransmitted(MacEventInformation eventInfo) {
		if (!ackCache.containsKey(eventInfo)) {
			if (!eventInfo.isBroadcast()) {
				this.retryCounter++;
				this.dcfManager.increaseCw();
				if (retryCounter <= getMaxRetransmissions()) {
					this.sendMessage();
					rateManager.reportDataFailed(eventInfo.getReceiver());
					_wifiResent++;
				} else {
					this.messageDropped(DropReason.LINK_DROP,
							eventInfo.getMessage());
					this.sendNextMessage();
					rateManager.reportFinalDataFailed(eventInfo.getReceiver());
					_wifiUnicastDropped++;
				}
			} else {
				// for broadcasts, to start a new send message.
				// should be never called, because mySendBroadcast call
				// successfulTrasmitted.
				this.sendNextMessage();
			}
			// to handle broadcasts, because multiple calls of successful or
			// unsuccessful is possible
			ackCache.put(eventInfo, null);
		}
		if (eventInfo.isBroadcast()) {
			_wifiBroadcastDropped++;
		}
	}

	/**
	 * Gets the actually {@link WifiState} of this MAC.
	 * 
	 * @return Returns the actually {@link WifiState} from the
	 *         {@link DcfManager}.
	 */
	public WifiState getWifiState() {
		return this.dcfManager.getWifiState();
	}

	/**
	 * Gets the TX Power in watt.
	 * 
	 * @return The tx Power in watt.
	 */
	public double getTxPowerW() {
		return txPowerW;
	}

	/**
	 * Gets the TX Power in dBm.
	 * 
	 * @return The tx Power in dBm.
	 */
	public double getTxPowerDdm() {
		return txPowerDbm;
	}

	/**
	 * Sets the TX Power in dBm and in watt.
	 * 
	 * @param txPowerDbm
	 *            The TX power in dBm.
	 */
	public void setTxPowerDbm(double txPowerDbm) {
		this.txPowerDbm = txPowerDbm;
		this.txPowerW = PropagationLossModel.dbmToW(txPowerDbm);
	}

	/**
	 * Sets the RTS/CTS Threshold.
	 * 
	 * @param rtsCtsThreshold
	 *            The new RTS/CTS Threshold.
	 */
	public void setRtsCtsThreshold(long rtsCtsThreshold) {
		this.rtsCtsThreshold = rtsCtsThreshold;
	}

	/**
	 * Inform the DCF-Manager about a higher noise on the medium or rather, the
	 * channel is not clear.
	 * 
	 * @param duration
	 *            The duration of this noise.
	 */
	public void notifyCarrierSense(long duration) {
		this.dcfManager.notifyCcaNow(duration);
	}

	/**
	 * Gets the unerlaying {@link WifiTopologyView}.
	 * 
	 * @return The {@link WifiTopologyView}.
	 */
	public WifiTopologyView getWifiTopologyView() {
		return topoView;
	}

	/**
	 * The used {@link Standard_802_11} from this MAC.
	 * 
	 */
	public Standard_802_11 getStandard_802_11() {
		return standard_802_11;
	}

	/**
	 * Sets the channel which should be used. This should be called before the
	 * MAC is added to the {@link TopologyView}. Changes after this will be
	 * ignored.
	 * 
	 * @param channel
	 *            The channel number
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * Gets the used channel.
	 * 
	 * @return The used channelnumber.
	 */
	public int getChannel() {
		return this.channel;
	}

	/**
	 * Gets the used frequency in Hz. It is dependent of the channel.
	 * 
	 * @return The used frequency.
	 */
	public long getFrequency() {
		long basicFrequency = standard_802_11.getBasicFrequency();
		long channelBandwidth = 5000000l; // 5MHz;
		long frequency = basicFrequency + this.channel * channelBandwidth;
		return frequency;
	}

	/**
	 * This Operation tries to get a Send Slot. It is started, after the
	 * BackoffTimer is started. If the event for the execution is fired, then
	 * will be checked if the BackOffTimer is 0. If it 0, then will be started
	 * the transfer of the message. If it is not 0, then will be created a new
	 * SendSlotOperation and new scheduled to the time of BackOffTimerEnd.
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 28.02.2013
	 */
	private class GetSendSlotOperation extends
			AbstractOperation<Ieee80211AdHocMac, Void> {
		private MacEventInformation eventInfo;

		protected GetSendSlotOperation(Ieee80211AdHocMac mac,
				MacEventInformation eventInfo) {
			super(mac);
			this.eventInfo = eventInfo;
		}

		@Override
		protected void execute() {

			getComponent().op = null;
			if (!getComponent().isOnline()) {
				Monitor.log(
						Ieee80211AdHocMac.class,
						Level.WARN,
						Time.getFormattedTime()
								+ " node "
								+ eventInfo.getSender()
								+ " wanted to get a slot to send but was already offline.");
				return;
			}
			boolean ignoreWifiState = getComponent().getDcfManager()
					.isIgnoreWifiState();

			long backOffTimeEnd = getComponent().getDcfManager()
					.getBackoffTimeEnd();
			long current = Time.getCurrentTime();

			if (backOffTimeEnd == current) {

				// Theory: MAC must be in an Idle State, if the backOffTimeEnd
				// == current!
				// The flag ignoreWifiState is useful to create collisions,
				// because the backOffTime is 0 for multiple MACs, but the
				// wifiState was changed from an other MAC.
				if (ignoreWifiState
						|| getComponent().getWifiState() == WifiState.IDLE) {
					WifiMacEventInformation wifiEventInfo = (WifiMacEventInformation) eventInfo;
					if (eventInfo.isBroadcast()) {
						getComponent().mySendBroadcast(wifiEventInfo);
					} else {
						if ((eventInfo.getMessage().getSize() + getComponent().FCS) < getComponent().rtsCtsThreshold) {
							getComponent().mySendUnicastWithoutRtsCts(
									wifiEventInfo);
						} else {
							getComponent().mySendUnicast(wifiEventInfo);
						}
					}
				} else {
					/*
					 * FIXME is this a valid state?
					 */
					getComponent().sendMessage();
					throw new AssertionError(
							"Why is the Mac not in an Idle State? "
									+ getComponent().getWifiState()
									+ "   "
									+ (getComponent().getDcfManager()
											.getTimeToIdleState()));
				}

			} else {
				if (backOffTimeEnd < current) {
					throw new AssertionError(
							"How can the backOffTimeEnd be smaller than the current time? ");
				} else {
					op = new GetSendSlotOperation(getComponent(), eventInfo);
					op.scheduleWithDelay(backOffTimeEnd
							- Time.getCurrentTime());
				}
			}
			this.operationFinished(true);
		}

		@Override
		public Void getResult() {
			return null;
		}

	}

	/**
	 * The {@link ProgressValue} for the sending of unicast messages. It shows
	 * the number of sent, received, dropped and resent messages.
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 28.02.2013
	 */
	public class WifiUnicastProgress implements ProgressValue {

		@Override
		public String getName() {
			return "WiFi Unicast (Sent/Rcvd/Dropped/Resent)";
		}

		@Override
		public String getValue() {
			return String.format("WiFi Unicast (%10d/%10d/%10d/%10d)",
					_wifiUnicastSent, _wifiUnicastRcvd, _wifiUnicastDropped,
					_wifiResent);
		}
	}

	/**
	 * The {@link ProgressValue} for the sending of broadcasts messages. It
	 * shows the number of sent, received and dropped messages. Additionally it
	 * shows the traffic, which is generated of successfully transmitted
	 * broadcasts.
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 28.02.2013
	 */
	public class WifiBroadcastProgress implements ProgressValue {

		@Override
		public String getName() {
			return "WiFi Broadcast (Sent/Rcvd/Dropped); Traffic (Sent/Rcvd)";
		}

		@Override
		public String getValue() {
			return String.format(
					"WiFi Broadcast (%10d/%10d/%10d); Traffic (%5s/%5s)",
					_wifiBroadcastSent, _wifiBroadcastRcvd,
					_wifiBroadcastDropped,
					readableFileSize(_wifiBroadcastDataSent),
					readableFileSize(_wifiBroadcastDataRcvd));
		}
	}

	/**
	 * Helper to create readable file sizes up to TB.
	 * 
	 * @param size
	 *            The size in Byte.
	 * @return A human readable String for the size.
	 */
	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];
	}

	private static boolean _linkAnalyzerInitialized = false;

	private static LinklayerAnalyzer _linkAnalyzer = null;

	protected static void _linkMsgEvent(LinkLayerMessage msg, SimHost host,
			Reason reason) {
		if (!_linkAnalyzerInitialized) {
			try {
				_linkAnalyzer = Monitor.get(LinklayerAnalyzer.class);
			} catch (AnalyzerNotAvailableException e) {
				_linkAnalyzer = null;
			}
			_linkAnalyzerInitialized = true;
		}
		if (_linkAnalyzerInitialized && _linkAnalyzer != null) {
			_linkAnalyzer.linkMsgEvent(msg, host, reason);
		}
	}
}
