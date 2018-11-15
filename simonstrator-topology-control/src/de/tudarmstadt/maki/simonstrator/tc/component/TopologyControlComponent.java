package de.tudarmstadt.maki.simonstrator.tc.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementPropertyBasedComparator;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.operation.AbstractOperation;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.CoalaVizReconfigureSasEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.Event;
import de.tudarmstadt.maki.simonstrator.tc.events.io.SocketTopologyEventReportingFacade;
import de.tudarmstadt.maki.simonstrator.tc.events.io.coalaviz.CoalaVizConstants;
import de.tudarmstadt.maki.simonstrator.tc.facade.CountingContextEventListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.CountingLinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.MultiplexingTopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.NullTopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamter;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.filtering.EdgeFilterFactory;
import de.tudarmstadt.maki.simonstrator.tc.filtering.EdgeFilters;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.ActiveTopologyControlAlgorithmMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.EdgeCountMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MeanDegreeMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.NodeCountMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SeedMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.TimestampMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.WorldSizeMetric;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.ITopologyControlReconfigurationComponent;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlReconfigurationDecision;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlSystemContext;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * This component manages the (periodic) execution of topology control.
 *
 * @author Roland Kluge - Initial implementation
 */
public class TopologyControlComponent implements HostComponent {

	private ITopologyControlFacade incrementalFacade;

	private final MultiplexingTopologyControlFacade multiplexingFacade;

	private LogicalLinkLayerUpdatingLinkStateListener virtualLinkLayerUpdater;

	private final TopologyControlComponentConfig configuration;

	private TopologyControlComponentAttributeHelper attributeHelper;

	private TopologyControlComponentStatisticsHelper statisticsHelper;

	private TopologyControlComponentEvaluationDataHelper evaluationDataHelper;

	private ITopologyControlFacade eventRecordingFacade;

	private TopologyControlInformationStoreComponent informationStore;

	private long currentTimestamp = -1;

	private Graph cachedInputTopology = null;

	// The timestamp of the last time that TC reconfiguration was triggered
	private long recentReconfigurationTimestamp = -1;

	public TopologyControlComponent(final TopologyControlComponentConfig configuration) {
		this.configuration = configuration;

		this.multiplexingFacade = new MultiplexingTopologyControlFacade();
		this.setIncrementalFacade(new NullTopologyControlFacade());
		this.setEventRecordingFacade(new NullTopologyControlFacade());
	}

	/**
	 * Returns the {@link TopologyControlComponent} on the given host (if exists)
	 *
	 * @param host
	 *                 the host to query
	 * @return the {@link TopologyControlComponent} or <code>null</code> if none
	 *         exists
	 */
	public static TopologyControlComponent find(final Host host) {
		try {
			return host.getComponent(TopologyControlComponent.class);
		} catch (final ComponentNotAvailableException e) {
			return null;
		}
	}

	/**
	 * Uses {@link Oracle} to find the (unique) {@link TopologyControlComponent}.
	 * This method requires global knowledge.
	 *
	 * @return the {@link TopologyControlComponent} or <code>null</code> if no host
	 *         contains a {@link TopologyControlComponent}
	 */
	public static TopologyControlComponent find() {
		final Optional<TopologyControlComponent> tcc = Oracle.getAllHosts().stream().filter(host -> {
			return find(host) != null;
		}).map(host -> find(host)).findAny();
		return tcc.orElse(null);
	}

	@Override
	public void initialize() {
		this.configuration.printToLog();
		final Host host = getHost();
		if (host != null) {
			setInformationStore(TopologyControlInformationStoreComponent.find(host));
		}

		this.multiplexingFacade.initalize();

		this.attributeHelper = new TopologyControlComponentAttributeHelper(this);

		this.evaluationDataHelper = new TopologyControlComponentEvaluationDataHelper(this);

		this.statisticsHelper = new TopologyControlComponentStatisticsHelper(this);

		this.configuration.monitoringComponents.forEach(component -> component.setParentComponent(this));

		registerListenersAtFacade();
		scheduleOperations();

		if (this.configuration.eventRecordingFacade != null) {
			final Event event = new CoalaVizReconfigureSasEvent();
			SocketTopologyEventReportingFacade.writeEvent(event, "localhost", CoalaVizConstants.DEFAULT_PORT);
		}
	}

	@Override
	public void shutdown() {
		this.multiplexingFacade.shutdown();
	}

	@Override
	public Host getHost() {
		return configuration.host;
	}

