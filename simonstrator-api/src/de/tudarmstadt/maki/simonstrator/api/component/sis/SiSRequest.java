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

package de.tudarmstadt.maki.simonstrator.api.component.sis;

import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties.SiSScope;

/**
 * Properties that are exclusive to requests (e.g., a tolerable timeout,
 * thresholds, etc.). This is implemented in the API to ensure compatibility
 * upon interface extensions of the request object.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class SiSRequest implements Cloneable {

	/**
	 * "null" request
	 */
	public static final SiSRequest NONE = new SiSRequest();

	private long timeout = 0;

	private long maxInformationAge = Long.MAX_VALUE;

	private SiSScope scope = null;

	private Class<?> sourceComponent = null;

	public SiSRequest() {
		this(0, Long.MAX_VALUE, null, null);
	}

	public SiSRequest(long timeout, long maxInformationAge, SiSScope scope,
			Class<?> sourceComponent) {
		this.timeout = timeout;
		this.maxInformationAge = maxInformationAge;
		this.scope = scope;
		this.sourceComponent = sourceComponent;
	}

	protected SiSRequest(SiSRequest request) {
		// TODO clone
		this.timeout = request.timeout;
		this.maxInformationAge = request.maxInformationAge;
		this.scope = request.scope;
		this.sourceComponent = request.sourceComponent;
	}

	/**
	 * 
	 * @return
	 */
	public long getTimeout() {
		return timeout;
	}

	public long getMaxInformationAge() {
		return maxInformationAge;
	}
	
	/**
	 * Set the scope of the respective information
	 * 
	 * @param scope
	 * @return
	 */
	public SiSRequest setScope(SiSScope scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * Scope of this request
	 * 
	 * @return scope or null
	 */
	public SiSScope getScope() {
		return scope;
	}

	/**
	 * Optional filter based on the source component(s) of the data (e.g., only
	 * Bypass-Data). This is NOT limited to HostComponents.
	 * 
	 * @param component
	 *            interface of the (desired) source component. This supports
	 *            type inheritance - if you specify e.g., PubSubComponent, you
	 *            will get data from any source extending that interface, so
	 *            from BypassPubSubComponent for example.
	 * 
	 * @return reference to the current properties instance to support chaining
	 */
	public <T> SiSRequest setSourceComponent(Class<T> sourceComponent) {
		this.sourceComponent = sourceComponent;
		return this;
	}

	/**
	 * Source component of this information
	 * 
	 * @return source component or null
	 */
	public Class<?> getSourceComponent() {
		return sourceComponent;
	}

	/*
	 * TODO define request parameters
	 */

	/*
	 * TODO hints on how often this requests will/might occur (esp. if a request
	 * is executed periodically!).
	 */

	@Override
	public SiSRequest clone() {
		return new SiSRequest(this);
	}

	/**
	 * Has to return true, if the given src described by
	 * {@link SiSInfoProperties} satisfies the request.
	 * 
	 * @param src
	 * @return
	 */
	public boolean satisfiableBy(SiSInfoProperties src) {
		if (this.getScope() != null && getScope() != src.getScope()) {
			return false;
		}
		if (getSourceComponent() != null) {
			if (src.getSourceComponent() == null) {
				return false;
			}
			if (!getSourceComponent()
					.isAssignableFrom(src.getSourceComponent())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (maxInformationAge ^ (maxInformationAge >>> 32));
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result
				+ ((sourceComponent == null) ? 0 : sourceComponent.hashCode());
		result = prime * result + (int) (timeout ^ (timeout >>> 32));
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
		SiSRequest other = (SiSRequest) obj;
		if (maxInformationAge != other.maxInformationAge)
			return false;
		if (scope != other.scope)
			return false;
		if (sourceComponent == null) {
			if (other.sourceComponent != null)
				return false;
		} else if (!sourceComponent.equals(other.sourceComponent))
			return false;
		if (timeout != other.timeout)
			return false;
		return true;
	}

}
