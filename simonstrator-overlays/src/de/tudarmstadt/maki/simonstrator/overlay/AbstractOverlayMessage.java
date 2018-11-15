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


package de.tudarmstadt.maki.simonstrator.overlay;

import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.overlay.api.OverlayMessage;


/**
 * Abstract base for all Overlay-Messages. This class enables support for an
 * {@link OperationAnalyzer} to filter messages that belong to one operation by
 * passing the OperationID along on each hop.
 * 
 * To use this functionality, you are encouraged to use the
 * "forward"-Constructor (that is the one where you pass the receivedMsg).
 * Otherwise, you would have to manually pass the ID in your overlay code which
 * will likely lead to hard to find bugs. This is generally a good idea with
 * regard to message forwarding and reply scenarios: provide each message with a
 * constructor that is used for forwarding and a constructor that is used for
 * replies (where you pass the original query message). This way you are also
 * able to increment a HopCount unified within the message rather than on
 * different places throughout your overlay.
 * 
 * <b>All fields and methods that are prefixed by an underscore are solely to be
 * used by analyzers and not within your applications code!</b>
 * 
 * 
 * @author unknown & Bjoern Richerzhagen
 * @version 1.0, 30.03.2012
 */
public abstract class AbstractOverlayMessage implements OverlayMessage {

	private static final long serialVersionUID = 1L;

	private OverlayContact sender;

	private OverlayContact receiver;

	private int _operationID;

	protected AbstractOverlayMessage() {
		// for Kryo
	}

	/**
	 * Use this constructor for messages that do not originate from a specific
	 * Operation.
	 * 
	 * @param sender
	 * @param receiver
	 */
	public AbstractOverlayMessage(OverlayContact sender, OverlayContact receiver) {
		this(sender, receiver, -1);
	}

	/**
	 * This is the preferred way to create a new message in the Overlay: by
	 * passing the operation ID that is responsible for the message creation. If
	 * you want to forward a message or reply to a message, you should follow
	 * the guidelines described in the class comment of an
	 * {@link AbstractOverlayMessage}
	 * 
	 * Your extending messages as well as the Overlay itself should never access
	 * the operationID via the methods in {@link AbstractOverlayMessage}, as
	 * these are solely for analyzing.
	 * 
	 * @param sender
	 *            contact of the sender. Optional, if your overlay does not use
	 *            those, set it to null
	 * @param receiver
	 *            contact of the intended receiver. Optional.
	 * @param operationID
	 */
	public AbstractOverlayMessage(OverlayContact sender,
			OverlayContact receiver,
			int operationID) {
		this._operationID = operationID;
		this.sender = sender;
		this.receiver = receiver;
	}

	/**
	 * Base constructor for all forward or reply-constructors - ensures that the
	 * operation ID is set according to the received message.
	 * 
	 * @param sender
	 * @param receiver
	 * @param receivedMsg
	 */
	public AbstractOverlayMessage(OverlayContact sender,
			OverlayContact receiver,
			AbstractOverlayMessage receivedMsg) {
		this(sender, receiver, receivedMsg._getOperationID());
	}

	/**
	 * The uniqueID of the intended receiver (Overlay ID) or null, if not
	 * specified.
	 * 
	 * @return
	 */
	public OverlayContact getReceiver() {
		return this.receiver;
	}

	/**
	 * The uniqueID (overlay ID) of the sender or null, if not specified.
	 * 
	 * @return
	 */
	public OverlayContact getSender() {
		return this.sender;
	}

	@Override
	public long getSize() {
		long size = 0;
		if (sender != null) {
			size += sender.getTransmissionSize();
		}
		if (receiver != null) {
			size += receiver.getTransmissionSize();
		}
		return size;
	}

	/**
	 * This method is only to be used by an Analyzer!
	 * 
	 * @return
	 */
	public int _getOperationID() {
		return this._operationID;
	}

}