	public void setIncrementalFacade(final ITopologyControlFacade facade) {
		this.multiplexingFacade.removeDelegateFacade(this.getTopologyControlFacade());
		this.incrementalFacade = facade;
		this.incrementalFacade.setTopologyControlComponent(this);
		this.incrementalFacade.setOperationMode(getTopologyControlOperationMode());
		for (final EdgeFilterFactory factory : configuration.edgeFilterFactories) {
			this.incrementalFacade.addEdgeFilter(factory.createEdgeFilter());
		}
		this.multiplexingFacade.addDelegateFacade(this.getTopologyControlFacade());
	}

	public ITopologyControlFacade getTopologyControlFacade() {
		return incrementalFacade;
	}

	public void setEventRecordingFacade(final ITopologyControlFacade facade) {
		this.eventRecordingFacade = facade;
		this.eventRecordingFacade.setTopologyControlComponent(this);
		for (final EdgeFilterFactory factory : configuration.edgeFilterFactories) {
			facade.addEdgeFilter(factory.createEdgeFilter());
		}
		this.multiplexingFacade.addDelegateFacade(facade);
	}

	public ITopologyControlFacade getEventRecordingFacade() {
		return eventRecordingFacade;
	}

	public TopologyControlComponentConfig getConfiguration() {
		return configuration;
	}

	public TopologyControlComponentStatisticsHelper getStatisticsHelper() {
		return this.statisticsHelper;
	}

	public TopologyControlComponentAttributeHelper getAttributeHelper() {
		return attributeHelper;
	}

	void executeTopologyControlIteration() {
		statisticsHelper.beginNextIteration();

		Monitor.log(getClass(), Level.INFO, "%s (simtime=%s)-------------------------------------------",
				getIterationPrefix(), Time.getFormattedTime(Time.getCurrentTime()));

		doIteration();
	}

	private String getIterationPrefix() {
		return String.format("iter#%03d ", this.statisticsHelper.getIterationCounter());
	}

	/**
	 * Returns the topology control interval in simulation {@link Time} units
	 *
	 * @return topology control interval
	 */
	public long getTopologyControlInterval() {
		return (long) (configuration.topologyControlIntervalInMinutes * Time.MINUTE);
	}

	/**
	 * Returns the monitoring interval in simulation {@link Time} units
	 *
	 * @return monitoring interval
	 */
	public long getMonitoringInterval() {
		return (long) (this.configuration.monitoringIntervalInMinutes * Time.MINUTE);
	}

	/**
	 * Registers the listeners that propagate events from/to the topology control
	 * facade
	 */
	private void registerListenersAtFacade() {
		this.virtualLinkLayerUpdater = new LogicalLinkLayerUpdatingLinkStateListener(
				this.configuration.outputTopologyProvider, this.eventRecordingFacade);

		final ITopologyControlFacade incrementalFacade = getTopologyControlFacade();

		incrementalFacade.setOperationMode(getTopologyControlOperationMode());
		incrementalFacade.configureAlgorithm(configuration.topologyControlAlgorithmID);
		incrementalFacade.addLinkStateListener(virtualLinkLayerUpdater);
	}

	private TopologyControlOperationMode getTopologyControlOperationMode() {
		return this.getConfiguration().topologyControlOperationMode;
	}

	/**
	 * Activates the 'late initialization' event and the periodic topology control
	 * operation
	 */
	private void scheduleOperations() {
		createTopologyControlOperation().scheduleWithDelay(getTopologyControlInterval());
		createMonitoringOperation().scheduleWithDelay(getMonitoringInterval());
	}

	private AbstractOperation<TopologyControlComponent, Void> createMonitoringOperation() {
		return new TopologyControlMonitoringOperation(this);
	}

	private AbstractOperation<TopologyControlComponent, Void> createTopologyControlOperation() {
		final AbstractOperation<TopologyControlComponent, Void> operation;
		switch (this.configuration.topologyControlFrequencyMode) {
		case PERIODIC:
			operation = new TopologyControlPeriodicOperation(this);
			break;
		case SINGLESHOT:
			operation = new TopologyControlSingleShotOperation(this);
			break;
		default:
			throw new IllegalStateException(String.format("Cannot handle the following execution mode: '%s'",
					this.configuration.topologyControlFrequencyMode));
		}
		return operation;
	}

	private void removeNode(final INode node) {
		Monitor.log(getClass(), Level.INFO, "Removing node %s", node);
		multiplexingFacade.removeNode(node.getId());
	}

