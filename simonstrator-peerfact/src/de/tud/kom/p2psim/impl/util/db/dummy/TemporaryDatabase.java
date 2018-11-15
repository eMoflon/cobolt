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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.util.db.IDatabase;
import de.tud.kom.p2psim.api.util.db.IDocument;
import de.tud.kom.p2psim.api.util.db.IMeasurement;
import de.tud.kom.p2psim.api.util.db.IMetric;

public class TemporaryDatabase implements IDatabase {

	private Map<String, Object> simulationInfo = Maps.newLinkedHashMap();
	private List<TemporaryDocument> documents = Lists.newArrayList();
	private List<TemporaryMeasurement> measurements = Lists.newArrayList();
	
	@Override
	public void addMeasurement(IMeasurement measurement) {
		measurements.add((TemporaryMeasurement)measurement);
	}

	@Override
	public void addDocument(IDocument document) {
		documents.add((TemporaryDocument)document);
	}

	@Override
	public void addSimulationInfo(String key, Object value) {
		simulationInfo.put(key, value);
	}
	
	private IDocument convertDocument(IDatabase database, TemporaryDocument temporaryDocument) {
		IDocument document = database.createDocument(temporaryDocument.getName());
		
		System.err.println("Converting document " + temporaryDocument.getName());
		
		Map<String, Object> entries = temporaryDocument.getEntryMap();
		for (String key : entries.keySet()) {
			System.err.println("Moving key " + key + " with value " + entries.keySet() + " to new database");
			if (entries.get(key) instanceof TemporaryDocument) {
				document.addEntry(key, convertDocument(database, (TemporaryDocument)entries.get(key)));
			} else {
				document.addEntry(key, entries.get(key));
			}
		}

		return document;
	}
	
	private IMeasurement convertMeasurement(IDatabase database, Map<IMetric, IMetric> metricMapping, TemporaryMeasurement temporaryMeasurement) {
		TemporaryMetric temporaryMetric = (TemporaryMetric) temporaryMeasurement.getMetric();
		
		IMetric metric = metricMapping.get(temporaryMetric);
		
		if (metric == null) {
			metric = database.createMetric(temporaryMetric.getName(), temporaryMetric.getType(), temporaryMetric.getComment(), temporaryMetric.getUnit());
			metricMapping.put(temporaryMetric, metric);
		}
		
		IMeasurement measurement = metric.createMeasurement();
		
		Map<String, Object> entries = temporaryMeasurement.getEntryMap();
		for (String key : entries.keySet()) {
			if (entries.get(key) instanceof TemporaryMeasurement) {
				measurement.addValue(key, convertMeasurement(database, metricMapping, (TemporaryMeasurement)entries.get(key)));
			} else if (entries.get(key) instanceof TemporaryDocument) {
				measurement.addValue(key, convertDocument(database, (TemporaryDocument)entries.get(key)));
			} else {
				measurement.addValue(key, entries.get(key));
			}
		}
		
		return measurement;
	}

	public void copyToDatabase(IDatabase database) {
		for (String key : simulationInfo.keySet()) {
			System.err.println("Adding simulation info " + key + " with value " + simulationInfo.get(key) + " to new database");
			database.addSimulationInfo(key, simulationInfo.get(key));
		}
		
		for (TemporaryDocument document : documents) {
			IDocument newDocument = convertDocument(database, document); 
			
			database.addDocument(newDocument);
		}

		Map<IMetric, IMetric> metricMapping = Maps.newHashMap();
		
		for (TemporaryMeasurement temporaryMeasurement : measurements) {
			
			IMeasurement measurement = convertMeasurement(database, metricMapping, temporaryMeasurement);
			
			database.addMeasurement(measurement);
		}
	}
	
	@Override
	public IMetric createMetric(String name, String type, String comment, String unit) {
		return new TemporaryMetric(name, type, comment, unit);
	}

	@Override
	public IDocument createDocument(String name) {
		return new TemporaryDocument(name);
	}
	
	public static class TemporaryDocument implements IDocument {
		private HashMap<String, Object> entries = Maps.newLinkedHashMap();
		private String name;
		
		public TemporaryDocument(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override
		public void addEntry(String key, Object value) {
			entries.put(key, value);
		}
		
		public Map<String, Object> getEntryMap() {
			return entries;
		}

		@Override
		public IDocument copy() {
			TemporaryDocument document = new TemporaryDocument(name);
			
			document.entries = (HashMap)entries.clone();
			
			return document;
		}
	}
	
	public static class TemporaryMetric implements IMetric {
		private String name;
		private String type;
		private String comment;
		private String unit;
		
		public TemporaryMetric(String name, String type, String comment, String unit) {
			this.name = name;
			this.type = type;
			this.comment = comment;
			this.unit = unit;
		}
		
		public String getName() {
			return name;
		}
		
		public String getType() {
			return type;
		}
		
		public String getComment() {
			return comment;
		}
		
		public String getUnit() {
			return unit;
		}

		@Override
		public IMeasurement createMeasurement() {
			return new TemporaryMeasurement(this);
		}

		@Override
		public IMeasurement createMeasurement(String[] types, Object[] values) {
			TemporaryMeasurement measurement = new TemporaryMeasurement(this);
			for (int i = 0; i < types.length; i++) {
				measurement.addValue(types[i], values[i]);
			}
			return measurement;
		}

	}
	
	public static class TemporaryMeasurement implements IMeasurement {
		private HashMap<String, Object> entries = Maps.newHashMap();
		private TemporaryMetric metric;
		
		public TemporaryMeasurement(TemporaryMetric metric) {
			this.metric = metric;
		}
		
		@Override
		public void addValue(String type, Object value) {
			entries.put(type, value);
		}

		public HashMap<String, Object> getEntryMap() {
			return entries;
		}
		
		@Override
		public IMeasurement copy() {
			TemporaryMeasurement measurement = new TemporaryMeasurement(metric);
			
			measurement.entries = (HashMap)entries.clone();
			
			return measurement;
		}

		@Override
		public IMetric getMetric() {
			return metric;
		}
		
	}

}
