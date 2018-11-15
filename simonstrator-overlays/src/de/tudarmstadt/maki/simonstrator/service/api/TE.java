package de.tudarmstadt.maki.simonstrator.service.api;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class TE {

	// TODO: Just a small scratch of an "Bjioern Prefered" API... needs some experimental implementations
	
	private TE() {
		
	}
	
	public static TE execute(String transitionName) {
		
		return new TE();
	}
	
	public TE parallel(List<NetID> nodes) {
		
		return this;
	}
	
	public TE callback() {
		return this;
	}
}