	private boolean shallAssumeActive(final IEdge edge) {
		return EdgeFilters.ignoreEdge(edge, getTopologyControlFacade().getEdgeFilters());
	}

	private void doIteration() {
		refreshCurrentTimestamp();

		if (isFirstIteration()) {
			if (this.getConfiguration().isReconfigurationEnabled()) {
				Monitor.log(getClass(), Level.INFO, "Initializing registered reconfiguration components");
				initializeReconfigurationComponents();
			} else {
				Monitor.log(getClass(), Level.INFO, "Reconfiguration support disabled.");
			}

			Monitor.log(getClass(), Level.INFO, "Configuration: " + configuration);

			this.statisticsHelper.collectAndWriteInitialInputTopologyStatistics(getInputTopology());
		}

		if (this.getConfiguration().isReconfigurationEnabled() && hasReconfigurationIntervalPassed()) {
			this.recentReconfigurationTimestamp = getCurrentTimestamp();
			requestAndDoReconfiguration();
		}

		invokeContextEventHandlers();

		invokeTopologyControlAlgorithm();

		doStatisticsRecording();
	}

	private boolean hasReconfigurationIntervalPassed() {
		final double nextReconfigurationTimestamp = this.recentReconfigurationTimestamp
				+ this.getConfiguration().reconfigurationIntervalInMinutes * Time.MINUTE;
		return this.recentReconfigurationTimestamp < 0 || this.getCurrentTimestamp() >= nextReconfigurationTimestamp;
	}

	private Graph refreshCachedInputTopology() {
		return this.cachedInputTopology = this.configuration.inputTopologyProvider.getTopology();
	}

	void doMonitoring() {
		refreshCurrentTimestamp();
		refreshCachedInputTopology();

		final Graph inputTopology = this.getInputTopology();
		final TopologyControlInformationStoreComponent informationStore = getInformationStore();
		informationStore.put(new EdgeCountMetric(inputTopology));
		informationStore.put(new NodeCountMetric(inputTopology));
		informationStore.put(new MeanDegreeMetric(inputTopology));
		informationStore.put(new WorldSizeMetric(this.configuration));
		informationStore.put(new SeedMetric(this.configuration));
		informationStore.put(new ActiveTopologyControlAlgorithmMetric(this.configuration));
		informationStore.put(new GoalNonfunctionalPropertyMetric(this.configuration));

		this.configuration.monitoringComponents.forEach(component -> component.performMeasurement());

	}

	/**
	 * Updates {@link #currentTimestamp} to the current simulation time and stores
	 * it in the current information record
	 * ({@link #getInformationRecordForCurrentTime()})
	 */
	private void refreshCurrentTimestamp() {
		final long currentSimulationTime = Time.getCurrentTime();
		if (this.currentTimestamp != currentSimulationTime) {
			this.setCurrentTimestamp(currentSimulationTime);
			getInformationStore().getOrCreateRecord(currentSimulationTime);
			getInformationStore().put(new TimestampMetric(this.getCurrentTimestamp()));
		}
	}

	/**
	 * Returns the currently configured time stamp
	 *
	 * @return the current time stamp
	 * @throws IllegalStateException
	 *                                   if no valid timestamp has ever been set
	 */
	private long getCurrentTimestamp() {
		if (this.currentTimestamp < 0)
			throw new IllegalStateException("Timestamp has not been set");

		return this.currentTimestamp;
	}

	/**
	 * Sets the current time stamp of this component
	 *
	 * May also be negative to indicate an unset time stamp
	 *
	 * @param currentTimestamp
	 *                             the time stamp to be used
	 */
	private void setCurrentTimestamp(final long currentTimestamp) {
		this.currentTimestamp = currentTimestamp;
	}

	/**
	 * Provides access to the {@link TopologyControlInformationStoreComponent} that
	 * is used by this component
	 *
	 * @return the used information store
	 */
	public TopologyControlInformationStoreComponent getInformationStore() {
		if (this.informationStore == null)
			throw new IllegalStateException("Information store has not been initialized.");

		return this.informationStore;
	}

	/**
	 * Updates the information store to be used by this component
	 *
	 * @param find
	 * @return
	 */
	public void setInformationStore(final TopologyControlInformationStoreComponent informationStore) {
		this.informationStore = informationStore;
	}

	private void initializeReconfigurationComponents() {
		getReconfigurationComponents().forEach(reconfigurationComponent -> {
			Monitor.log(getClass(), Level.INFO, "Initializing reconfiguration component '%s'.",
					reconfigurationComponent);
			reconfigurationComponent.initialize(this);
		});
	}

