/*
 * Copyright (c) 2005-2010 KOM Multimedia Communications Lab
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

import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

public class NTPClient {
	
	private static final String TIME_SERVER = "ptbtime1.ptb.de";
	
	public static Date getDate() {
		NTPUDPClient client = new NTPUDPClient();
		
		client.setDefaultTimeout(10000);
		
		try {
			client.open();
			
			InetAddress hostAddr = InetAddress.getByName(TIME_SERVER);
			
			TimeInfo info = client.getTime(hostAddr);
			
			NtpV3Packet message = info.getMessage();
			
			TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
			
			client.close();
			
			return rcvNtpTime.getDate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		
		return new Date();		
	}

}
