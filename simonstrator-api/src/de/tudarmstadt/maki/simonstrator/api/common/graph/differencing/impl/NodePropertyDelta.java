/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.common.graph.differencing.impl;

import de.tudarmstadt.maki.simonstrator.api.common.Timestamp;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.differencing.api.IGraphElementPropertyDelta;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

public class NodePropertyDelta<T> extends NodeDelta implements IGraphElementPropertyDelta<T> {

	private final SiSType<T> property;
	private final T oldValue;

	public NodePropertyDelta(final SiSType<T> property, final T oldValue, final INode node, final Timestamp timestamp) {
		super(node, timestamp);
		this.property = property;
		this.oldValue = oldValue;
	}

	@Override
	public SiSType<T> getProperty() {
		return property;
	}

	@Override
	public T getOldValue() {
		return oldValue;
	}

}