	private void requestAndDoReconfiguration() {
		for (final ITopologyControlReconfigurationComponent reconfigurationComponent : getReconfigurationComponents()) {
			Monitor.log(getClass(), Level.INFO, "%sRequesting reconfiguration decision from '%s'", getIterationPrefix(),
					getReconfigurationComponents());
			final TopologyControlSystemContext systemContext = collectTopologyControlContext();
			final TopologyControlReconfigurationDecision reconfigurationDecision = reconfigurationComponent
					.proposeReconfiguration(systemContext);
			final String ignoredPrefix = reconfigurationComponent.shallIgnoreDecision() ? " Ignored " : "Accepted ";
			Monitor.log(getClass(), Level.INFO, "%s%sReconfiguration: '%s'", ignoredPrefix, getIterationPrefix(),
					reconfigurationDecision);
			if (reconfigurationDecision != ITopologyControlReconfigurationComponent.NO_RECONFIGURATION_DECISION
					&& !reconfigurationComponent.shallIgnoreDecision()) {
				this.performReconfiguration(reconfigurationDecision);
				break;
			}
		}
	}

	private void performReconfiguration(final TopologyControlReconfigurationDecision reconfigurationDecision) {
		if (reconfigurationDecision.hasAlgorithmIdChanged()) {
			this.configuration.topologyControlAlgorithmID = reconfigurationDecision.getAlgorithmId();
			this.multiplexingFacade.configureAlgorithm(this.configuration.topologyControlAlgorithmID);
			for (final TopologyControlAlgorithmParamter parameter : reconfigurationDecision.getAlgorithmParameters()) {
				this.configuration.topologyControlAlgorithmParamters.put(parameter);
			}
		}
		final double topologyControlIntervalInMinutes = reconfigurationDecision.getTopologyControlIntervalInMinutes();
		if (topologyControlIntervalInMinutes != ITopologyControlReconfigurationComponent.NO_RECONFIGURATION_OF_TC_INTERVAL) {
			this.configuration.setTopologyControlIntervalInMinutes(topologyControlIntervalInMinutes);
		}
	}

	private TopologyControlSystemContext collectTopologyControlContext() {
		final TopologyControlSystemContext context = new TopologyControlSystemContext(this);
		return context;
	}

	private TopologyControlAlgorithmParamters getTopologyControlAlgorithmParameters() {
		return configuration.topologyControlAlgorithmID
				.extractRelevantParameters(configuration.topologyControlAlgorithmParamters);
	}

	private void invokeContextEventHandlers() {
		final Graph inputTopology = getInputTopology();

		Monitor.log(getClass(), Level.INFO, "iter#%03d CEH...", this.statisticsHelper.getIterationCounter());

		final CountingLinkStateListener ceLSMListener = new CountingLinkStateListener(this.eventRecordingFacade);
		final CountingContextEventListener intraCEExecutionCountingContextEventListener = new CountingContextEventListener();
		getTopologyControlFacade().addLinkStateListener(ceLSMListener);
		getTopologyControlFacade().addContextEventListener(intraCEExecutionCountingContextEventListener);

		multiplexingFacade.beginContextEventSequence();
		final long ceTic = System.currentTimeMillis();

		final Graph facadeGraph = getTopologyControlFacade().getGraph();
		for (final INode node : inputTopology.getNodes()) {
			final INodeID nodeId = node.getId();
			final boolean isEmpty = this.attributeHelper.isBatteryEmpty(nodeId);
			if (facadeGraph.containsNode(node)) {
				if (isEmpty) {
					removeNode(node);
				} else {
					final List<SiSType<?>> changedProperties = this.attributeHelper.updateNode(node);
					for (final SiSType<?> property : changedProperties) {
						multiplexingFacade.updateNodeAttribute(node, property);
					}
				}
			} else if (!isEmpty) {
				Monitor.log(getClass(), Level.INFO, "Adding node %s", node);
				attributeHelper.initializeNode(node);
				multiplexingFacade.addNode(node);
			}
		}

		final List<INode> toBeRemovedNodes = new ArrayList<>();
		for (final INode node : facadeGraph.getNodes()) {
			if (!inputTopology.containsNode(node)) {
				toBeRemovedNodes.add(node);
			}
		}

		for (final INode node : toBeRemovedNodes) {
			removeNode(node);
		}

		final List<IEdge> toBeRemovedEdges = new ArrayList<>();
		for (final IEdge edge : facadeGraph.getEdges()) {
			if (!inputTopology.containsEdge(edge)) {
				toBeRemovedEdges.add(edge);
			} else if (inputTopology.containsEdge(edge) && shallAssumeActive(edge)) {
				edge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.ACTIVE);
				virtualLinkLayerUpdater.activateLinkInVirtualTopology(edge);
			}
		}

