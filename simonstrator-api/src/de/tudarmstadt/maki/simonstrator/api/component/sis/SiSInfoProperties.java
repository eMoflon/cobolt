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

import de.tudarmstadt.maki.simonstrator.api.Time;



/**
 * A description object that characterizes a given information in the SiS. This
 * is utilized for both: descriptions as well as requests of data (e.g., the
 * {@link SiSRequest} extends this interface). Both share common
 * QoS-characteristics modeled in this interface.
 * 
 * The interface supports chaining.
 * 
 * Remember to alter the clone() method, when adding new properties!
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class SiSInfoProperties implements Cloneable {
	
	/**
	 * "null" properties.
	 */
	public static final SiSInfoProperties NONE = new SiSInfoProperties();

	private SiSScope scope = null;

	private Class<?> sourceComponent = null;

	private long lastUpdateTimestamp = Time.getCurrentTime();

	/*
	 * Here, we should collect some ideas for common description properties.
	 */

	/*
	 * TODO cost to get this information OR cost we are willing to spend
	 */

	/*
	 * TODO accuracy of the information
	 */

	public void setLastUpdateTimestamp() {
		this.lastUpdateTimestamp = Time.getCurrentTime();
	}

	public long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	/**
	 * Later, we might want to define scopes in a more flexible way?
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public enum SiSScope {
		NODE_LOCAL, REGIONAL, GLOBAL
	}

	/**
	 * Set the scope of the respective information
	 * 
	 * @param scope
	 * @return
	 */
	public SiSInfoProperties setScope(SiSScope scope) {
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
	public <T> SiSInfoProperties setSourceComponent(
			Class<T> sourceComponent) {
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

	@Override
	public SiSInfoProperties clone() {
		SiSInfoProperties prop = new SiSInfoProperties();
		prop.setScope(scope);
		prop.setSourceComponent(sourceComponent);
		return prop;
	}

	@Override
	public String toString() {
		return "SiSInfoProperties ["
				+ (scope != null ? "scope=" + scope + ", " : "")
				+ (sourceComponent != null ? "sourceComponent="
						+ sourceComponent : "") + "]";
	}

}
