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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.impl.util.Transformer;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Some static helper methods for dealing with Collection objects.
 * 
 * @author Tobias Lauinger <tl1003@rbg.informatik.tu-darmstadt.de>
 * Some methods added by Leo Nobach.
 * 
 */
public class CollectionHelpers {

	/**
	 * Copies all entries from <code>source</code> that have the predicate
	 * <code>filter</code> to <code>dest</code>.
	 * 
	 * @param <T>
	 *            the key type of the maps.
	 * @param <U>
	 *            the value type of the maps.
	 * @param source
	 *            the source map. May not be <code>null</code>.
	 * @param dest
	 *            the destination map. May not be <code>null</code>. If this map
	 *            already contains entries, they might be overwritten by entries
	 *            from <code>source</code> with the same key.
	 * @param filter
	 *            a filter that determines the entries from <code>source</code>
	 *            that are to be put into <code>dest</code>.
	 */
	public static <T, U> void filterMap(
			final Map<? extends T, ? extends U> source,
			final Map<? super T, ? super U> dest,
			final Predicate<? super U> filter) {
		for (final Map.Entry<? extends T, ? extends U> candidate : source
				.entrySet()) {
			if (filter.isTrue(candidate.getValue())) {
				dest.put(candidate.getKey(), candidate.getValue());
			}
		}
	}

	/**
	 * Copies all entries from <code>source</code> that have the predicate
	 * <code>filter</code> to <code>dest</code> in the order in which they are
	 * returned by <code>source</code>'s iterator.
	 * 
	 * @param <T>
	 *            the entry type of the collections.
	 * @param source
	 *            the source collection. May not be <code>null</code>.
	 * @param dest
	 *            the destination collection. May not be <code>null</code>. If
	 *            it already contains entries, the behaviour depends on the add
	 *            implementation of the underlying collection. For example, a
	 *            set prevents duplicate entries, but a list does not.
	 * @param filter
	 *            a filter that determines the entries from <code>source</code>
	 *            that are to be added to <code>dest</code>.
	 */
	// public static <T> void filter(final Collection<? extends T> source,
	// final Collection<? super T> dest, final Predicate<? super T> filter) {
	public static <T> void filter(final Collection<? extends T> source,
			final Collection<? super T> dest, final Predicate<T> filter) {
		for (final T candidate : source) {
			if (filter.isTrue(candidate)) {
				dest.add(candidate);
			}
		}
	}

	/**
	 * Sorts <code>source</code> and adds the first n entries to
	 * <code>dest</code>. If <code>source</code> contains less than n entries,
	 * all of them are added to <code>dest</code>.
	 * 
	 * If adding an entry to <code>dest</code> does not increase the
	 * collection's size, for example if <code>dest</code> is a set and already
	 * contained the inserted contact, an additional entry of
	 * <code>source</code> will be added, if available. This guarantees that
	 * <code>n</code> new, distinct entries are added to collection
	 * <code>dest</code> as long as this can be fulfilled with the contents of
	 * <code>source</code>, and as <code>dest</code> does recognise duplicate
	 * entries. Consequently, this guarantee does not hold for simple lists.
	 * 
	 * Both collections may not be <code>null</code>.
	 * 
	 * @param <T>
	 *            the entry type of the collections.
	 * @param source
	 *            the source collection.
	 * @param dest
	 *            the destination collection.
	 * @param order
	 *            the order in which <code>source</code> is to be sorted.
	 * @param n
	 *            the number of new entries that are to be added to
	 *            <code>dest</code>.
	 */
	public static <T> void copyNSorted(final Collection<? extends T> source,
			final Collection<? super T> dest,
			final Comparator<? super T> order, final int n) {
		final List<? extends T> src = Collections.list(Collections
				.enumeration(source));
		Collections.sort(src, order);
		final Iterator<? extends T> it = src.iterator();
		final int maxEntries = dest.size() + n;
		while (it.hasNext() && dest.size() < maxEntries) {
			dest.add(it.next());
		}
	}

