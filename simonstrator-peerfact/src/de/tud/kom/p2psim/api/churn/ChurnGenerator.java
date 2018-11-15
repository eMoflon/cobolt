/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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



package de.tud.kom.p2psim.api.churn;

import de.tud.kom.p2psim.api.scenario.Composable;

/**
 * This component models the connectivity of peers within p2p systems which is
 * also referred to as churn. Using a specific ChurnModel, it is possible to
 * study its impact on the performance of a p2p system.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 12/03/2007
 * 
 */
public interface ChurnGenerator extends Composable {

	/**
	 * Sets a specific ChurnModel which will be used within a simulation run
	 * 
	 * @param model
	 *            the specified churn model
	 */
	public void setChurnModel(ChurnModel model);

	/**
	 * Sets the point in time at which the simulation framework will activate
	 * the churn behavior which has to be set using setChurnModel()-method.
	 * 
	 * @param time
	 *            start time at which peers will be affected by churn
	 */
	public void setStart(long time);

	/**
	 * Sets the point in time at which the simulation framework will deactivate
	 * the churn behavior.
	 * 
	 * @param time
	 *            end time at which peers will not be no longer affected by
	 *            churn
	 */
	public void setStop(long time);

}
