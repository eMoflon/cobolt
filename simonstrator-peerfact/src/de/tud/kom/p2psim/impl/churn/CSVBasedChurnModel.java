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

package de.tud.kom.p2psim.impl.churn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.impl.scenario.DefaultConfigurator;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * A simple churn model that reads a text file as input to set groups of nodes on- and offline.
 * Doing so the path to the text file has to be given in the config.
 * 
 * The file must have the following format:
 * 
 * group_name, time when to go online, time when to go offline, duration
 * 
 * The following example shows the format:
 * 
 * main_noise_peers_8 , 30min, 15min, 45min
 * main_noise_peers_8 , 1h, 2h, 1h
 * ...
 * 
 * @author Nils Richerzhagen
 * @version 1.0, Feb 18, 2015
 */
public class CSVBasedChurnModel implements ChurnModel {

	private String filename;
	
	private final String SEP = ",";
	
	private Map<String, LinkedList<ChurnInfo>> churnInfos;

	private Map<String, Boolean> usedForDowntime;
	

	/**
	 * 
	 * @param file
	 */
	@XMLConfigurableConstructor({ "file" })
	public CSVBasedChurnModel(String file) {
		this.filename = file;
		this.churnInfos = new LinkedHashMap<String, LinkedList<ChurnInfo>>();
		this.usedForDowntime = new LinkedHashMap<String, Boolean>();
	}

	@Override
	public long getNextUptime(SimHost host) {
		String groupId = host.getProperties().getGroupID();
		LinkedList<ChurnInfo> hostChurnInfos = churnInfos.get(groupId);
		
		if(usedForDowntime.containsKey(groupId) && usedForDowntime.get(groupId) == true){
			hostChurnInfos.removeFirst();
			usedForDowntime.put(groupId, false);
		}
		else
			usedForDowntime.put(groupId, false);
		
		if(hostChurnInfos.isEmpty())
			throw new AssertionError("No more churn infos left, is tht possible? " + host.getProperties().getGroupID());
		ChurnInfo churnInfo = hostChurnInfos.getFirst();
		if(churnInfo == null){
			throw new AssertionError("Seems to have not enough entries for group: " + host.getProperties().getGroupID());
		}
		

//		if(!notRemovedFirstThisRun){
//			notRemovedFirstThisRun = true;
//		}
		return churnInfo.getStartTime() - Simulator.getCurrentTime();
		
//		// the next interval should be an offline 
//		if(churnInfo.isOnline()){
//			return churnInfo.getDuration();
//		}
//		else{
//			try {
//				hostChurnInfos.removeFirst();
//			} catch (NoSuchElementException e) {
//				throw new AssertionError("Seems to have not enough entries for group: " + host.getProperties().getGroupID());
//			}
//			churnInfo = hostChurnInfos.getFirst();
//			if(churnInfo == null)
//			{
//				throw new AssertionError("Seems to have not enough entries for group: " + host.getProperties().getGroupID());
//			}
//			if(!churnInfo.isOnline()){
//				throw new AssertionError("There should be an online interval after the offline one: " + host.getProperties().getGroupID());
//			}
//			return churnInfo.getDuration();
//		}
	}

	@Override
	public long getNextDowntime(SimHost host) {
		String groupId = host.getProperties().getGroupID();
		LinkedList<ChurnInfo> hostChurnInfos = churnInfos.get(groupId);
		
		usedForDowntime.put(groupId, true);
		
//		if(usedForDowntime){
//			hostChurnInfos.removeFirst();
//			usedForDowntime = false;
//		}
		
		ChurnInfo churnInfo = hostChurnInfos.getFirst();
		if(churnInfo == null){
			throw new AssertionError("Seems to have not enough entries for group: " + host.getProperties().getGroupID());
		}
		// use churn info
		return churnInfo.getEndTime() - Simulator.getCurrentTime();
		
//		// the next interval should be an offline 
//		if(!churnInfo.isOnline()){
//			return churnInfo.getDuration();
//		}
//		else{
//			try {
//				hostChurnInfos.removeFirst();
//			} catch (NoSuchElementException e) {
//				throw new AssertionError("Seems to have not enough entries for group: " + host.getProperties().getGroupID());
//			}
//			churnInfo = hostChurnInfos.getFirst();
//			if(churnInfo == null){
//				throw new AssertionError("Seems to have not enough entries for group: " + host.getProperties().getGroupID());
//			}
//			if(churnInfo.isOnline()){
//				throw new AssertionError("There should be an offline interval after the previous online one: " + host.getProperties().getGroupID());
//			}
//			return churnInfo.getDuration();
//		}
	}

