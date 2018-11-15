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

package de.tudarmstadt.maki.simonstrator.api.component.transition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that supports state changes within a mechanism (e.g., setting and
 * altering internal variables). This is internally realized as a
 * {@link SelfTransition}, however, the object implementing
 * {@link SelfTransition} is automatically created if this annotation is used,
 * allowing much simpler code for simple state changes.
 * 
 * An example for such a mechanism-state variable might be a gossip parameter
 * within a local dissemination mechanism. Please note: to avoid
 * inconsistencies, the generated code does NOT alter the field directly.
 * Instead, it searches for a setter-method according to the value of this
 * annotation.
 * 
 * @author Bjoern Richerzhagen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface MechanismState {

	/**
	 * The name of the state variable. Used to identify the respective fields. A
	 * method "set[value]" has to be present.
	 * 
	 * @return
	 */
	String value();

}
