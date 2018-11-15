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

package de.tud.kom.p2psim.impl.analyzer.metric.output;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSDataCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties.SiSScope;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.aggregation.AbstractAggregation;

/**
 * Metric output acting as a bridge to the SiS
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Sep 28, 2015
 */
public class MetricOutputSiS extends AbstractOutput {

	protected Map<INodeID, SiSComponent> hosts = new LinkedHashMap<INodeID, SiSComponent>();
	
	@Override
	public void onStop() {
		// ignore for now
	}

	@Override
	public void onInitialize(List<Metric> metrics) {
		
		List<INodeID> ids = new LinkedList<>();
		for (Host host : Oracle.getAllHosts()) {
			try {
				SiSComponent sis = host.getComponent(SiSComponent.class);
				hosts.put(host.getId(), sis);
			} catch (ComponentNotAvailableException e) {
				continue;
			}
		}

		// Register a metric at the SiS
		for (Metric metric : metrics) {
			// Only allow per-host metrics
			if (metric.isOverallMetric()) {
				continue;
			}
			
			MetricSiSDataCallback callback = new MetricSiSDataCallback(metric);
			
			for (SiSComponent hostSiS : hosts.values()) {
				// FIXME metric type cannot be resolved programmatically
				SiSType<Double> sisType = SiSTypes.getType(metric.getName(),
						Double.class);
				if (sisType == null) {
					// register type
					SiSTypes.registerType(metric.getName(), Double.class,
							new AbstractAggregation.AggregationDouble());
					sisType = SiSTypes.getType(metric.getName(), Double.class);
					assert sisType != null;
				}
				hostSiS.provide().nodeState(sisType, callback);
			}
		}

	}


	private class MetricSiSDataCallback implements SiSDataCallback<Double> {

		private final Metric<?> metric;
		
		private final SiSInfoProperties info = new SiSInfoProperties()
				.setScope(SiSScope.NODE_LOCAL);

		public MetricSiSDataCallback(Metric<?> metric) {
			this.metric = metric;
		}

		@Override
		public Double getValue(INodeID nodeId, SiSProviderHandle providerHandle)
				throws InformationNotAvailableException {
			@SuppressWarnings("unchecked")
			MetricValue<Double> mv = (MetricValue<Double>) metric.getPerHostMetric(nodeId);
			Double value = mv.getValue();
			if (mv.isValid()) {
				return value;
			}
			throw new InformationNotAvailableException();
		}

		@Override
		public SiSInfoProperties getInfoProperties() {
			return info;
		}

		@Override
		public Set<INodeID> getObservedNodes() {
			return hosts.keySet();
		}
	}

}