	@Override
	public void prepare(List<SimHost> churnHosts) {

		for (SimHost host : churnHosts) {
			for (SimNetInterface netI : host.getNetworkComponent()
					.getSimNetworkInterfaces()) {
				if (netI.isOnline()) {
					netI.goOffline();
				}
			}
		}
		parseTrace(filename);
	}

	private void parseTrace(String filename) {
		System.out.println("==============================");
		System.out.println("Reading trace from " + filename);

		/*
		 * This parser works for the following csv file structure.
		 * 
		 * groupID, startTime, endTime, duration, boolean (true/false)
		 * 
		 * groupID - describes the group id of the nodes that are affected
		 * 
		 * startTime - the time when the event should occur
		 * 
		 * endTime - Insanity Check
		 * 
		 * duration - how long should the event take
		 * 
		 * interChrunInterval - defines the time between each leave or join
		 * action in this group and this event. This is used as it makes the
		 * behaviour more realistic compared to a simple online/offline at the
		 * same time action.
		 * 
		 * boolean - defines if the group should be online or offline during this event
		 */
		BufferedReader csv = null;
		boolean entrySuccessfullyRead = false;
		int lines = 0;

		try {
			csv = new BufferedReader(new FileReader(filename));

			String previousGroupId = "";
			boolean firstGroup = true;
			long previousEndTime = 0;
			
			while (csv.ready()) {
				String line = csv.readLine();
				lines++;

				if (line.indexOf(SEP) > -1) {
					String[] parts = line.split(SEP);

						if (parts.length == 4) {
							try {
								String groupID = parts[0].replaceAll("\\s+","");
//								int a = DefaultConfigurator.parseNumber("20m", Integer.class);
								long startTime = DefaultConfigurator.parseNumber(parts[1].replaceAll("\\s+",""), Long.class);
								long endTime = DefaultConfigurator.parseNumber(parts[2].replaceAll("\\s+",""), Long.class);
								long duration = DefaultConfigurator.parseNumber(parts[3].replaceAll("\\s+",""), Long.class);
//								boolean online = Boolean.parseBoolean(parts[4].replaceAll("\\s+", ""));		
								
//								String onlineString = parts[5].replaceAll("\\s+","");
								
								if(firstGroup){
									previousGroupId = groupID;
//									previousEndTime = startTime;
									firstGroup = false;
								}
								
								// new group id
								if(!previousGroupId.equals(groupID)){
									previousGroupId = groupID;
//									previousEndTime = startTime;
									previousEndTime = 0;
								}
									
								// Insanity Checks
//								if(startTime != previousEndTime){
								if(startTime < previousEndTime){
									throw new AssertionError("Wrong time in CSV for startTime as previousEndTime is larger.");
								}
								if(endTime - startTime != duration){
									throw new AssertionError("Duration must be the same as endTime - startTime");
								}
//								if((onlineString.equals("true") && !online)|| (onlineString.equals("false") && online)){									
//									throw new AssertionError("Boolean should be the same!");								
//								}
								
								LinkedList<ChurnInfo> infoList = churnInfos.get(groupID);
								if(infoList == null){
									infoList = new LinkedList<CSVBasedChurnModel.ChurnInfo>();
//									infoList.add(new ChurnInfo(startTime, online, endTime));
									infoList.add(new ChurnInfo(startTime, endTime));
									churnInfos.put(groupID, infoList);
								}
								else{
//									infoList.add(new ChurnInfo(startTime, online, endTime));
									infoList.add(new ChurnInfo(startTime, endTime));
								}
								
								entrySuccessfullyRead = true;

							} catch (NumberFormatException e) {
								// Ignore leading comments
								if (entrySuccessfullyRead) {
									// System.err.println("CSV ParseError " +
									// line);
								}
							}
						} else {
							throw new AssertionError("To many/few columns in CSV.");
						}
					}
				}
			
		}
		catch (Exception e) {
			System.err.println("Could not open " + filename);
			throw new RuntimeException("Could not open " + filename);
		}
		finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					//
				}
			}
		}

	}
	
	
	private class ChurnInfo {
	
		long startTime;
		long endTime;
//		boolean online;
				
		public ChurnInfo(long startTime, long endTime){
//		public ChurnInfo(long startTime, boolean online, long endTime){
			this.startTime = startTime;
//			this.online = online;
			this.endTime = endTime;
		}
		
		public long getEndTime(){
			return endTime;
		}
		public long getDuration(){
			// TODO introduce skew/offset
			return (endTime - startTime);
		}
		
		public long getStartTime(){
			return startTime;
		}

	}

}
