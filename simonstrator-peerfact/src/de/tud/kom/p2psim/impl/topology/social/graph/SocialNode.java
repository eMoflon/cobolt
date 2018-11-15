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
 * The Node for the Social Graph, with the information of the activity! The
 * Default value for the activity is 1. The value should be only in the range of
 * 0 to 1.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.06.2013
 */
public class SocialNode {
	private static int idCounter = 0;

	private double activity;

	private int id;

	public SocialNode() {
		this.activity = 1;
		this.id = idCounter++;
	}

	public SocialNode(int id) {
		this.id = id;
		this.activity = 1;
	}

	public SocialNode(double activity) {
		this.activity = activity;
		this.id = idCounter++;
	}

	public SocialNode(int id, double activity) {
		this.id = id;
		this.activity = activity;
	}

	protected void setActivity(double activity) {
		this.activity = activity;
	}

	public double getActivity() {
		return activity;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "[ID: " + id + ", Activity: " + activity + "]";
	}
}
