package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import java.util.Random;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.DefaultTopologyComponent;
import de.tud.kom.p2psim.impl.topology.movement.GaussMarkovMovement;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This modified version of the Gauss-Markov movement introduces a hesitation
 * probability, which allows nodes to not move at all with a certain
 * probability.
 */
public class HesitatedGaussMarkovMovement extends GaussMarkovMovement {

	final Random random = Randoms.getRandom(HesitatedGaussMarkovMovement.class);

	private double hesitationProbability;

	@XMLConfigurableConstructor({ "alpha", "edgeThreshold", "hesitationProbability" })
	public HesitatedGaussMarkovMovement(double alpha, double edgeThreshold, double hesitationProbability) {
		super(alpha, edgeThreshold);
		this.hesitationProbability = hesitationProbability;
	}

	@Override
	protected boolean shallMove(SimLocationActuator comp) {
		// Do not move if battery is empty
		if (comp instanceof DefaultTopologyComponent) {
			double currentPercentage = ((DefaultTopologyComponent) comp).getHost().getEnergyModel().getInfo()
					.getCurrentPercentage();
			if (currentPercentage == 0.0)
				return false;
		}

		final boolean isHesitating = random.nextDouble() <= hesitationProbability;
		return super.shallMove(comp) && !isHesitating;
	}

}
