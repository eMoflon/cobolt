/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.topology.social.graph;

/**
 * The Edge for the Social Graph, with the information of the interaction! The
 * Default value for the interaction is 1. The value should be only in the range
 * of 0 to 1.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.06.2013
 */
public class SocialEdge {

	private double interaction;

	public SocialEdge() {
		this.interaction = 1;
	}

	public SocialEdge(double interaction) {
		this.interaction = interaction;
	}

	protected void setInteraction(double interaction) {
		this.interaction = interaction;
	}

	public double getInteraction() {
		return this.interaction;
	}

	@Override
	public String toString() {
		return "Interaction: +" + interaction;
	}
}
