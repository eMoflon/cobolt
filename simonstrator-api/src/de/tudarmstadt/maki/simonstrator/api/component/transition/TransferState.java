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

import de.tudarmstadt.maki.simonstrator.api.component.transitionV1.AtomicTransitionStrategy;

/**
 * Annotate fields that you want to automatically migrate during a transition.
 * Usually, this makes sense if you have an abstract base class for your
 * strategies and have some field like, e.g., a reference to a local overlay
 * component that is required by all of those.
 * 
 * If a <strong>field</strong> is marked with @TransferState, but the target
 * component does not also carry a field or a constructor with the annotation
 * and the same type and name, the transfer of the respective value is discarded
 * silently.
 * 
 * This annotation might also be used to annotate a <strong>constructor</strong>
 * - in this case, the respective annotation has to carry the names of the
 * variables that are to be transfered in the order they are used by the
 * constructor.
 * 
 * For more complex transformations of state between different transition
 * enabled components, you need to specify a custom instance of an
 * {@link AtomicTransitionStrategy}.
 * 
 * @author Bjoern Richerzhagen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface TransferState {

	/**
	 * A string array listing the names of transfered variables in order of the
	 * arguments of the constructor. The names are used to determine the getX()
	 * method name - should start with an upper case letter.
	 * 
	 * @return
	 */
	String[] value();

}
