package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import java.util.ArrayList;
import java.util.List;

import org.gervarro.democles.specification.impl.DefaultPattern;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

/**
 * This is a data class that stores the mapping between a simonstrator pattern and the corresponding Democles pattern
 * @author lneumann
 */
public class SimonstratorPatternToDemoclesIntegratedPatternMapping {
	
	private BiMap<Integer, EdgeID> edges;
	private BiMap<Integer, INodeID> nodes;
	private int origin;
	
	private DefaultPattern democlesPattern;
	private List<DefaultPattern> nacPatterns;
	
	void setDemoclesPattern(DefaultPattern democlesPattern){
		this.democlesPattern = democlesPattern;
	}
	
	void setNacPatterns(DefaultPattern nac){
		if(nacPatterns == null) {
			nacPatterns = new ArrayList<DefaultPattern>();
		}
		this.nacPatterns.add(nac);
	}

	void setOriginMapping(int i){
		this.origin = i;
	}

	void setEdgeMapping(int i, EdgeID edge){
		if(edges == null){
			edges = HashBiMap.create();;
		}
		edges.put(i, edge);
	}
	
	void setNodeMapping(int i, INodeID node){
		if(nodes == null){
			nodes = HashBiMap.create();;
		}
		nodes.put(i, node);
	}
	
	public int getOriginMapping(){
		return this.origin;
	}
	
	public EdgeID getEdgeMapping(int i){
		return edges.get(i);
	}
	
	public boolean hasEdgeMapping(EdgeID id) {
		return edges != null && edges.inverse().get(id) != null;
	}
	
	public int getEdgeMapping(EdgeID id){
		return edges.inverse().get(id);
	}
	
	public INodeID getNodeMapping(int i){
		return nodes.get(i);
	}
	
	public boolean hasNodeMapping(INodeID id) {
		return nodes != null && nodes.inverse().get(id) != null;
	}
	
	public int getNodeMapping(INodeID id){
		return nodes.inverse().get(id);
	}
	
	public DefaultPattern getDemoclesPattern() {
		return democlesPattern;
	}
	
	public List<DefaultPattern> getNacPatterns(){
		return nacPatterns;
	}

}
