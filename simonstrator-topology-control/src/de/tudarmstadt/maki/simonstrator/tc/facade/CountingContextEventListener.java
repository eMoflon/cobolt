package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class CountingContextEventListener implements IContextEventListener {

	private int nodeAddedCount;
	private int nodeRemovedCount;
	private final Map<SiSType<?>, Integer> nodeAttributeUpdateCount = new HashMap<>();
	private int edgeAddedCount;
	private int edgeRemovedCount;
	private final Map<SiSType<?>, Integer> edgeAttributeUpdateCount = new HashMap<>();

	public CountingContextEventListener() {
		reset();
	}

	public CountingContextEventListener(final CountingContextEventListener other) {
		this.nodeAddedCount = other.nodeAddedCount;
		this.nodeRemovedCount = other.nodeRemovedCount;
		this.nodeAttributeUpdateCount.putAll(other.nodeAttributeUpdateCount);
		this.edgeAddedCount = other.edgeAddedCount;
		this.edgeRemovedCount = other.edgeRemovedCount;
		this.edgeAttributeUpdateCount.putAll(other.edgeAttributeUpdateCount);
	}

	public void reset() {
		nodeAddedCount = 0;
		nodeRemovedCount = 0;
		nodeAttributeUpdateCount.clear();

		edgeAddedCount = 0;
		edgeRemovedCount = 0;
		edgeAttributeUpdateCount.clear();
	}

	public int getNodeAddedCount() {
		return nodeAddedCount;
	}

	public int getNodeRemovedCount() {
		return nodeRemovedCount;
	}

	public <T> int getNodeAttributeUpdateCount(final SiSType<T> property) {
		return nodeAttributeUpdateCount.containsKey(property) ? nodeAttributeUpdateCount.get(property) : 0;
	}

	public int getEdgeAddedCount() {
		return edgeAddedCount;
	}

	public int getEdgeRemovedCount() {
		return edgeRemovedCount;
	}

	public <T> int getEdgeAttributeUpdateCount(final SiSType<T> property) {
		return edgeAttributeUpdateCount.containsKey(property) ? edgeAttributeUpdateCount.get(property) : 0;
	}

	public int getAggregatedContextEventCount() {
		int count = this.nodeAddedCount + this.nodeRemovedCount + this.edgeAddedCount + this.edgeRemovedCount;
		for (final Integer attributeModificationCount : this.edgeAttributeUpdateCount.values()) {
			count += attributeModificationCount;
		}
		for (final Integer attributeModificationCount : this.nodeAttributeUpdateCount.values()) {
			count += attributeModificationCount;
		}
		return count;
	}

	@Override
	public void postNodeAdded(final INode node) {
		++nodeAddedCount;
	}

	@Override
	public void preNodeRemoved(final INode node) {
		++nodeRemovedCount;
	}

	@Override
	public <T> void postNodeAttributeUpdated(final INode node, final SiSType<T> property) {
		if (!nodeAttributeUpdateCount.containsKey(property)) {
			nodeAttributeUpdateCount.put(property, 0);
		}

		nodeAttributeUpdateCount.put(property, nodeAttributeUpdateCount.get(property) + 1);
	}

	@Override
	public void postEdgeAdded(final IEdge edge) {
		++edgeAddedCount;
	}

	@Override
	public void preEdgeRemoved(final IEdge edge) {
		++edgeRemovedCount;
	}

	@Override
	public <T> void postEdgeAttributeUpdated(final IEdge edge, final SiSType<T> property) {
		if (!edgeAttributeUpdateCount.containsKey(property)) {
			edgeAttributeUpdateCount.put(property, 0);
		}

		edgeAttributeUpdateCount.put(property, edgeAttributeUpdateCount.get(property) + 1);
	}

	public String format() {
		return String.format("[+n=%4d, -n=%4d, +e=%4d, -e=%4d, mod-w=%4d, mod-E=%4d, mod-p=%4d]", //
				this.nodeAddedCount, this.nodeRemovedCount, //
				this.edgeAddedCount, this.edgeRemovedCount, //
				this.getEdgeAttributeUpdateCount(UnderlayTopologyProperties.WEIGHT),
				this.getNodeAttributeUpdateCount(UnderlayTopologyProperties.REMAINING_ENERGY),
				this.getEdgeAttributeUpdateCount(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER));
	}

	@Override
	public String toString() {
		return String.format("CE listener %s", this.format());
	}

}
