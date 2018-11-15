package de.tudarmstadt.maki.simonstrator.api.component.sis.util;

import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSDataCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;

/**
 * Utility-Bridge between {@link TopologyProvider} and {@link SiSComponent}
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class SiSTopologyProvider implements SiSDataCallback<Graph> {

	private final TopologyProvider topo;

	private final TopologyID topoId;

	private Set<INodeID> localNodeID;

	private SiSInfoProperties infoProperties;

	public SiSTopologyProvider(SiSComponent sis, SiSType<Graph> type,
			TopologyProvider topo, TopologyID topoId, Class<?> srcComponent) {
		this.topo = topo;
		this.topoId = topoId;
		this.localNodeID = INodeID.getSingleIDSet(sis.getHost().getId());
		infoProperties = new SiSInfoProperties()
				.setSourceComponent(srcComponent);
		sis.provide().nodeState(type, this);
	}

	@Override
	public SiSInfoProperties getInfoProperties() {
		return infoProperties;
	}

	@Override
	public Set<INodeID> getObservedNodes() {
		return localNodeID;
	}

	@Override
	public Graph getValue(INodeID nodeID, SiSProviderHandle providerHandle)
			throws InformationNotAvailableException {
		if (localNodeID.contains(nodeID)) {
			return topo.getLocalView(topoId);
		}
		throw new InformationNotAvailableException();
	}

}
