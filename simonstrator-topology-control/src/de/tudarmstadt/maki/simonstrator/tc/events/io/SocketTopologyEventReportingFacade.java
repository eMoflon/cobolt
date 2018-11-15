package de.tudarmstadt.maki.simonstrator.tc.events.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EndOfContextEventHandlingPhaseEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EndOfTopologyControlPhaseEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.Event;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.EventLog;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.StartOfSimulationEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.UpdateEdgeEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.UpdateNodeEvent;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class SocketTopologyEventReportingFacade extends AbstractTopologyEventRecordingFacade {
	private static final String PROPERTY_COALAVIZ_COLOR = "color";

	private final String host = "localhost";

	private final int port;

	private static final Logger logger = Logger.getLogger(SocketTopologyEventReportingFacade.class);

	private final Map<EdgeID, String> edgeToRecentColorMap = new HashMap<>();

	@XMLConfigurableConstructor({ "port" })
	public SocketTopologyEventReportingFacade(final int port) {
		this.port = port;
		this.registerEvent(new StartOfSimulationEvent());
	}

	@Override
	public void initalize() {
		final int worldSize = TopologyControlComponent.find().getConfiguration().worldSize;
		this.registerEvent(new UpdateWorldSizeEvent(worldSize, worldSize));
	}

	@Override
	public void endContextEventSequence() {
		super.endContextEventSequence();
		writeAndClearEventLog();
	}

	@Override
	public void shutdown() {
		super.shutdown();
		writeAndClearEventLog();
	}

	@Override
	public INode addNode(final INode prototype) {
		final INode updatedNode = super.addNode(prototype);
		addDerivedEvents(prototype);
		return updatedNode;
	}

	@Override
	public IEdge addEdge(final IEdge prototype) {
		final IEdge addedEdge = super.addEdge(prototype);
		addDerivedEvents(prototype);
		return addedEdge;
	}

	@Override
	public <T> void updateNodeAttribute(final INode node, final SiSType<T> property) {
		super.updateNodeAttribute(node, property);
		addDerivedEvents(node);
	}

	@Override
	public <T> void updateEdgeAttribute(final IEdge edge, final SiSType<T> property) {
		super.updateEdgeAttribute(edge, property);
		addDerivedEvents(getGraph().getEdge(edge.getId()));
	}

	public void writeAndClearEventLog() {
		final EventLog eventLog = getEventLog();
		final EventLog filteredEventLog = filterEventLog(eventLog);
		writeEventLog(filteredEventLog, this.host, this.port);
		clearEventLog();
	}

	public static void writeEvent(final Event event, final String host, final int port) {
		final EventLog eventLog = new EventLog();
		eventLog.addEvent(event);
		writeEventLog(eventLog, host, port);
	}

	public static void writeEventLog(final EventLog filteredEventLog, final String host, final int port) {
		try {
			final ObjectMapper objectMapper = new ObjectMapper();
			final String jsonString = objectMapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(filteredEventLog);
			final String normalizedJsonString = cleanNewlines(jsonString);
			try (final Socket clientSocket = new Socket(host, port)) {
				final DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				outToServer.writeBytes(normalizedJsonString);
			} catch (final IOException e) {
				logger.error("Failed to write data due to " + e, e);
			}
		} catch (final JsonProcessingException e) {
			logger.error("Problem with JSON serialization", e);
		}
	}

	private void addDerivedEvents(final IEdge prototype) {
		final EdgeState state = prototype.getProperty(UnderlayTopologyProperties.EDGE_STATE);
		final String edgeColor;
		if (state != null) {
			switch (state) {
			case INACTIVE:
				edgeColor = "gray";
				break;
			case ACTIVE:
			case UNCLASSIFIED:
				edgeColor = "black";
				break;
			default:
				throw new IllegalArgumentException("Unsupported state: " + state);
			}
		} else {
			edgeColor = "black";
		}
		if (shallCreateEdgeColorEvent(prototype, edgeColor)) {
			this.registerEvent(new UpdateEdgeEvent(prototype, PROPERTY_COALAVIZ_COLOR, edgeColor));
			this.edgeToRecentColorMap.put(prototype.getId(), edgeColor);
		}
	}

	private boolean shallCreateEdgeColorEvent(final IEdge prototype, final String newEdgeColor) {
		final EdgeID id = prototype.getId();
		if (!this.edgeToRecentColorMap.containsKey(id))
			return true;
		else if (newEdgeColor.equals(this.edgeToRecentColorMap.get(id)))
			return false;
		return true;
	}

	private void addDerivedEvents(final INode prototype) {
		if (prototype.getProperty(UnderlayTopologyProperties.BASE_STATION_PROPERTY) != null) {
			this.registerEvent(new UpdateNodeEvent(prototype, PROPERTY_COALAVIZ_COLOR, "green"));
		}
	}

	/**
	 * Returns a view of the given {@link EventLog} that only contains events of a
	 * desired type
	 *
	 * @param eventLog
	 *            the original {@link EventLog}
	 * @return the filtered {@link EventLog}
	 */
	private EventLog filterEventLog(final EventLog eventLog) {
		final EventLog filteredEventLog = new EventLog();
		eventLog.getEvents().stream().filter(event -> keepEvent(event))
				.forEach(event -> filteredEventLog.addEvent(event));
		return filteredEventLog;
	}

	private boolean keepEvent(final Event event) {
		switch (event.getType()) {
		case UpdateNodeEvent.TYPE_ID: {
			final UpdateNodeEvent updateNodeEvent = (UpdateNodeEvent) event;
			final Object newValue = updateNodeEvent.getNewValue();
			final boolean isUndefinedNewValue = isUndefinedValue(newValue);
			return !isUndefinedNewValue && getNodePropertiesToReport().contains(updateNodeEvent.getProperty());
		}
		case UpdateEdgeEvent.TYPE_ID: {
			final UpdateEdgeEvent updateEdgeEvent = (UpdateEdgeEvent) event;
			final Object newValue = updateEdgeEvent.getNewValue();
			final boolean isUndefinedNewValue = isUndefinedValue(newValue);
			return !isUndefinedNewValue && getEdgePropertiesToReport().contains(updateEdgeEvent.getProperty());
		}
		case EndOfContextEventHandlingPhaseEvent.TYPE_ID:
		case EndOfTopologyControlPhaseEvent.TYPE_ID:
			return false;
		default:
			return true;
		}
	}

	private boolean isUndefinedValue(final Object newValue) {
		if (newValue == null)
			return true;

		if ("NaN".equals(newValue))
			return true;

		if (newValue instanceof Double) {
			final Double doubleValue = (Double) newValue;
			if (doubleValue.isNaN())
				return true;
		}
		return false;
	}

	private List<String> getNodePropertiesToReport() {
		final List<String> names = mapToNames(
				Arrays.asList(UnderlayTopologyProperties.LATITUDE, UnderlayTopologyProperties.LONGITUDE));
		names.add(PROPERTY_COALAVIZ_COLOR);
		return names;
	}

	private List<String> getEdgePropertiesToReport() {
		final List<String> names = mapToNames(Arrays.asList());
		names.add(PROPERTY_COALAVIZ_COLOR);
		return names;
	}

	private List<String> mapToNames(final List<SiSType<?>> properties) {
		return properties.stream().map(prop -> prop.getName()).collect(Collectors.toList());
	}

	private static String cleanNewlines(final String jsonString) {
		String result = jsonString.replaceAll("[\\n]+", "").replaceAll("[\\r]+", "");
		result = result + "\n";
		return result;
	}
}
