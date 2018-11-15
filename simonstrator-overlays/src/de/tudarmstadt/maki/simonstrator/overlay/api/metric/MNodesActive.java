package de.tudarmstadt.maki.simonstrator.overlay.api.metric;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.LifecycleComponent;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.overlay.api.metric.MNodesActive.MNodesActiveValue;

/**
 * Counts the number of nodes that are active at a given point in time.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class MNodesActive extends AbstractMetric<MNodesActiveValue> {

	private Class<? extends LifecycleComponent> nodeType;

	@SuppressWarnings("unchecked")
	@XMLConfigurableConstructor({ "type" })
	public MNodesActive(String type) {
		super("MNodesActive" + type.substring(type.lastIndexOf(".") + 1),
				"Number of active nodes of " + type.substring(type.lastIndexOf(".") + 1),
				MetricUnit.NONE);
		// Find class
		try {
			nodeType = (Class<? extends LifecycleComponent>) Class.forName(type);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e.getStackTrace());
		}
	}

	@Override
	public void initialize(List<Host> hosts) {
		List<LifecycleComponent> comps = new LinkedList<>();
		for (Host host : hosts) {
			try {
				comps.add(host.getComponent(nodeType));
			} catch (ComponentNotAvailableException e) {
				// Skip.
			}
		}
		setOverallMetric(new MNodesActiveValue(comps));
	}

	public class MNodesActiveValue
			implements de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue<Double> {

		private List<LifecycleComponent> comps;

		private long lastCalcTimestamp;

		private double activeNodes = -1;

		public MNodesActiveValue(List<LifecycleComponent> comps) {
			this.comps = comps;
		}

		@Override
		public Double getValue() {
			calc();
			return activeNodes;
		}

		@Override
		public boolean isValid() {
			calc();
			return activeNodes >= 0;
		}

		private void calc() {
			if (Time.getCurrentTime() == lastCalcTimestamp) {
				return;
			}
			lastCalcTimestamp = Time.getCurrentTime();
			activeNodes = 0;
			for (LifecycleComponent comp : comps) {
				if (comp.isActive()) {
					activeNodes++;
				}
			}
		}

	}

}
