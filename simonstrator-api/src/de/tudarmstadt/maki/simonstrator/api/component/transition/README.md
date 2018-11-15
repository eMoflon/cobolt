# The Transition Engine

Initially based on work done by Alex Froemmgen, Julius Rueckert and Bjoern Richerzhagen as described in the paper *Towards the Description and Execution of Transitions in Networked Systems*, the current version of the engine is futher optimized for use within the simonstrator environment by overlay and application developers. 
This *new* version of the engine and its concepts is documented in this README file.

## Usage

The `TransitionEngine` provides convenient access to transparent proxies supporting transitions between different realizations of a mechanism. 
To this end, it provides a simple lifecycle management as defined in `TransitionEnabled` as well as convenience functions such as automated state transfer and object creation, relying on `TransferState` annotations. 
While the engine supports transitions out of the box, a developer may register its own implementations of the `AtomicTransition` interface to extend the state transfer (e.g., to include transformations of state variables) or to better react to transitions failures.

### The TransitionEnabled Interface

To use the engine, your *to-be-exchanged* mechanisms need to implement `TransitionEnabled`.
This will add two lifecycle methods to your mechanisms:

```java
public void startMechanism(TransitionEnabled.Callback cb);
public void stopMechanism(TransitionEnabled.Callback cb);
```

Make sure that the provided callback is triggered as soon as your mechanism is ready to operate or has stopped operating, respectively.
It is also recommended to free resources by nullifying pointers in the `stopMechanism` method.
After the call to `stopMechanism`, the transition engine ensures that no method invocations occur on the object anymore.
However, if you scheduled events within the mechanism, these will still fire.


```java
public interface MyMechanism implements TransitionEnabled {

	// Your mechanism's API
	
}
```


### Transfer State

Commonly, you want to keep a reference to the `Host` object our your custom `HostComponent` within your `TransitionEnabled` mechanism.
This can easily be realized with the state transfer mechanism of the transition engine.
Assuming you want to maintain a reference to the `Host` object - so your mechanism has a single public constructor and a getter as shown in the example below:

```java
public class MyMechanismImpl implements MyMechanism {

	private Host host;

	public MyMechanism(Host host) {
		this.host = host;
	}
	
	public Host getHost() {
		return host;
	}
	
}
```

Now, when the transition engine wants to create an instance of this mechanism, it needs to know that `host` is state that is supposed to be transferred between instances.
Therefore, you need to annotate the filed with `TransferState`, specifying the name of the getter that is used to access the respective information:

```java
public class MyMechanismImpl implements MyMechanism {

	@TransferState({ "Host" })
	private Host host;

	@TransferState({ "Host" })
	public MyMechanism(Host host) {
		this.host = host;
	}
	
	public Host getHost() {
		return host;
	}
	
}
```

Note the capital **H** in **Host** - this is required, as the getter is called get**H**ost.
By annotating the constructor, the transition engine is able to resolve the state of host and pass it to the constructor.
If you need to transfer more than one state variable to your constructor, be sure to list them in correct order of the constructor's arguments.
You are not forced to use a constructor to transfer state: instead, you may just provide a setter method for the annotated variable (or both - in that case, the constructor is used):

```java
public class MyMechanismImpl implements MyMechanism {

	@TransferState({ "Host" })
	private Host host;

	public MyMechanism() {
		// nothing
	}
	
	public Host getHost() {
		return host;
	}
	
	public Host setHost() {
		return host;
	}
	
}
```

### Registering a Mechanism

To actually use your transition enabled mechanism, you need to create a mechanism proxy via the transition engine.
Assuming the engine is available as a component on your host object, you create proxies (usually in the `initialize()` method of your component) by calling:

```java
TransitionEngine transitionEngine = host.getComponent(TransitionEngine.class);
MyMechanism proxy = transitionEngine.createMechanismProxy(
		MyMechanism.class, new MyMechanismImpl(host), "MyProxy", null);
```

Note how the transition engine creates a proxy adhering to the `MyMechanism` interface. 
In addition to the interface, you need to specify a default implementation (instance) of the mechanism - this is also the place were the initial state (e.g., a reference to the host object) is set.
Next, you specify a name for the proxy - this is lateron needed to identify your proxy in the transition engine when you plan to execute a transition.


### Triggering a local Transition

Once you created your proxy, triggering a transition is as easy as calling:

```java
transitionEngine.executeAtomicTransition("MyProxy", MyMechanismOtherImplementation.class);
```

You need to specify the name of the proxy that you used during creation (it's a good idea to store that in a static final string field...) as well as the target mechanism implementation.
The actual transition is transparent to your application.
The engine will instantiate the target mechanism, transfer any state information that carries the respective `TransferState` annotation, and then invoke the lifecycle method `startMechanism` on the target mechanism.
Once the callback of that method is called (with success), the engine will call `stopMechanism` on the original (currently used) mechanism.
At the same time, the proxy transparently relays all method invocations to the new component.
The transition is finished, once the `stopMechanism` callback is called.

If the new mechanism fails to start (callback returns on failure), the transition is aborted and the original mechanism remains active.

