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

/**
 * A relation that is equal to another SymmetricRelation, if both elements match, even if they are swapped.
 * @author 
 *
 * @param <T1>
 * @param <T2>
 */
public class SymmetricRelation<T1, T2> {

	T1 a;
	T2 b;
	
	public SymmetricRelation(T1 a, T2 b) {
		super();
		this.a = a;
		this.b = b;
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
		if (a == null && b == null) return 0;
		if (a == null) return prime * b.hashCode();
		if (b == null) return prime * a.hashCode();
		return prime * (a.hashCode() + b.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SymmetricRelation other = (SymmetricRelation) obj;
		if (a == null || b == null) return false;
		return (a.equals(other.a) && b.equals(other.b) || a.equals(other.b) && b.equals(other.a));
	}
	
}
