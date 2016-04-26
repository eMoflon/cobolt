package de.tudarmstadt.maki.modeling.jvlc.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.impl.KTCConstraintViolationEnumeratorImpl;

public class CollectionConstraintViolationEnumerator extends KTCConstraintViolationEnumeratorImpl {

	private Collection<String> constraintViolations = new ArrayList<>();
	
	public void clearConstraintViolationList() {
		this.constraintViolations.clear();
	}
	
	public Collection<String> getConstraintViolationList() {
		return Collections.unmodifiableCollection(constraintViolations);
	}
	
	@Override
	public void reportViolation(final KTCLink link1, final KTCLink link2, final KTCLink link3) {
		this.constraintViolations.add(String.format("%s, %s, %s", link1, link2, link3));
	}
}
