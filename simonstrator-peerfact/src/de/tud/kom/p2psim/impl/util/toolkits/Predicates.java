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


package de.tud.kom.p2psim.impl.util.toolkits;

import de.tud.kom.p2psim.api.common.HostProperties;
import de.tud.kom.p2psim.api.common.SimHost;

/**
 * Some predicate definitions.
 * 
 * @author Tobias Lauinger <tl1003@rbg.informatik.tu-darmstadt.de>
 */
public class Predicates {

	/**
	 * A predicate that is true for everything.
	 */
	private static final Predicate NO_FILTER = new Predicate() {
		@Override
		public final boolean isTrue(final Object object) {
			return true;
		}
	};

	/**
	 * @return a predicate that is true for everything.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> getFilterNothing() {
		return NO_FILTER;
	}

	/**
	 * A predicate that is false for everything.
	 */
	private static final Predicate ALL_FILTER = new Predicate() {
		@Override
		public final boolean isTrue(final Object o) {
			return false;
		}
	};

	/**
	 * @return a predicate that is false for everything.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> getFilterEverything() {
		return ALL_FILTER;
	}

	/**
	 * A predicate that combines two predicates in a logical AND.
	 * 
	 * @author Tobias Lauinger <tl1003@rbg.informatik.tu-darmstadt.de>
	 */
	public static class AndPredicate<T> implements Predicate<T> {

		/**
		 * The two predicates (conditions) that must both return true in order
		 * for this predicate to return true.
		 */
		private final Predicate<? super T> condition1, condition2;

		/**
		 * Constructs a new AndPredicate that returns true iff both
		 * <code>predicate1</code> and <code>predicate2</code> return true.
		 * <code>predicate1</code> is tested first.
		 * 
		 * @param predicate1
		 *            the first predicate that must hold for elements on which
		 *            this predicate returns true.
		 * @param predicate2
		 *            the second predicate that must hold for elements on which
		 *            this predicate returns true.
		 */
		public AndPredicate(final Predicate<? super T> predicate1,
				final Predicate<? super T> predicate2) {
			condition1 = predicate1;
			condition2 = predicate2;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final boolean isTrue(final T object) {
			if (condition1.isTrue(object) && condition2.isTrue(object)) {
				return true;
			}
			return false;
		}

	}

	public static final Predicate<SimHost> IS_CHURN_AFFECTED = new Predicate<SimHost>() {
		public boolean isTrue(SimHost host) {
			HostProperties properties = host.getProperties();
			return properties.isChurnAffected();
		}
	};

}
