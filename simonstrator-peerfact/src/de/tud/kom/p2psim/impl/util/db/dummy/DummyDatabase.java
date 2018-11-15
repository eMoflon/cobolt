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

package de.tud.kom.p2psim.impl.util.db.dummy;

import de.tud.kom.p2psim.api.util.db.IDatabase;
import de.tud.kom.p2psim.api.util.db.IDocument;
import de.tud.kom.p2psim.api.util.db.IMeasurement;
import de.tud.kom.p2psim.api.util.db.IMetric;

public class DummyDatabase implements IDatabase {	
	@Override
	public void addMeasurement(IMeasurement measurement) {
	}

	@Override
	public void addDocument(IDocument document) {

	}

	@Override
	public void addSimulationInfo(String key, Object value) {
		
	}
	
	@Override
	public IMetric createMetric(String name, String type, String comment, String unit) {
		return new DummyMetric();
	}

	@Override
	public IDocument createDocument(String name) {
		return new DummyDocument();
	}
	
	public static class DummyDocument implements IDocument {

		@Override
		public void addEntry(String key, Object value) {

		}

		@Override
		public IDocument copy() {
			return new DummyDocument();
		}
	}
	
	public static class DummyMetric implements IMetric {
		@Override
		public IMeasurement createMeasurement() {
			return new DummyMeasurement(this);
		}

		@Override
		public IMeasurement createMeasurement(String[] types, Object[] values) {
			return new DummyMeasurement(this);
		}

	}
	
	public static class DummyMeasurement implements IMeasurement {
		private DummyMetric metric;
		
		public DummyMeasurement(DummyMetric metric) {
			this.metric = metric;
		}
		
		@Override
		public void addValue(String type, Object value) {
			
		}

		@Override
		public IMeasurement copy() {
			return new DummyMeasurement(metric);
		}

		@Override
		public IMetric getMetric() {
			return metric;
		}
		
	}

}