		for (final IEdge edge : toBeRemovedEdges) {
			multiplexingFacade.removeEdge(edge);
		}

		final List<IEdge> modifiedEdges = new ArrayList<>();
		for (final IEdge edge : inputTopology.getEdges()) {
			if (this.attributeHelper.isBatteryEmpty(edge.fromId()) || this.attributeHelper.isBatteryEmpty(edge.toId()))
				continue;

			if (facadeGraph.containsEdge(edge)) {
				final boolean isEdgeModified = attributeHelper.updateEdge(this, edge, inputTopology);

				if (shallAssumeActive(edge)) {
					edge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.ACTIVE);
					virtualLinkLayerUpdater.activateLinkInVirtualTopology(edge);
				} else if (isEdgeModified) {
					modifiedEdges.add(edge);
				}

			} else {
				attributeHelper.initializeEdge(this, edge, inputTopology);

				if (shallAssumeActive(edge)) {
					edge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.ACTIVE);
					// TODO@rkluge: Move this somehow into the facade
					virtualLinkLayerUpdater.activateLinkInVirtualTopology(edge);
				} else {
					this.multiplexingFacade.addEdge(edge);
				}
			}
		}

		// Heuristic improvement because link unclassifications tend to
		// propagate towards links of larger weight
		modifiedEdges.sort(new GraphElementPropertyBasedComparator(UnderlayTopologyProperties.WEIGHT));

		for (final IEdge modifiedEdge : modifiedEdges) {
			this.multiplexingFacade.updateEdgeAttribute(modifiedEdge, UnderlayTopologyProperties.WEIGHT);
			this.multiplexingFacade.updateEdgeAttribute(modifiedEdge, UnderlayTopologyProperties.DISTANCE);
			this.multiplexingFacade.updateEdgeAttribute(modifiedEdge,
					UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
			this.multiplexingFacade.updateEdgeAttribute(modifiedEdge,
					UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE);
		}

		final long ceToc = System.currentTimeMillis();

		this.multiplexingFacade.checkConstraintsAfterContextEvent();

		final long checkToc = System.currentTimeMillis();

		this.multiplexingFacade.endContextEventSequence();

		final long contextEventDuration = ceToc - ceTic;
		final long contextEventCheckTime = checkToc - ceToc;
		statisticsHelper.recordPostContextEventStatistics(this, ceLSMListener,
				intraCEExecutionCountingContextEventListener, contextEventDuration, contextEventCheckTime);

		Monitor.log(getClass(), Level.INFO,
				"iter#%03d CEH   Done (t=%.0fms, t_check=%.0fms, numCEs=%s, allLSMs=%d [%s])",
				this.statisticsHelper.getIterationCounter(), this.getStatisticsDTO().ceTimeInMillis,
				getStatisticsDTO().ceCheckTimeInMillis, intraCEExecutionCountingContextEventListener.format(),
				getStatisticsDTO().ceLSMCountTotal, ceLSMListener.format());
	}

	private void invokeTopologyControlAlgorithm() {
		Monitor.log(getClass(), Level.INFO, "iter#%03d TCA...(algo=%s, parameters=%s)",
				this.statisticsHelper.getIterationCounter(), getTopologyControlFacade().getConfiguredAlgorithm(),
				getTopologyControlAlgorithmParameters());

		if (this.getTopologyControlOperationMode() == TopologyControlOperationMode.BATCH) {
			// IMPORTANT: These LSMs shall not be recorded!
			incrementalFacade.unclassifyAllLinks();
		}

		final CountingLinkStateListener tcLSMListener = new CountingLinkStateListener(this.eventRecordingFacade);
		getTopologyControlFacade().addLinkStateListener(tcLSMListener);

		virtualLinkLayerUpdater.reset();

		final long tic = System.currentTimeMillis();

		getTopologyControlFacade().run(getTopologyControlAlgorithmParameters());
		virtualLinkLayerUpdater.applyCachedLinkStateModifications();

		final long toc = System.currentTimeMillis();

		getTopologyControlFacade().checkConstraintsAfterTopologyControl();

		final long checkToc = System.currentTimeMillis();

		final EvaluationStatistics statisticsDTO = this.getStatisticsDTO();
		statisticsDTO.tcTimeInMillis = toc - tic;
		statisticsDTO.tcCheckTimeInMillis = checkToc - toc;
		statisticsDTO.tcLSMCountEffective = virtualLinkLayerUpdater.getEffectiveLinkStateChangeCount();

		statisticsDTO.tcLSMCountTotal = tcLSMListener.getAggregatedLinkStateChangeCount();
		statisticsDTO.tcLSMCountAct = tcLSMListener.getActivationCount();
		statisticsDTO.tcLSMCountInact = tcLSMListener.getInactivationCount();
		statisticsDTO.tcLSMCountClassification = tcLSMListener.getActivationCount()
				+ tcLSMListener.getInactivationCount();
		statisticsDTO.tcLSMCountUnclassification = tcLSMListener.getUnclassificationCount();
		statisticsDTO.tcViolationCount = getTopologyControlFacade().getConstraintViolationCount();

		this.multiplexingFacade.endTopologyControlSequence();

		Monitor.log(getClass(), Level.INFO,
				String.format(
						"iter#%03d TCA   Done (t=%.0fms,  t_check=%.0fms,  violations=%d, allLSMs=%d [%s], effLSMs=%d)",
						this.statisticsHelper.getIterationCounter(), statisticsDTO.tcTimeInMillis,
						statisticsDTO.tcCheckTimeInMillis, statisticsDTO.tcViolationCount,
						statisticsDTO.tcLSMCountTotal, tcLSMListener.format(), statisticsDTO.tcLSMCountEffective));

		getTopologyControlFacade().removeLinkStateListener(tcLSMListener);
	}

	private void doStatisticsRecording() {
		Monitor.log(getClass(), Level.INFO, "iter#%03d Stat...", this.statisticsHelper.getIterationCounter());
		final long tic = System.currentTimeMillis();

		this.statisticsHelper.collectStatistics(getInputTopology(), getOutputTopology());

		final long globalToc = System.currentTimeMillis();
		final EvaluationStatistics statisticsDTO = getStatisticsDTO();
		statisticsDTO.totalTimeInMinutes = (globalToc - this.statisticsHelper.getGlobalTic()) / 1000.0 / 60.0;

		final long toc = System.currentTimeMillis();
		statisticsDTO.statTimeInMillis = toc - tic;

		final String formattedSimulationTime = Time.getFormattedTime(Time.getCurrentTime());
		Monitor.log(getClass(), Level.INFO,
				"iter#%03d Stat  Done (t=%.0fms, n=%d, m=%d, t-real: %.2fmin, t-sim: %s, avgBatPct=%.2f, minBatPct=%.2f, maxBatPct=%.2f, numEmptyNodes=%d, numSCCsInInput=%d, numSCCsInOutput=%d)",
				this.statisticsHelper.getIterationCounter(), statisticsDTO.statTimeInMillis,
				statisticsDTO.nodeCountTotal, statisticsDTO.edgeCountTotal, //
				statisticsDTO.totalTimeInMinutes, formattedSimulationTime, //
				statisticsDTO.energyPercentageAvg, statisticsDTO.energyPercentageMin, statisticsDTO.energyPercentageMax, //
				statisticsDTO.nodeCountEmpty, //
				statisticsDTO.numStronglyConnectedComponentsInput, statisticsDTO.numStronglyConnectedComponentsOutput);

		this.evaluationDataHelper.writeDataLine();

	}

	/**
	 * Returns the topology that represents the output topology of TC
	 */
	public Graph getOutputTopology() {
		if (this.configuration.outputTopologyProvider == null)
			throw new IllegalStateException("Output topology provider not set");

		return this.configuration.outputTopologyProvider.getTopology();
	}

	/**
	 * Returns the topology from which the input topology of TC is read
	 */
	public Graph getInputTopology() {
		if (this.configuration.inputTopologyProvider == null)
			throw new IllegalStateException("Input topology provider not set");

		return this.cachedInputTopology;
	}

	private EvaluationStatistics getStatisticsDTO() {
		return this.statisticsHelper.getStatisticsDTO();
	}

	private List<ITopologyControlReconfigurationComponent> getReconfigurationComponents() {
		return this.configuration.reconfigurationComponents;
	}

	private boolean isFirstIteration() {
		return 1 == statisticsHelper.getIterationCounter();
	}
}
