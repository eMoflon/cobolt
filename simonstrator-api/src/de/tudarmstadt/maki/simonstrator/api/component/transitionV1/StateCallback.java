package de.tudarmstadt.maki.simonstrator.api.component.transitionV1;

public final class StateCallback {
	private final TransitionEnabled component;
	private final CallbackTarget target;
	private final boolean isSource;
	private final boolean isTarget;

	private boolean called = false;

	public StateCallback(TransitionEnabled component, CallbackTarget target,
			boolean isSource, boolean isTarget) {
		this.component = component;
		this.target = target;
		this.isSource = isSource;
		this.isTarget = isTarget;

		assert isSource != isTarget;
	}

	public void success() {
		if (called)
			throw new RuntimeException(
					"Transition StateCallback invoked twice.");
		called = true;

		target.success(component, isSource, isTarget);
	}

	public void fail(Exception e) {
		if (called)
			throw new RuntimeException(
					"Transition StateCallback invoked twice.");
		called = true;

		target.fail(component, isSource, isTarget, e);
	}
}