	/**
	 * Sorts <code>source</code> and adds the first n entries that match
	 * <code>filter</code> to <code>dest</code>. If <code>source</code> contains
	 * less than n entries, all of them are added to <code>dest</code>.
	 * 
	 * For details, see
	 * {@link CollectionHelpers#copyNSorted(Collection, Collection, Comparator, int)}
	 * , except that <code>source</code> is filtered using <code>filter</code>
	 * before selecting entries to copy.
	 * 
	 * @param <T>
	 *            the entry type of the collections.
	 * @param source
	 *            the source collection.
	 * @param dest
	 *            the destination collection.
	 * @param filter
	 *            a filter that filters out entries of <code>source</code>
	 *            before sorting.
	 * @param order
	 *            the order in which <code>source</code> is to be sorted.
	 * @param n
	 *            the number of new entries that are to be added to
	 *            <code>dest</code>.
	 */
	// public static <T> void copyNSortedAndFiltered(
	// final Collection<? extends T> source,
	// final Collection<? super T> dest,
	// final Predicate<? super T> filter,
	// final Comparator<? super T> order, final int n) {
	public static <T> void copyNSortedAndFiltered(final Collection<T> source,
			final Collection<T> dest, final Predicate<T> filter,
			final Comparator<T> order, final int n) {
		final List<T> filtered = new ArrayList<T>(source.size());
		filter(source, filtered, filter);
		Collections.sort(filtered, order);
		final Iterator<T> it = filtered.iterator();
		final int maxEntries = dest.size() + n;
		while (it.hasNext() && dest.size() < maxEntries) {
			dest.add(it.next());
		}
	}

	/**
	 * Copies the entries from <code>source</code> to <code>dest1</code> until
	 * <code>dest1</code> has reached size <code>dest1Capacity</code>. The
	 * remaining entries are saved in <code>overflow</code>. The order of
	 * insertion depends on the iterator of the underlying map. Note that as
	 * <code>dest1</code> is a map, inserting an entry with a key that already
	 * exists in the map does not increase its size (if the insertion is
	 * permitted).
	 * 
	 * @param <T>
	 *            the key type.
	 * @param <U>
	 *            the entry/value type.
	 * @param source
	 *            the mappings to be copied.
	 * @param dest1
	 *            the first destination for the entries, to be filled until its
	 *            size reaches dest1Capacity.
	 * @param overflow
	 *            destination for the remaining entries.
	 * @param dest1Capacity
	 *            maximum allowed size for dest1.
	 */
	public static <T, U> void copyUntilFull(
			final Map<? extends T, ? extends U> source,
			final Map<? super T, ? super U> dest1, Map<T, U> overflow,
			final int dest1Capacity) {
		for (final Map.Entry<? extends T, ? extends U> srcEntry : source
				.entrySet()) {
			if (dest1.size() < dest1Capacity) {
				dest1.put(srcEntry.getKey(), srcEntry.getValue());
			} else {
				overflow.put(srcEntry.getKey(), srcEntry.getValue());
			}
		}
	}

