package de.tudarmstadt.maki.simonstrator.overlay.api.metric;

import java.io.Writer;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.overlay.api.IOverlayMessageAnalyzer;
import de.tudarmstadt.maki.simonstrator.overlay.api.OverlayMessage;
import de.tudarmstadt.maki.simonstrator.overlay.api.metric.MOverlayMessages.MessageMetricValue;

/**
 * Generic Metric-Implementation of the {@link IOverlayMessageAnalyzer},
 * providing size and count for given OverlayMessages.
 * 
 * @author Bjoern Richerzhagen
 * @version Jun 30, 2014
 */
public abstract class MOverlayMessages extends AbstractMetric<MessageMetricValue> implements IOverlayMessageAnalyzer {

	protected enum Reason {
		SEND, RECEIVE
	}

	private Reason reason;

	private final boolean perHost;

	/**
	 * Currently only supported for send.
	 */
	private NetInterfaceName netName;

	/**
	 * 
	 * @param name
	 *            name of the metric, global metrics will be prefixed with a "G"
	 * @param description
	 * @param unit
	 * @param reason
	 *            SEND, RECEIVE
	 * @param perHost
	 *            if false, this is a global metric
	 */
	public MOverlayMessages(String name, String description, MetricUnit unit, Reason reason, boolean perHost,
			String netName) {
		super(perHost ? name : "G" + name, description, unit);
		Monitor.registerAnalyzer(this);
		this.reason = reason;
		this.perHost = perHost;
		try {
			this.netName = NetInterfaceName.valueOf(netName.toUpperCase());
		} catch (Exception e) {
			this.netName = null;
		}
	}

	/**
	 * A metric value with a simple counter that can be incremented
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 14.08.2012
	 */
	public class MessageMetricValue
			implements de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue<Double> {

		private double value = 0;

		private NetInterface net;

		public MessageMetricValue(Host host) {
			if (host != null) {
				this.net = host.getNetworkComponent().getByName(netName);
			}
		}

		@Override
		public Double getValue() {
			return value;
		}

		@Override
		public boolean isValid() {
			if (net == null) {
				return value >= 0;
			} else {
				return net.isUp();
			}
		}

		public void incValue(double inc) {
			this.value += inc;
		}

	}

	@Override
	public void initialize(List<Host> hosts) {
		if (perHost) {
			for (Host host : hosts) {
				addHost(host, createMessageMetricValue(host));
			}
		} else {
			setOverallMetric(createMessageMetricValue(null));
		}
	}

	/**
	 * Allows extending metrics to add more complex metric values.
	 * 
	 * @param host
	 *            or null, if global
	 * @return
	 */
	protected MessageMetricValue createMessageMetricValue(Host host) {
		return new MessageMetricValue(host);
	}

	/**
	 * Called whenever a message is passed to the monitor that matches our
	 * filters.
	 * 
	 * @param mv
	 *            the MetricValue to update
	 * @param msg
	 *            the message
	 * @param host
	 *            the host or null, if overall metric
	 */
	protected abstract void update(MessageMetricValue mv, Message msg, Host host);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tudarmstadt.maki.simonstrator.overlay.api.IOverlayMessageAnalyzer#
	 * onSentOverlayMessage
	 * (de.tudarmstadt.maki.simonstrator.overlay.api.OverlayMessage,
	 * de.tudarmstadt.maki.simonstrator.api.Host)
	 */
	@Override
	public void onSentOverlayMessage(OverlayMessage msg, Host host, NetInterfaceName netName) {
		if (reason == Reason.SEND && (this.netName == null || netName == this.netName)) {
			if (perHost) {
				update(getPerHostMetric(host.getId()), msg, host);
			} else {
				update(getOverallMetric(), msg, null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tudarmstadt.maki.simonstrator.overlay.api.IOverlayMessageAnalyzer#
	 * onReceivedOverlayMessage
	 * (de.tudarmstadt.maki.simonstrator.overlay.api.OverlayMessage,
	 * de.tudarmstadt.maki.simonstrator.api.Host)
	 */
	@Override
	public void onReceivedOverlayMessage(OverlayMessage msg, Host host) {
		if (reason == Reason.RECEIVE) {
			if (perHost) {
				update(getPerHostMetric(host.getId()), msg, host);
			} else {
				update(getOverallMetric(), msg, null);
			}
		}
	}

	@Override
	public final void start() {
		// ignore, should use initialize instead.
	}

	@Override
	public final void stop(Writer output) {
		// ignore
	}

	/**
	 * Counter for a a given overlay Message type - specfied using the
	 * fully-qualifying class name. Support type hierarchies, i.e., you may
	 * specify a common base interface instead of each message class separately.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Oct 26, 2013
	 */
	public static class CountType extends MOverlayMessages {

		private Class<?> msgClass;

		@XMLConfigurableConstructor({ "reason", "perHost", "type", "net" })
		public CountType(String reason, boolean perHost, String type, String net) {
			super("Count" + type.substring(type.lastIndexOf(".") + 1) + reason + net,
					"Total count for " + reason + " of " + type.substring(type.lastIndexOf(".") + 1), MetricUnit.NONE,
					Reason.valueOf(reason.toUpperCase()), perHost, net);
			// Find class
			try {
				msgClass = Class.forName(type);
			} catch (ClassNotFoundException e) {
				throw new AssertionError();
			}
		}

		@Override
		protected void update(MessageMetricValue mv, Message msg, Host host) {
			if (msgClass == null) {
				return;
			}
			if (msgClass.isAssignableFrom(msg.getClass())) {
				mv.incValue(1);
			}
		}
	}

	/**
	 * Message size aggregator for a single overlay message TYPE, identified by
	 * the simple class name. There is a magic name "AllOverlayMessages", that
	 * lets you count all messages arriving at the overlay layer. Furthermore,
	 * the message name may be an array of multiple messages, concatenated with
	 * a ";"
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Oct 26, 2013
	 */
	public static class SizeType extends MOverlayMessages {

		private Class<?> msgClass;

		@XMLConfigurableConstructor({ "reason", "perHost", "type", "net" })
		public SizeType(String reason, boolean perHost, String type, String net) {
			super("Size" + type.substring(type.lastIndexOf(".") + 1) + reason + net,
					"Total size for " + reason + " of " + type.substring(type.lastIndexOf(".") + 1), MetricUnit.DATA,
					Reason.valueOf(reason.toUpperCase()), perHost, net);
			// Find class
			try {
				msgClass = Class.forName(type);
			} catch (ClassNotFoundException e) {
				throw new AssertionError(e.getStackTrace());
			}
		}

		@Override
		protected void update(MessageMetricValue mv, Message msg, Host host) {
			if (msgClass == null) {
				return;
			}
			if (msgClass.isAssignableFrom(msg.getClass())) {
				mv.incValue(msg.getSize());
			}
		}
	}

}
