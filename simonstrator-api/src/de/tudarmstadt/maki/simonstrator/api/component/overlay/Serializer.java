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

package de.tudarmstadt.maki.simonstrator.api.component.overlay;

import java.io.InputStream;
import java.io.OutputStream;

import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * Can be implemented by an overlay to serialize and de-serialize messages. Has
 * to be registered via the {@link SerializerComponent}. The default fallback is
 * to use Java serilization. If you want to build prototypes with more realistic
 * message sizes, you should provide your own serializers.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface Serializer {

	/**
	 * Has to return the types that are sent via the network. Custom serializers
	 * are provided with the @Se
	 * 
	 * @return
	 */
	public Class<?>[] getSerializableTypes();

	/**
	 * Serialize the given message using the provided output stream. For
	 * convenience, you might of course fill the provided primitive OutputStream
	 * using an ObjectOutputStream - which will, however, increase serialization
	 * size.
	 * 
	 * @param out
	 * @param msg
	 */
	@Deprecated
	public void serialize(OutputStream out, Message msg);

	/**
	 * Recreate a message from the given input stream of bytes. The stream will
	 * contain exactly one {@link Message} belonging to the protocol/overlay
	 * running on the port of this serializer. Your serializer has to ensure
	 * proper identification and instantiation of messages and fields.
	 * 
	 * @param in
	 * @return
	 */
	@Deprecated
	public Message create(InputStream in);

}
