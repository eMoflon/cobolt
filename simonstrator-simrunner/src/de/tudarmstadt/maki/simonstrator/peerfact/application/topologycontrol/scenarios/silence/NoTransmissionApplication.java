package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.silence;

import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;

/**
 * This component neither sends nor transmits data.
 * 
 * @author Roland Kluge - Initial implementation
 */
public class NoTransmissionApplication extends TopologyControlEvaluationApplication_ImplBase {

	public NoTransmissionApplication(TopologyControlEvaluationApplicationConfig config) {
		super(config);
	}
}
