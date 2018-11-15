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

package de.tud.kom.p2psim.impl.network.realNetworking.messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import de.tudarmstadt.maki.simonstrator.api.Message;

// TODO: Auto-generated Javadoc
/**
 * The Class RealNetworkingMessage.
 */
public class RealNetworkingMessage implements Serializable {

	/** The net msg. */
	private Message netMsg = null;
	
	/**
	 * Instantiates a new real networking message.
	 */
	public RealNetworkingMessage() {
	}
	
    /**
     * Instantiates a new real networking message.
     *
     * @param netMsg the net msg
     */
    public RealNetworkingMessage(Message netMsg) {
		this.netMsg = netMsg;
	}

    /**
     * Write object.
     *
     * @param oos the oos
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
    	
    	RealNetworkingSerializer.serialize(oos, netMsg);

    }
 
	/**
	 * This method is private but it is called using reflection by java
	 * serialization mechanism. It overwrites the default object serialization.
	 * 
	 * <br/><br/><b>IMPORTANT</b>
	 * The access modifier for this method MUST be set to <b>private</b> otherwise {@link java.io.StreamCorruptedException}
	 * will be thrown.
	 *
	 * @param ois the ois
	 * @throws IOException an exception that might occur during data reading
	 * @throws ClassNotFoundException this exception will be raised when a class is read that is
	 * not known to the current ClassLoader
	 */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    	
    	this.netMsg = RealNetworkingDeserializer.deserialize(ois);

    	//        transientString = (String) ois.readObject();
    }
     
    /**
     * This method is called after the deserialization has been done.
     * Here you can make some final touches and do custom stuff with your deserialized instance.
     * The access modifier can be any one you like.
     * 
     * @return a reference to <b>this</b> object
     * @throws ObjectStreamException throw this exception if you figure that something is wrong with your object
     */
    private /*or public od protected or default*/ Object readResolve() throws ObjectStreamException {
        System.out.println(RealNetworkingMessage.class.getName() + ".readResolve");
        System.out.println("Feel free to do custom stuff here.");
//        string += " (object modified in 'readResolve' method)";
        return this;
    }
    
}
