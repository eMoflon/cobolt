package de.tudarmstadt.maki.simonstrator.overlay.monitoring.oracle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.api.component.monitoring.MonitoringComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSDataCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.AggregationFunction;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSRequest;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.AggregationNotPossibleException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;

/**
 * Implementation of the {@link MonitoringComponent} using global knowledge.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class GlobalKnowledgeResolver implements MonitoringComponent {

	private final Host host;

	private SiSComponent ownSiS;

	public GlobalKnowledgeResolver(Host host) {
		this.host = host;
	}

	@Override
	public Host getHost() {
		return host;
	}

	public SiSComponent getLocalSiS() {
		if (ownSiS == null) {
			try {
				ownSiS = host.getComponent(SiSComponent.class);
			} catch (ComponentNotAvailableException e) {
				throw new AssertionError();
			}
		}
		return ownSiS;
	}

	@Override
	public void initialize() {
		// not required
		if (!Oracle.isSimulation()) {
			throw new AssertionError();
		}
	}

	@Override
	public void shutdown() {
		// not required
	}

	@Override
	public <T> MonitoringHandle collectAggregatedObservation(
			AggregationFunction aggFunction, SiSType<T> type,
			SiSRequest request, MonitoringResultCallback<T> resultCallback) {
		MonitoringHandle handle = collectRawObservations(type, request);
		/*
		 * Calc Aggregate
		 */
		Map<INodeID, T> values = getLocalSiS().get().allLocalObservations(type,
				request);
		try {
			resultCallback
					.onResult(type.aggregate(values.values(), aggFunction));
		} catch (AggregationNotPossibleException e) {
			throw new AssertionError();
		}
		return handle;
	}

	@Override
	public <T> MonitoringHandle collectRawObservations(SiSType<T> type,
			SiSRequest request) {
		/*
		 * Request needs to be cloned once and then altered to resolve local
		 * scope!
		 */
		SiSRequest localRequest = request.clone();
		SiSInfoProperties infoProperties = new SiSInfoProperties();
		infoProperties.setLastUpdateTimestamp();
		MonitoringDataCallback<T> provider = new MonitoringDataCallback<T>(
				type, localRequest, infoProperties);
		SiSProviderHandle sisHandle = getLocalSiS().provide().nodeState(type,
				provider);
		provider.setSiSHandle(sisHandle);
		return new MonitoringHandleImpl<T>(type, request, provider);
	}

	@Override
	public void stopCollecting(MonitoringHandle handle) {
		MonitoringHandleImpl<?> h = (MonitoringHandleImpl<?>) handle;
		getLocalSiS().provide().revoke(h.provider.sisHandle);
	}

	@Override
	public Graph getCurrentDataGraph() {
		/*
		 * TODO implement
		 */
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	private class MonitoringHandleImpl<T> implements MonitoringHandle {

		private final MonitoringDataCallback<T> provider;

		public MonitoringHandleImpl(SiSType<T> type, SiSRequest request,
				MonitoringDataCallback<T> provider) {
			this.provider = provider;
		}

	}

	private class MonitoringDataCallback<T> implements SiSDataCallback<T> {

		private final Map<INodeID, SiSComponent> allSis = new LinkedHashMap<>();

		private final SiSType<T> type;

		private final SiSRequest request;

		private SiSProviderHandle sisHandle;

		private SiSInfoProperties sourceInfoProperties;

		public MonitoringDataCallback(SiSType<T> type, SiSRequest request,
				SiSInfoProperties sourceInfoProperties) {
			for (Host host : Oracle.getAllHosts()) {
				try {
					SiSComponent sisComp = host.getComponent(SiSComponent.class);
					allSis.put(host.getId(), sisComp);
				} catch (ComponentNotAvailableException e) {
					//
				}
			}
			this.type = type;
			this.request = request;
			this.sourceInfoProperties = sourceInfoProperties;
		}

		public void setSiSHandle(SiSProviderHandle handle) {
			sisHandle = handle;
		}

		@Override
		public T getValue(INodeID nodeId, SiSProviderHandle providerHandle)
				throws InformationNotAvailableException {
			/*
			 * TODO check request (needs to be altered?)
			 */

			/*
			 * If the request is concerning the cellular latency SiSType the request must be altered. It is then a
			 * observation of the latency from the to be asked component to the requesting component.
			 * 
			 * To be asked component is the nodeId.
			 * 
			 * Requesting component is this current host.
			 */
			if (type.equals(SiSTypes.LATENCY_CELL)) {
				return allSis.get(nodeId).get().localObservationOf(GlobalKnowledgeResolver.this.host.getId(), type, request);
			}

			return allSis.get(nodeId).get().localState(type, request);
		}

		@Override
		public Set<INodeID> getObservedNodes() {
			return allSis.keySet();
		}

		@Override
		public SiSInfoProperties getInfoProperties() {
			// Global Knowledge: always accurate :)
			sourceInfoProperties.setLastUpdateTimestamp();
			return sourceInfoProperties;
		}

	}

	/**
	 * Factory for the {@link GlobalKnowledgeResolver}
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public static class Factory implements HostComponentFactory {

		@Override
		public HostComponent createComponent(Host host) {
			return new GlobalKnowledgeResolver(host);
		}

	}

}
