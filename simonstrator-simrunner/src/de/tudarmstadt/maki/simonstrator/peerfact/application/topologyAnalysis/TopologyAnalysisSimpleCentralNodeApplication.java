package de.tudarmstadt.maki.simonstrator.peerfact.application.topologyAnalysis;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView.LogicalWiFiTopology;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;

public class TopologyAnalysisSimpleCentralNodeApplication implements HostComponent {

	private Host host;

	public TopologyAnalysisSimpleCentralNodeApplication(Host host) {
		this.host = host;
	}

	@Override
	public void initialize() {
		Monitor.log(getClass(), Level.INFO, "Initializing " + getClass().getSimpleName());
		AnalyzeWholeTopologyEvent op = new AnalyzeWholeTopologyEvent();
		op.scheduleWithDelay(op.getInterval());
	}

	@Override
	public void shutdown() {

	}

	@Override
	public Host getHost() {
		return this.host;
	}

	private class AnalyzeWholeTopologyEvent
			extends PeriodicOperation<TopologyAnalysisSimpleCentralNodeApplication, Void> {

		protected AnalyzeWholeTopologyEvent() {
			super(TopologyAnalysisSimpleCentralNodeApplication.this, null, 60 * Time.SECOND);
		}

		@Override
		protected void executeOnce() {

			Monitor.log(getClass(), Level.INFO, Simulator.getFormattedTime(Simulator.getCurrentTime())
					+ " Do central analysis " + getClass().getSimpleName());

			final Graph wifiTopology = GlobalOracle.getTopology(LogicalWiFiTopology.class,
					LogicalWifiTopologyView.getUDGTopologyID());
			// Old code - left here temporarily for documenting the transition
			// to the new API via LogicalWiFiTopology
			// final Graph wifiTopology =
			// GlobalOracle.getTopology(DefaultTopologyComponent.class,
			// DefaultTopologyComponent.getWifiUdgIdentifier());
			Monitor.log(getClass(), Level.INFO, "\t\t\tWiFi View: n=%d, m=%d", wifiTopology.getNodeCount(),
					wifiTopology.getEdgeCount());
			final Graph localView = GlobalOracle.getTopology(LogicalWiFiTopology.class,
					LogicalWifiTopologyView.getAdaptableTopologyID());

			// Old code - left here temporarily for documenting the
			// transition
			// to the new API via LogicalWiFiTopology
			// DefaultTopologyComponent defaultTopologyComponent = getHost()
			// .getComponent(DefaultTopologyComponent.class);
			// Graph localView =
			// defaultTopologyComponent.getLocalView(DefaultTopologyComponent.getWifiIdentifier());
			Monitor.log(getClass(), Level.INFO, "\t\t\tLocal View: n=%d, m=%d", localView.getNodeCount(),
					localView.getEdgeCount());
		}

		@Override
		public Void getResult() {
			return null;
		}

	}
}
