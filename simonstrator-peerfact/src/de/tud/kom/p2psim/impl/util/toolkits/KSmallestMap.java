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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Map-like data structure that keeps at most K entries. If further entries are
 * added, these replace existing entries that are larger with respect to a given
 * comparator.
 * <p>
 * Note that the only guarantee given by this Map-like class is that it contains
 * at most K entries. (The Predicates that can be passed to some methods are
 * only valid for that one method call.)
 * <p>
 * Also note that this class does not implement the {@link Map} interface as
 * some operations have slightly different contracts.
 * 
 * @author Tobias Lauinger <tl1003@rbg.informatik.tu-darmstadt.de>
 */
public final class KSmallestMap<K, V> implements KSortedLookupList<K, V> {

	private final SortedMap<K, V> kSmallest;

	private final int k;

	/**
	 * Constructs a new KSmallestMap with the maximum capacity of <code>k</code>
	 * .
	 * 
	 * @param k
	 *            the maximum number of key-value mappings that this map will
	 *            store.
	 * @param comp
	 *            a Comparator on the keys of this map. May not be
	 *            <code>null</code>.
	 */
	public KSmallestMap(final int k, final Comparator<K> comp) {
		this.k = k;
		this.kSmallest = new TreeMap<K, V>(comp);
	}

	public String toString() {
		return kSmallest.toString();
	}

	/**
	 * Puts the given key-value mapping into the map.
	 * <p>
	 * If this map contains less than k entries, the new key-value mapping is
	 * added. Similarly, if the new key is already contained in this map, its
	 * value will be updated. Else, if this map already contains k entries, the
	 * new value is added if its key is smaller (with respect to the preset
	 * comparator) than an existing key. The largest existing key and its value
	 * will be evicted from this map. If the new key is larger than all existing
	 * keys, this method will not alter the underlying map.
	 * 
	 * @param key
	 *            the new key to be put into the map.
	 * @param value
	 *            the new value to be put into the map under the given key.
	 * @param allowUpdate
	 *            whether the new value is allowed to overwrite an old value if
	 *            <code>key</code> is already contained in this map.
	 * @return true if the new key-value mapping has been put into the map,
	 *         false if the underlying map has not been altered.
	 */
	public boolean put(final K key, final V value) {
		final K largest;
		if (kSmallest.size() < k) {
			// still places left
			kSmallest.put(key, value);
			return true;
		} else if (kSmallest.containsKey(key)) {
			// update value
			kSmallest.put(key, value);
			return true;
		} else if (kSmallest.comparator().compare(
				(largest = kSmallest.lastKey()), key) > 0) {
			// key is smaller than largest entry of kSmallest: replace
			kSmallest.remove(largest);
			kSmallest.put(key, value);
			return true;
		}
		return false; // contact not added (too large)
	}

	/**
	 * Puts the given key and value into this map according to the contract of
	 * {@link #put(Object, Object)} except that the value of an existing key may
	 * only be updated if <code>allowUpdate=true</code>.
	 * 
	 * @param key
	 *            the new key to be put into the map.
	 * @param value
	 *            the new value to be put into the map under the given key.
	 * @param allowUpdate
	 *            whether the new value is allowed to overwrite an old value if
	 *            <code>key</code> is already contained in this map.
	 * @return true if the new key-value mapping has been put into the map,
	 *         false if the underlying map has not been altered.
	 */
	public boolean put(final K key, final V value, final boolean allowUpdate) {
		if (!allowUpdate && kSmallest.containsKey(key)) {
			return false;
		}
		return put(key, value);
	}

	/**
	 * Puts the given key and value into this map according to the contract of
	 * {@link #put(Object, Object)} with the tightened restriction that a
	 * key-value mapping can potentially be added only if the Predicate
	 * <code>keyPredicate</code> holds for <code>key</code> and the Predicate
	 * <code>valuePredicate</code> holds for <code>value</code>.
	 * 
	 * @param key
	 *            the key to be added.
	 * @param value
	 *            the value to be added.
	 * @param keyPredicate
	 *            a Predicate on <code>key</code> that has to hold if the
	 *            key-value mapping is to be added. If <code>keyPredicate</code>
	 *            is <code>null</code>, all keys are permitted.
	 * @param valuePredicate
	 *            a Predicate on <code>value</code> that has to hold if the
	 *            key-value mapping is to be added. If
	 *            <code>valuePredicate</code> is <code>null</code>, all values
	 *            are permitted.
	 * @return whether the key-value mapping has been added (that is, if
	 *         <code>keyPredicate</code> or <code>valuePredicate</code> evaluate
	 *         to <code>false</code> for <code>key</code> or <code>value</code>
	 *         respectively, <code>false</code> will be returned).
	 */
	public boolean put(final K key, final V value,
			final Predicate<? super K> keyPredicate,
			final Predicate<? super V> valuePredicate) {
		final boolean keyOkay = (keyPredicate == null || keyPredicate
				.isTrue(key));
		final boolean valueOkay = (valuePredicate == null || valuePredicate
				.isTrue(value));

		if (keyOkay && valueOkay) {
			return put(key, value);
		}
		return false;
	}

