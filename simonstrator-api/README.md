# The Simonstrator-API

**A note on Maven: you need to export maven libraries within this project. Go to Properties -> Java Build Path -> Order and Export and tick `Maven Dependencies`.**

The Simonstrator-API enables the development of communication protocols and peer-to-peer systems that can run natively on all Java platforms and within simulators. There are only a few, but very important rules and concepts when programming with this API:


## Time
To execute the protocols within an event-based simulator, all time-related methods and calls have to be encapsulated. Within the API, this is done through the `Time` class. This class exposes a static method to get the current time (`Time.getCurrentTime()`) as a `long`. Please note, that this value should only be used for _relative_ calculations. Within your protocol, you always need to do something _after_ `X` seconds from now, not _at_ `X` o'clock.

Now, to support time units within simulations and in the real world, the `Time` class exposes static constants that __have to be used__ whenever you do time related calculations. These are:

* `Time.MICROSECOND`, the highest resolution you can get
* `Time.MILLISECOND`
* `Time.SECOND`
* `Time.MINUTE`
* `Time.HOUR`

Now, assume you want to delete a routing table `entry` after the respective contact has been idle for more than ten seconds. You might have stored the `Time.getCurrentTime()` of the last communication as `entry.lastSeen`. Within your code, you can delete the contact if:

```java
entry.lastSeen + 10 * Time.SECOND < Time.getCurrentTime()
```

`Time` provides some auxiliary functions to print the current time in a human-readable format, which is useful for debugging - have a look at `Time.getFormattedTime()` and its extensions.

## Events and Threads
Related to the timing-issue, all parallelism and concurrency within your protocol also needs to be encapsulated by the API. As a rule of thumb: _never use threads or runnables_ in your code. Now, assume you want to do an action after ten seconds. In Java, a (primitive) approach would be something like this:

```java
class PeriodicStuff extends Thread {
	public void run() {
		Thread.sleep(10000);
		// do something after ten seconds
	}
}
```

When using the Simonstrator-API, you will instead use the `Event` class. This class allows you to execute a given action after some time. The action is provided as a callback directly to the `Event` class:

```java
EventHandler handler = new EventHandler() {
	public void eventOccurred(Object content, int type) {
		// do something
		// use the object/type fields to pass additional data
	}
}
Event.scheduleWithDelay(10*Time.SECOND, handler, null, 0);
```

Notice the signature of `Event.scheduleWithDelay` - besides the delay and the handler that is to be called, you can also pass an arbitrary object as `content` and an `int` as type. This enables easy distinction between different events if a common `EventHandler` is used. In this case, it is good coding practice to define the allowed types, i.e., the values you use for the `type` field, as constants.

When you want to execute a given task periodically, this can of course also be done by always scheduling a new event within the previous `EventHandler`'s `eventOccured` method. However, for convenience, you should also consider extending the `PeriodicOperation` that provides a higher level of abstraction for periodic tasks.

## Communication (Net I/O and Message Serialization)
When designing a communication protocol, you most probably also want to communicate - be it via WiFi, Bluetooth, or any other kind of technology. The Simonstrator-API provides an abstraction layer for message-based communication through the `NetworkComponent` and the `TransportComponent`. The respective APIs are very much related to the default Java Net I/O-interfaces. Their basic usage is described in the _Getting Started_ Section, usually you will bind protocols in `initialize()` of your component.

Currently, only message based protocols are supported - which is perfectly fine for most overlays. The generic interface for a protocol instance is `TransportProtocol`, all currently implemented protocols provide the `MessageBasedTransport` interface allowing you to use convenience methods to send and receive messages. Please note, that you need to bind a `TransportMessageListener` on all message-oriented transport protocols to be notified upon incoming messages.

Within the simulator, messages do not need to be serialized, as only references to the respective objects are passed around. However, as soon as you want to deploy your overlay on real hardware, you need to provide a serialization logic for all messages that are sent within your overlay. This is done by implementing the `Serializer` and registering it on your overlay's port(s) using the `SerializerComponent`. Please refer to existing overlays for best practices on message serialization. If you need to serialize information from the transport layer (e.g., a network address or `TransInfo` within the API), you should refer to the `TransportAssetsSerializer` which is available as a global component via `Binder.getComponent()`.

## Getting Started
First of all, take a look at an existing overlay/protocol - you will notice that they all consist of a few basic building blocks. The most important building block is the implementation of the `OverlayComponent` and the respective factory class. Usually, they are named after the respective overlay, which, in most cases, is also given by the packet name. Examples can be found in the SimonstratorOverlay-Project - if you do not have access to this project, please ask your supervisor. Your project structure will probably look like this:

- [overlay-name]
    - [messages]
         - MessageTypeOne
         - MessageTypeTwo
    - [operations]
         - JoinOperation
         - DoSomethingOperation
    - YourHostComponent
    - YourHostComponentFactory
    - YourHostComponentMessageHandler

The main building blocks are explained in the following.

### Your Overlay Component and Component Factory
Start by creating the `OverlayComponent` (or Node, the names are interchangeable) for your protocol. Usually, it is a good idea to extend existing abstract implementations, for example an `AbstractOverlayNode`, if provided. The interface (or the abstract class) enforce a set of methods that your overlay needs to implement. Consider the respective JavaDocs for further guidance. Usually, you have to implement or overwrite the `initialize()` method as the most important step. Within this method, your overlay should bind all required components:

* Communication interfaces (eg. Transport Protocols on given ports)
* Other overlays (eg. you want to use another overlay as a service)
* Any other `HostComponent` as defined by the API - those can also be sensors, actuators, etc.

To access those other components from within your own component, use the `Host` object that can usually be retrieved via `getHost`. Attention: components are not guaranteed to be available before the `initialize` method has been called. Binding a component within a constructor is therefore not a good idea. The `Host` object provides a generic method to retrieve single components that implement a given API:

```java
public <T extends HostComponent> T getComponent(Class<T> componentClass)
			throws ComponentNotAvailableException;
```

A variant of this method is able to return a list of components, if the interface you provided is implemented by multiple components. As you most likely need to retrieve components that enable network communication, two shorthands within `Host` are provided for that purpose:

```java
public TransportComponent getTransportComponent();
public NetworkComponent getNetworkComponent();
```

Assuming you want to communicate via UDP on a given port, you should bind the respective protocol within the `initialize`-method as follows (note, this method is provided for your convenience within the `AbstractOverlayNode` that is available in the `SimonstratorOverlays` project):

```java
NetInterface net = getHost().getNetworkComponent().getByName(
				NetInterfaceName.WIFI);
UDP udp = host.getTransportComponent().getProtocol(UDP.class,
					net.getLocalInetAddress(), 12345);
```

In the example, we just created a UDP-socket on the wireless interface, listening on (and sending from) port 12345. Just keep a reference to the `UDP` object you just received to send and receive messages:

```java
udp.setTransportMessageListener(/* Your Message Listener here! */);
udp.send(/* [...] */);
```

Usually, you will create a separate class as `MessageHandler`, pass the `UDP` object to that class and register that class as a `TransportMessageListener` as shown in the code snippet. Of course, you are also free to use TCP via the `TCPMessageBased` interface. More details on that are provided in the next section.

In order to create your overlay, you need to provide a factory class that implements `HostComponentFactory`. This interface enforces only a single method: `createComponent(Host host)`, which has to return an instance of your `HostComponent`.

### Message Handler, Messages, and Serialization
As described in the _Communication_ section, you most probably define your own communication protocol on top of a message-based TCP or UDP transport protocol. Assuming you implemented a message handler class as described in the previous section, you are now able to send messages using one of the send-methods:

```java
public int send(Message msg, NetID receiverNet, int receiverPort);

public int sendAndWait(Message msg, NetID receiverNet, int receiverPort, 
	TransMessageCallback senderCallback, long timeout);

public int sendReply(Message reply, NetID receiver, int receiverPort, int commID);
```

In order to simply send a message, you obviously make use of the plain `send` method. As many protocols require the recipient of a message to send an acknowledgement or some kind of answer, there are two additional functions to aid in implementing a _request/reply_ scenario. Using `sendAndWait`, you can send a message and provide a callback and a timeout. If you do not receive an answer (issued via `sendReply` at the receiving node) within the timeout, the callback will fire a failure event. This allows you to retransmit the message, remove the contact from your routing tables, or perform any other kind of error handling. In case you do receive a reply within the timeout, the callback fires a `messageReceived` event. Within this event, you can then process the reply message. Please not, that reply messages are __not__ passed through the `TransportMessageListener` of your central message handler but instead arrive directly within the callback provided for the `sendAndWait` method.

The UDP packet size on real devices is limited by the underlying network interface (MTU). If you want to send larger packets in your overlay, you need to switch to TCP or increase the MTU. For the `RealTransportUDP`, which is part of the `SimonstratorUtils`, this can be achieved through `RealTransportUDP.setMaxDatagramSize()`. Keep in mind that large UDP packets lead to fragmentation on the IP layer and, thus, a higher loss probability for the original message.

# Documentation for other API packages
The respective API subpackages should be documented within the packet. Just include a README.md file containing all relevant information for your overlay.