	/**
	 * Distributes the contents of <code>source</code> into <code>dest1</code>
	 * and <code>dest2</code> according to the predicate
	 * <code>firstPredicate</code>.
	 * 
	 * All entries that match <code>firstPredicate</code> are inserted into
	 * <code>dest1</code>, the remaining entries are inserted into
	 * <code>dest2</code>.
	 * 
	 * @param source
	 *            the map with the entries that are to be distributed over
	 *            <code>dest1</code> and <code>dest2</code>.
	 * @param dest1
	 *            a Map that will receive all entries from <code>source</code>
	 *            which match <code>firstPredicate</code>.
	 * @param dest2
	 *            a Map that will receive all entries from <code>source</code>
	 *            that do <i>not</i> match <code>firstPredicate</code>.
	 * @param firstPredicate
	 *            a predicate that indicates those entries that are to be saved
	 *            into <code>dest1</code>. All remaining entries, that is those
	 *            that do not match <code>firstPredicate</code>, are saved into
	 *            <code>dest2</code>. Uses the entry to compare, not the key.
	 */
	public static <T, U> void splitMap(
			final Map<? extends T, ? extends U> source,
			final Map<? super T, ? super U> dest1,
			final Map<? super T, ? super U> dest2,
			final Predicate<? super U> firstPredicate) {
		for (final Map.Entry<? extends T, ? extends U> bucketEntry : source
				.entrySet()) {
			if (firstPredicate.isTrue(bucketEntry.getValue()) == true) {
				dest1.put(bucketEntry.getKey(), bucketEntry.getValue());
			} else {
				dest2.put(bucketEntry.getKey(), bucketEntry.getValue());
			}
		}
	}
	
	/**
	 * Returns a random entry from the given collection. Uses the PRNG used by
	 * the simulator. Returns null if the list is empty. 
	 * 
	 * @author Leo Nobach
	 * 
	 * @return
	 */
	public static <E> E getRandomEntry(Collection<E> coll) {
		if (coll instanceof List) return getRandomEntry((List<E>)coll);
		if (coll.size() == 0)
			return null;
		int i = Randoms.getRandom(CollectionHelpers.class).nextInt(coll.size());
		for (E c : coll) {
			if (i <= 0)
				return c;
			i--;
		}
		return null;
	}
	
	/**
	 * Returns a random entry from the given collection. Uses the PRNG used by
	 * the simulator. Returns null if the list is empty. Optimized for lists.
	 * 
	 * @author Leo Nobach
	 * 
	 * @return
	 */
	public static <E> E getRandomEntry(List<E> coll) {
		if (coll.size() == 0)
			return null;
		int i = Randoms.getRandom(CollectionHelpers.class).nextInt(coll.size());
		return coll.get(i);
	}
	
	/**
	 * Randomly returns maxSize elements from in, or less if in is smaller than maxSize.
	 * @param <T>
	 * @param in
	 * @param maxSize
	 * @return
	 */
	public static <T> List<T> getRandomPartFrom(Collection<? extends T> in, int maxSize) {
		List<T> result = new ArrayList<T>();
		result.addAll(in);
		Collections.shuffle(result, Randoms.getRandom(CollectionHelpers.class));
		if (maxSize < result.size())
			result = result.subList(0, maxSize);
		return result;
	}
	
	/**
	 * Transforms a list into another one, element by element, given a Transformer.
	 * @param <Ts>
	 * @param <Tt>
	 * @param sourceList
	 * @param transformer
	 * @return
	 */
	public static <Ts, Tt> List<Tt> transformList(List<Ts> sourceList, Transformer<Ts, Tt> transformer) {
		List<Tt> result = new ArrayList<Tt>(sourceList.size());
		for (Ts s : sourceList) result.add(transformer.transform(s));
		return result;
	}
	
	public static double getQuantile(Collection<Double> l, double alpha) {
		List<Double> sorted = new ArrayList<Double>(l);
		Collections.sort(sorted);
		int size = sorted.size();

		if (size == 0)
			return Double.NaN;

		double kDouble = (size-1) * alpha;
		int k = (int) Math.floor(kDouble);
		double g = kDouble - k;

		double lowerValue = sorted.get(k);

		if (sorted.size() <= k + 1)
			return lowerValue;

		double upperValue = sorted.get(k + 1);
		
		return (1 - g) * lowerValue + g * upperValue;

	}

	public static <T> T getFirstOrNull(Collection<T> c) {
		Iterator<T> it = c.iterator();
		return it.hasNext()?it.next():null;
	}

	public static <T> Collection<T> minus(Iterable<T> a, Collection<T> b) {
		Set<T> result = new HashSet<T>();
		for (T e : a) {
			if (!b.contains(e)) result.add(e);
		}
		return result;
	}
	
}







