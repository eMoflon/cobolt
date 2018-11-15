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

package de.tud.kom.p2psim.api.application;

/**
 * Interface for a WorkloadListener. This interface allow to add the
 * {@link WorkloadListener} and to remove them from the Generator.
 * 
 * @author Christoph Muenker
 * @version 1.0, 14.06.2013
 */
public interface WorkloadGenerator {

	/**
	 * Adds a {@link WorkloadListener} to the workload generator. If you add the
	 * same listener twice, the listener will be not added as a new listener.
	 * 
	 * @param listener
	 *            The listener, which should be added.
	 */
	public void addWorkloadListener(WorkloadListener listener);

	/**
	 * Removes a {@link WorkloadListener} from the workload generator.
	 * 
	 * @param listener
	 *            The listener, which should be removed.
	 */
	public void removeWorkloadListener(WorkloadListener listener);
}
