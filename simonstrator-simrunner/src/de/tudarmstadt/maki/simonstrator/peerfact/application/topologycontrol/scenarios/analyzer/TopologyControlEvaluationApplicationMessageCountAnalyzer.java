package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.analyzer;

import java.io.IOException;
import java.io.Writer;

import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;

public class TopologyControlEvaluationApplicationMessageCountAnalyzer implements Analyzer {


	@Override
	public void start() {
	}

	@Override
	public void stop(Writer out) {
		int totalReceivedMessage = 0;
		int totalSentMessages = 0;
		for (final Host host : GlobalOracle.getHosts()) {
			try {
				TopologyControlEvaluationApplication_ImplBase application = host
						.getComponent(TopologyControlEvaluationApplication_ImplBase.class);
				totalReceivedMessage += application.getTotalMessageReceivedCount();
				totalSentMessages += application.getTotalMessageSentCount();
			} catch (final ComponentNotAvailableException e) {
				// Disabled
			}
		}
		try {
			out.write(String.format("Message statistics from application '%s'\n",
					TopologyControlEvaluationApplication_ImplBase.class));
			out.write(String.format("Total sent messages: %d, total received messages: %d\n", totalSentMessages,
					totalReceivedMessage));
			out.flush();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
