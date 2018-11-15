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

package de.tud.kom.p2psim.impl.topology.movement;

import java.util.Properties;

import de.tud.kom.p2psim.impl.simengine.Simulator;

public class BonnMotionConfig extends Properties {
	
	public BonnMotionConfig() {
		super();
	}
	
	public void setModel(String model){
		this.put("model", model);
	}
	
	public void setSeed(long seed){
		this.put("R", Long.toString(seed));
	}
	
	public void setNodes(int nodes){
		this.put("n", Integer.toString(nodes));
	}
	
	public void setXLength(double x){
		this.put("x", Double.toString(x));
	}
	
	public void setYLength(double y){
		this.put("y", Double.toString(y));
	}
	
	public void setDuration(long duration){
		this.put("d", Long.toString(duration/Simulator.SECOND_UNIT));
	}
	
	public void setMinSpeed(double minSpeed){
		this.put("l", Double.toString(minSpeed));
	}
	
	public void setMaxSpeed(double maxSpeed){
		this.put("h", Double.toString(maxSpeed));
	}
	
	public void setPauseTime(double pauseTime){
		this.put("p", Double.toString(pauseTime));
	}
	
	public void setIgnore(double ignore){
		this.put("i", Double.toString(ignore));
	}
	
	public void setMeanSpeed(double meanSpeed){
		this.put("o", Double.toString(meanSpeed));
	}
	
	public void setDeltaSpeed(double deltaSpeed){
		this.put("p", Double.toString(deltaSpeed));
	}
	
	public void setMeanPauseTime(double meanPauseTime){
		this.put("k", Double.toString(meanPauseTime));
	}
	
	public void setDeltaPauseTime(double deltaPauseTime){
		this.put("l", Double.toString(deltaPauseTime));
	}
}
