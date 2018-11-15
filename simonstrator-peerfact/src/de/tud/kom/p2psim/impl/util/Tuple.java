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


package de.tud.kom.p2psim.impl.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class Tuple<T1, T2> {

	T1 a;
	T2 b;
	
	public Tuple() {
		
	}
	
	public Tuple(T1 a, T2 b) {
		super();
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String toString() {
		return "(" + a + ", " + b + ")";
	}

	public T1 getA() {
		return a;
	}
	public void setA(T1 a) {
		this.a = a;
	}
	public T2 getB() {
		return b;
	}
	public void setB(T2 b) {
		this.b = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		return true;
	}

	public static <K, V> List<Tuple<K, V>> tupleListFromMap(Map<K, V> map) {
		List<Tuple<K,V>> l = new ArrayList<Tuple<K, V>>(map.size());
		for (Entry<K, V> e : map.entrySet()) {
			l.add(new Tuple<K, V>(e.getKey(), e.getValue()));
		}
		return l;
	}

    public static <K, V> Map<K, V> mapFromTupleList(List<Tuple<K, V>> tupleList) {
        Map<K, V> map = Maps.newLinkedHashMap();
        for (Tuple<K, V> tuple : tupleList) {
            map.put(tuple.getA(), tuple.getB());
        }
        return map;
    }
	
	public static <K, V1, V2> List<Tuple<K, V2>> transformSecondArgumentInList(List<Tuple<K, V1>> sourceList, Transformer<V1, V2> tr) {
		
		List<Tuple<K, V2>> newLst = new ArrayList<Tuple<K, V2>>(sourceList.size());
		for(Tuple<K, V1> s : sourceList) {
			newLst.add(new Tuple<K, V2>(s.getA(), tr.transform(s.getB())));
		}
		return newLst;
	}

	public static <T, U> List<T> getFirstArgumentList(List<Tuple<T, U>> src) {
		List<T> l = new ArrayList<T>(src.size());
		for (Tuple<T, ?> el : src) l.add(el.getA());
		return l;
	}
	
	public static <A> List<Tuple<A, A>> streamTupleList(A... a) {
		
		List<Tuple<A, A>> result = new ArrayList<Tuple<A, A>>();
		
		if (a.length%2 != 0) throw new IllegalArgumentException("The number of arguments must be even, but is " + a.length);
		for (int i = 0; i < a.length; i+=2) {
			result.add(new Tuple<A, A>(a[i], a[i+1]));
		}
		
		return result;
		
	}
	
	public static <A extends Comparable<A>> Comparator<Tuple<A, ?>> getFirstArgumentComparator() {
		return new Comparator<Tuple<A,?>>() {

			@Override
			public int compare(Tuple<A, ?> o1, Tuple<A, ?> o2) {
				return o1.getA().compareTo(o2.getA());
			}
			
		};
	}
	
	public static <B extends Comparable<B>> Comparator<Tuple<?, B>> getSecondArgumentComparator() {
		return new Comparator<Tuple<?, B>>() {

			@Override
			public int compare(Tuple<?, B> o1, Tuple<?, B> o2) {
				return o1.getB().compareTo(o2.getB());
			}
			
		};
	}

	public static <T1, T2> Tuple<T1, T2> create(T1 a, T2 b) {
		return new Tuple<T1, T2>(a, b);
	}
}
