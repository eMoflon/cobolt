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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public interface KSortedLookupList<K, V> {

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
	 *                the new key to be put into the map.
	 * @param value
	 *                the new value to be put into the map under the given key.
	 * @param allowUpdate
	 *                whether the new value is allowed to overwrite an old value
	 *                if <code>key</code> is already contained in this map.
	 * @return true if the new key-value mapping has been put into the map,
	 *         false if the underlying map has not been altered.
	 */
	public abstract boolean put(final K key, final V value);

	/**
	 * Puts the given key and value into this map according to the contract of
	 * {@link #put(Object, Object)} except that the value of an existing key may
	 * only be updated if <code>allowUpdate=true</code>.
	 * 
	 * @param key
	 *                the new key to be put into the map.
	 * @param value
	 *                the new value to be put into the map under the given key.
	 * @param allowUpdate
	 *                whether the new value is allowed to overwrite an old value
	 *                if <code>key</code> is already contained in this map.
	 * @return true if the new key-value mapping has been put into the map,
	 *         false if the underlying map has not been altered.
	 */
	public abstract boolean put(final K key, final V value, final boolean allowUpdate);

	/**
	 * Puts the given key and value into this map according to the contract of
	 * {@link #put(Object, Object)} with the tightened restriction that a
	 * key-value mapping can potentially be added only if the Predicate
	 * <code>keyPredicate</code> holds for <code>key</code> and the
	 * Predicate <code>valuePredicate</code> holds for <code>value</code>.
	 * 
	 * @param key
	 *                the key to be added.
	 * @param value
	 *                the value to be added.
	 * @param keyPredicate
	 *                a Predicate on <code>key</code> that has to hold if the
	 *                key-value mapping is to be added. If
	 *                <code>keyPredicate</code> is <code>null</code>, all
	 *                keys are permitted.
	 * @param valuePredicate
	 *                a Predicate on <code>value</code> that has to hold if
	 *                the key-value mapping is to be added. If
	 *                <code>valuePredicate</code> is <code>null</code>, all
	 *                values are permitted.
	 * @return whether the key-value mapping has been added (that is, if
	 *         <code>keyPredicate</code> or <code>valuePredicate</code>
	 *         evaluate to <code>false</code> for <code>key</code> or
	 *         <code>value</code> respectively, <code>false</code> will be
	 *         returned).
	 */
	public abstract boolean put(final K key, final V value, final Predicate<? super K> keyPredicate, final Predicate<? super V> valuePredicate);

	/**
	 * Adds the key-value mappings in the Map <code>mappings</code> to this
	 * map according to the contract of {@link #put(Object, Object)} and in the
	 * order of the iterator of <code>mappings</code>' entry set.
	 * 
	 * @param mappings
	 *                a Map containing the entries to be added.
	 */
	public abstract void putAll(final Map<? extends K, ? extends V> mappings);

	/**
	 * Adds the key-value mappings in the Map <code>mappings</code> to this
	 * map according to the contract of
	 * {@link #put(Object, Object, Predicate, Predicate)} and in the order of
	 * the iterator of <code>mappings</code>' entry set. Each key of
	 * <code>mappings</code> and each value are tested individually with
	 * <code>keyPredicate</code> and <code>valuePredicate</code>
	 * respectively and can be added only if both predicates hold.
	 * 
	 * @param mappings
	 *                a Map containing the entries to be added.
	 * @param keyPredicate
	 *                a Predicate on <code>key</code> that has to hold if a
	 *                key-value mapping is to be added. If
	 *                <code>keyPredicate</code> is <code>null</code>, all
	 *                keys are permitted.
	 * @param valuePredicate
	 *                a Predicate on <code>value</code> that has to hold if a
	 *                key-value mapping is to be added. If
	 *                <code>valuePredicate</code> is <code>null</code>, all
	 *                values are permitted.
	 */
	public abstract void putAll(final Map<? extends K, ? extends V> mappings, final Predicate<? super K> keyPredicate, final Predicate<? super V> valuePredicate);

	/**
	 * Puts the given collection of keys into this map according to the contract
	 * of {@link #put(Object, Object)}. The values will be set to
	 * <code>null</code>. The keys are added in the order of the collection's
	 * iterator.
	 * 
	 * @param keys
	 *                a Collection that contains the keys that should be added
	 *                (and associated the <code>null</code> value).
	 */
	public abstract void putAll(final Collection<? extends K> keys);

	/**
	 * Puts the given collection of keys into this map according to the contract
	 * of {@link #put(Object, Object, boolean)}. The values will be set to
	 * <code>defaultValue</code>. The keys are added in the order of the
	 * collection's iterator.
	 * 
	 * @param keys
	 *                a Collection that contains the keys that should be added
	 *                (and associated the <code>defaultValue</code>).
	 * @param defaultValue
	 *                the value that will be inserted for all new keys.
	 * @param allowUpdate
	 *                whether the new value is allowed to overwrite an old value
	 *                if <code>key</code> is already contained in this map.
	 */
	public abstract void putAll(final Collection<? extends K> keys, final V defaultValue, final boolean allowUpdate);

	/**
	 * Puts the given collection of keys into this map according to the contract
	 * of {@link #put(Object, Object, Predicate)}. The values will be set to
	 * <code>null</code>. The keys are added in the order of the collection's
	 * iterator. Each key of <code>keys</code> is tested individually with
	 * <code>keyPredicate</code> and can be added only if the predicate holds.
	 * 
	 * @param keys
	 *                a Collection that contains the keys that should be added
	 *                (and associated the <code>null</code> value).
	 * @param keyPredicate
	 *                a Predicate on the keys contained in <code>keys</code>
	 *                that has to hold if an element of <code>keys</code> is
	 *                to be added. If <code>keyPredicate</code> is
	 *                <code>null</code>, all keys are permitted.
	 */
	public abstract void putAll(final Collection<? extends K> keys, final Predicate<? super K> keyPredicate);

	/**
	 * Determines whether <code>test</code> lies inside the range of this map,
	 * that is, whether it is larger than or equal to the smallest key and
	 * smaller than or equal the largest key of this map.
	 * 
	 * @param test
	 *                the key to be tested.
	 * @return true if <code>test</code> lies inside the (closed) interval
	 *         denoted by the keys of this map.
	 */
	public abstract boolean isInRange(final K test);

	/**
	 * @return an <b>unmodifiable</b> view on the Collection of the values of
	 *         this map.
	 * @see Map#values()
	 */
	public abstract Collection<V> values();

	/**
	 * @return an <b>unmodifiable</b> view on the Set of the keys of this map.
	 * @see Map#keySet()
	 */
	public abstract Set<K> keySet();

	/**
	 * Get the minimal key from this map (with respect to the preset Comparator)
	 * for which <code>valuePredicate</code> holds for its associated value.
	 * 
	 * @param valuePredicate
	 *                a Predicate that must hold for the value of the returned
	 *                key.
	 * @return the minimal key from this map that fulfils the given constraints,
	 *         or <code>null</code> if no satisfying key can be found.
	 */
	public abstract K getMinKey(final Predicate<V> valuePredicate);

	/**
	 * Determines the maximum key of this map with respect to the given
	 * Comparator <code>maxComp</code>. <code>valuePredicate</code> must
	 * hold for the value associated with that key. If two keys from this map
	 * are equal according to <code>maxComp</code>, the key that is smaller
	 * with respect to this map's preset Comparator will be returned.
	 * 
	 * @param maxComp
	 *                a Comparator according to which a maximum key will be
	 *                looked for.
	 * @param valuePredicate
	 *                a Predicate that must hold for the value of the returned
	 *                key.
	 * @return the key from this map that maximises <code>maxComp</code> (and
	 *         minimises this map's preset comparator in the case of a tie), or
	 *         <code>null</code> if this map is empty.
	 */
	public abstract K getMaxKey(final Comparator<? super K> maxComp, final Predicate<? super V> valuePredicate);

	/**
	 * Sets the value associated with all existing keys to <code>newValue</code>.
	 * 
	 * @param newValue
	 *                the new value to be set for each existing key.
	 */
	public abstract void setAllValues(final V newValue);

	/**
	 * @see Map#remove(Object)
	 */
	public abstract V remove(final K key);

	/**
	 * @see Map#containsValue(Object)
	 */
	public abstract boolean containsValue(final V value);

	/**
	 * @see Map#clear()
	 */
	public abstract void clear();

	/**
	 * @see Map#get(Object)
	 */
	public abstract V get(final K key);

	/**
	 * @see Map#containsKey(Object)
	 */
	public abstract boolean containsKey(final K key);

	/**
	 * @see Map#size()
	 */
	public abstract int size();

	/**
	 * @see SortedMap#firstKey()
	 */
	public abstract K firstKey();

	/**
	 * @see SortedMap#lastKey()
	 */
	public abstract K lastKey();

}