	/**
	 * Adds the key-value mappings in the Map <code>mappings</code> to this map
	 * according to the contract of {@link #put(Object, Object)} and in the
	 * order of the iterator of <code>mappings</code>' entry set.
	 * 
	 * @param mappings
	 *            a Map containing the entries to be added.
	 */
	public void putAll(final Map<? extends K, ? extends V> mappings) {
		for (final Map.Entry<? extends K, ? extends V> entry : mappings
				.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds the key-value mappings in the Map <code>mappings</code> to this map
	 * according to the contract of
	 * {@link #put(Object, Object, Predicate, Predicate)} and in the order of
	 * the iterator of <code>mappings</code>' entry set. Each key of
	 * <code>mappings</code> and each value are tested individually with
	 * <code>keyPredicate</code> and <code>valuePredicate</code> respectively
	 * and can be added only if both predicates hold.
	 * 
	 * @param mappings
	 *            a Map containing the entries to be added.
	 * @param keyPredicate
	 *            a Predicate on <code>key</code> that has to hold if a
	 *            key-value mapping is to be added. If <code>keyPredicate</code>
	 *            is <code>null</code>, all keys are permitted.
	 * @param valuePredicate
	 *            a Predicate on <code>value</code> that has to hold if a
	 *            key-value mapping is to be added. If
	 *            <code>valuePredicate</code> is <code>null</code>, all values
	 *            are permitted.
	 */
	public void putAll(final Map<? extends K, ? extends V> mappings,
			final Predicate<? super K> keyPredicate,
			final Predicate<? super V> valuePredicate) {
		for (final Map.Entry<? extends K, ? extends V> entry : mappings
				.entrySet()) {
			put(entry.getKey(), entry.getValue(), keyPredicate, valuePredicate);
		}
	}

	/**
	 * Puts the given collection of keys into this map according to the contract
	 * of {@link #put(Object, Object)}. The values will be set to
	 * <code>null</code>. The keys are added in the order of the collection's
	 * iterator.
	 * 
	 * @param keys
	 *            a Collection that contains the keys that should be added (and
	 *            associated the <code>null</code> value).
	 */
	public void putAll(final Collection<? extends K> keys) {
		for (final K key : keys) {
			put(key, null);
		}
	}

	/**
	 * Puts the given collection of keys into this map according to the contract
	 * of {@link #put(Object, Object, boolean)}. The values will be set to
	 * <code>defaultValue</code>. The keys are added in the order of the
	 * collection's iterator.
	 * 
	 * @param keys
	 *            a Collection that contains the keys that should be added (and
	 *            associated the <code>defaultValue</code>).
	 * @param defaultValue
	 *            the value that will be inserted for all new keys.
	 * @param allowUpdate
	 *            whether the new value is allowed to overwrite an old value if
	 *            <code>key</code> is already contained in this map.
	 */
	public void putAll(final Collection<? extends K> keys,
			final V defaultValue, final boolean allowUpdate) {
		for (final K key : keys) {
			put(key, defaultValue, allowUpdate);
		}
	}

	/**
	 * Puts the given collection of keys into this map according to the contract
	 * of {@link #put(Object, Object, Predicate)}. The values will be set to
	 * <code>null</code>. The keys are added in the order of the collection's
	 * iterator. Each key of <code>keys</code> is tested individually with
	 * <code>keyPredicate</code> and can be added only if the predicate holds.
	 * 
	 * @param keys
	 *            a Collection that contains the keys that should be added (and
	 *            associated the <code>null</code> value).
	 * @param keyPredicate
	 *            a Predicate on the keys contained in <code>keys</code> that
	 *            has to hold if an element of <code>keys</code> is to be added.
	 *            If <code>keyPredicate</code> is <code>null</code>, all keys
	 *            are permitted.
	 */
	public void putAll(final Collection<? extends K> keys,
			final Predicate<? super K> keyPredicate) {
		for (final K key : keys) {
			put(key, null, keyPredicate, null);
		}
	}

	/**
	 * Determines whether <code>test</code> lies inside the range of this map,
	 * that is, whether it is larger than or equal to the smallest key and
	 * smaller than or equal the largest key of this map.
	 * 
	 * @param test
	 *            the key to be tested.
	 * @return true if <code>test</code> lies inside the (closed) interval
	 *         denoted by the keys of this map.
	 */
	public boolean isInRange(final K test) {
		final Comparator<? super K> comp = kSmallest.comparator();
		if (comp.compare(kSmallest.firstKey(), test) <= 0
				&& comp.compare(test, kSmallest.lastKey()) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * @return an <b>unmodifiable</b> view on the Collection of the values of
	 *         this map.
	 * @see Map#values()
	 */
	public Collection<V> values() {
		return Collections.unmodifiableCollection(kSmallest.values());
	}

	/**
	 * @return an <b>unmodifiable</b> view on the Set of the keys of this map.
	 * @see Map#keySet()
	 */
	public Set<K> keySet() {
		return Collections.unmodifiableSet(kSmallest.keySet());
	}

	/**
	 * Get the minimal key from this map (with respect to the preset Comparator)
	 * for which <code>valuePredicate</code> holds for its associated value.
	 * 
	 * @param valuePredicate
	 *            a Predicate that must hold for the value of the returned key.
	 * @return the minimal key from this map that fulfils the given constraints,
	 *         or <code>null</code> if no satisfying key can be found.
	 */
	public K getMinKey(final Predicate<V> valuePredicate) {
		for (final Map.Entry<K, V> entry : kSmallest.entrySet()) {
			if (valuePredicate.isTrue(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Determines the maximum key of this map with respect to the given
	 * Comparator <code>maxComp</code>. <code>valuePredicate</code> must hold
	 * for the value associated with that key. If two keys from this map are
	 * equal according to <code>maxComp</code>, the key that is smaller with
	 * respect to this map's preset Comparator will be returned.
	 * 
	 * @param maxComp
	 *            a Comparator according to which a maximum key will be looked
	 *            for.
	 * @param valuePredicate
	 *            a Predicate that must hold for the value of the returned key.
	 * @return the key from this map that maximises <code>maxComp</code> (and
	 *         minimises this map's preset comparator in the case of a tie), or
	 *         <code>null</code> if this map is empty.
	 */
	public K getMaxKey(final Comparator<? super K> maxComp,
			final Predicate<? super V> valuePredicate) {
		K currentMax = null;

		for (final Map.Entry<K, V> entry : kSmallest.entrySet()) {
			if (!valuePredicate.isTrue(entry.getValue())) {
				continue;
			}
			if (currentMax == null
					|| maxComp.compare(currentMax, entry.getKey()) < 0) {
				currentMax = entry.getKey();
			}
		}

		return currentMax;
	}

	/**
	 * Sets the value associated with all existing keys to <code>newValue</code>
	 * .
	 * 
	 * @param newValue
	 *            the new value to be set for each existing key.
	 */
	public void setAllValues(final V newValue) {
		for (final K key : kSmallest.keySet()) {
			kSmallest.put(key, newValue);
		}
	}

	/**
	 * @see Map#remove(Object)
	 */
	public V remove(final K key) {
		return kSmallest.remove(key);
	}

	/**
	 * @see Map#containsValue(Object)
	 */
	public boolean containsValue(final V value) {
		return kSmallest.containsValue(value);
	}

	/**
	 * @see Map#clear()
	 */
	public void clear() {
		kSmallest.clear();
	}

	/**
	 * @see Map#get(Object)
	 */
	public V get(final K key) {
		return kSmallest.get(key);
	}

	/**
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(final K key) {
		return kSmallest.containsKey(key);
	}

	/**
	 * @see Map#size()
	 */
	public int size() {
		return kSmallest.size();
	}

	/**
	 * @see SortedMap#firstKey()
	 */
	public K firstKey() {
		return kSmallest.firstKey();
	}

	/**
	 * @see SortedMap#lastKey()
	 */
	public K lastKey() {
		return kSmallest.lastKey();
	}

}
