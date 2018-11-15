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

package de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute;


/**
 * We use URIs to define topics - this enables nesting and categorization
 * through prefix matching. A topic is just a special kind of {@link Attribute},
 * if you do not need the added complexity of a content-based pub/sub. It also
 * provides a shortcut to the built-in prefix matching filter.
 * 
 * A Topic is, thus, an URI in the form
 * 
 * <code>/event/transition/trigger</code>
 * 
 * The basic pub/sub component can thus be seen as topic / channel based, but
 * further semantics on top, using attributes (i.e., numerical ranges) are
 * possible as well, leading to a more content-based pub/sub.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface Topic extends Attribute<String>, AttributeFilter<String> {

	public final static String NAME = "topic";

	/**
	 * True, if this topic is a subtopic (sub-URL) of the provided topic t
	 * 
	 * @param t
	 * @return
	 */
	public boolean isIncludedBy(Topic t);

	/**
	 * True, if the topic t is included by this topic.
	 * 
	 * @param t
	 * @return
	 */
	public boolean includes(Topic t);

	@Override
	public Topic clone();

}
