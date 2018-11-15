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


package de.tud.kom.p2psim.impl.util.guirunner.seed;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Taken from gulli:board
 * @author 
 *
 */
public class MD5 {

	    public static String hash(String input) {
	        /* Berechnung */
	        MessageDigest md5;
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
	        md5.reset();
	        md5.update(input.getBytes());
	        byte[] result = md5.digest();

	        /* Ausgabe */
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<result.length; i++) {
	            hexString.append(Integer.toHexString(0xFF & result[i]));
	        }
	        return hexString.toString();
	        
	    }	
}
