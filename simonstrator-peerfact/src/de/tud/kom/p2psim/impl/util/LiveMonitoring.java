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


package de.tud.kom.p2psim.impl.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Global unit for managing runtime information of the simulator that may be displayed to the
 * user.
 * 
 * Manages a list of ProgressValue objects. These objects are a basic name/value scheme: They all have an 
 * arbitrary, human-readable name and a value that may (and should) change during runtime. 
 * 
 * @author Leo Nobach
 *
 */
public class LiveMonitoring {

	private static Map<String, ProgressValue> progValues = new LinkedHashMap<String, ProgressValue>();

	private static Map<String, Integer> counter = new LinkedHashMap<String, Integer>();

	/**
	 * Returns the list of ProgressValues that are currently registered to the 
	 * LiveMonitoring unit.
	 * @return
	 */
	public static List<ProgressValue> getProgressValues() {
        synchronized (progValues) {
			return new ArrayList<ProgressValue>(progValues.values());
        }
	}

	/**
	 * Adds a ProgressValue to the LiveMonitoring unit.
	 * @param val
	 */
	public static void addProgressValue(Value val) {
        String name = val.getName();

        synchronized (progValues) {
            if (progValues.get(name) == null) {
                if (val instanceof AvgProgressValue) {
                    AvgProgressValueAggr aggr = new AvgProgressValueAggr((AvgProgressValue)val);
                    progValues.put(name, aggr);
                } else if (val instanceof SumProgressValue) {
                    SumProgressValueAggr aggr = new SumProgressValueAggr((SumProgressValue)val);
                    progValues.put(name, aggr);
                } else {
                    progValues.put(name, (ProgressValue)val);
                }
                counter.put(name, 1);
            } else {
                ProgressValue value = progValues.get(name);

                if (value instanceof AvgProgressValueAggr) {
                    ((AvgProgressValueAggr)value).add((AvgProgressValue)val);
                } else  if (value instanceof SumProgressValueAggr) {
                    ((SumProgressValueAggr)value).add((SumProgressValue)val);
                } else {
                    Integer count = counter.get(name);
                    counter.put(name, count+1);
                    if (val instanceof ProgressValue) {
                        progValues.put(name + "(" + count + ")", (ProgressValue)val);
                    }
                }
            }
        }
	}

    public interface Value {
        /**
         * Returns an arbitrary fancy human-readable name describing this value.
         * @return
         */
        public String getName();
    }

	/**
	 * Value that can be monitored at runtime. Simple name/value data structure. May be inserted in the LiveMonitoring
	 * unit that allows global dispatching of this value (e.g. the GUIRunner or other UIs) BEFORE simulation start 
	 * (in the configuration period or before).
	 *
	 */
	public interface ProgressValue extends Value {
		/**
		 * Returns the value how it shall be displayed.
		 * Mind that this method may be called from another thread than the simulation thread.
		 * Therefore, you should synchronize it when large data structures are crawled.
		 * @return
		 */
		public String getValue();
		
	}

    public interface AvgProgressValue extends Value {
        public double getValue();
    }

    public interface SumProgressValue extends Value {
        public double getValue();
    }

    private static class AvgProgressValueAggr implements ProgressValue  {
		private List<AvgProgressValue> values = new ArrayList<AvgProgressValue>();
        private String name = "";

        public AvgProgressValueAggr(AvgProgressValue value) {
            this.name = value.getName();
            values.add(value);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            double sum = 0;
            synchronized (values) {
                for (AvgProgressValue value : values) {
                    sum += value.getValue();
                }
            }
            return "" + (sum / values.size());
        }

        public void add(AvgProgressValue value) {
            synchronized (values) {
                values.add(value);
            }
        }
    }

    private static class SumProgressValueAggr implements ProgressValue  {
		private List<SumProgressValue> values = new ArrayList<SumProgressValue>();
        private String name = "";

        public SumProgressValueAggr(SumProgressValue value) {
            this.name = value.getName();
            values.add(value);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            double sum = 0;
            synchronized (values) {
                for (SumProgressValue value : values) {
                    sum += value.getValue();
                }
            }
            return "" + sum;
        }

        public void add(SumProgressValue value) {
            synchronized (values) {
                values.add(value);
            }
        }
    }

	/**
	 * Adds a ProgressValue to the LiveMonitoring unit. Does nothing, if an object equal to this one
	 * is already in the live monitoring field list.
	 * @param val
	 */
	public static void addProgressValueIfNotThere(ProgressValue val) {
        synchronized (progValues) {
		    if (!progValues.values().contains(val)) addProgressValue(val);
        }
	}
	
	static List<IPeriodicFulltextDump> dumps = new LinkedList<IPeriodicFulltextDump>();
	
	public interface IPeriodicFulltextDump {
		
		public String getDumpAsString();
		
	}
	
	public static void addFulltextDumper(IPeriodicFulltextDump dump) {
		dumps.add(dump);
	}
	
	public static List<IPeriodicFulltextDump> getFulltextDumps() {
		return dumps;
	}
	
}
