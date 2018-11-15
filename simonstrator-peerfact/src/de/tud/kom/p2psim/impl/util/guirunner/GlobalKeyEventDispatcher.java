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


package de.tud.kom.p2psim.impl.util.guirunner;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Fängt alle AWT-Key-Events ab und gibt sie an seine KeyListener weiter.
 * 
 * @author
 * @version 3.0, 13.11.2008
 * 
 */
public class GlobalKeyEventDispatcher {

	List<KeyListener> listeners = new ArrayList<KeyListener>();

	Window frame2BActive = null;

	/**
	 * Standard-Konstruktor
	 */
	public GlobalKeyEventDispatcher() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent event) {
				if (event instanceof KeyEvent)
					dispatch((KeyEvent) event);
			}

		}, AWTEvent.KEY_EVENT_MASK);
	}

	/**
	 * GlobalKeyEventDispatcher, der Events nur an die EventListener meldet,
	 * wenn das Fenster frame2BActive im Vordergrund ist (isFocused)
	 * 
	 * @param frame2BActive
	 */
	public GlobalKeyEventDispatcher(Window frame2BActive) {
		this();
		this.frame2BActive = frame2BActive;
	}

	/**
	 * Fügt einen KeyListener hinzu
	 * 
	 * @param l
	 */
	public void addKeyListener(KeyListener l) {
		listeners.add(l);
	}

	void dispatch(KeyEvent event) {
		if (shallDispatch()) {
			if (event.getID() == KeyEvent.KEY_PRESSED)
				for (KeyListener listener : listeners) {
					listener.keyPressed(event);
				}
			else if (event.getID() == KeyEvent.KEY_RELEASED)
				for (KeyListener listener : listeners) {
					listener.keyReleased(event);
				}
			else if (event.getID() == KeyEvent.KEY_TYPED)
				for (KeyListener listener : listeners) {
					listener.keyTyped(event);
				}
		}
	}

	private boolean shallDispatch() {
		return (frame2BActive == null || frame2BActive.isFocused());
	}

}