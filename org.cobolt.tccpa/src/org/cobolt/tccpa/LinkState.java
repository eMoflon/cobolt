package org.cobolt.tccpa;

import tccpa.Link;

/**
 * Reflects the possible values of {@link Link#getState()}
 */
public interface LinkState {

	int ACTIVE = 1;

	int UNMARKED = 0;

	int INACTIVE = -1;

}
