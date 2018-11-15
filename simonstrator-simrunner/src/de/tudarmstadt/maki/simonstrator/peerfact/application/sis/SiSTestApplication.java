package de.tudarmstadt.maki.simonstrator.peerfact.application.sis;

import java.util.Random;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;

/**
 * Calling some methods on the SiS, to test basic functions.
 *
 * @author Bjoern Richerzhagen
 *
 */
public class SiSTestApplication implements HostComponent {

	private final Host host;

	private SiSComponent sis;

	private static int lastId = 0;

	private int id = ++lastId;

	protected final Random rnd = Randoms.getRandom(SiSTestApplication.class);

	protected boolean active = false;

	private double dummyValue = 0;

	public SiSTestApplication(Host host) {
		this.host = host;
	}

	@Override
	public void initialize() {
		active = true;
		try {
			sis = host.getComponent(SiSComponent.class);
		} catch (ComponentNotAvailableException e) {
			throw new AssertionError(
					"This application requires a SiS-Component to work.");
		}
	}

	public SiSComponent getSiS() {
		return sis;
	}

	public int getId() {
		return id;
	}

	public double getDummyValue() {
		return dummyValue;
	}

	public void setDummyValue(double dummyValue) {
		this.dummyValue = dummyValue;
	}

	@Override
	public void shutdown() {
		active = false;
		sis = null;
	}

	@Override
	public Host getHost() {
		return host;
	}

	@Override
	public String toString() {
		return "[SiSTestApp-" + id + "]";
	}

	public static class Factory implements HostComponentFactory {

		@Override
		public SiSTestApplication createComponent(Host host) {
			return new SiSTestApplication(host);
		}

	}

}
