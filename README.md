# stock-exchange-simulator.
Simulated stock exchange application; demonstrating concurrency, networking, generics


# USAGE
Execute the bash script </br>
./run.sh

<i><b>Note:</b></i>
For launch script to run successfully, you must have xterm command installed
First verify you have this by entering "which xterm".
If no result is printed to console, you must then install it.
(Linux users should already have this; OSX users might need to download XQuartz)

The script will auto launch three applications in separate windows..</br>

--The first window to open will be your server (the stock exchange)
	this process accepts commands and emits events

--The second window to open is a network monitor
	this process will listen to and report events emitted from the exchange
	(e.g. price changes, market open/close notifications)

--The third window to open is the client
	this proces will send commands to the server
	(e.g. create account, request quotes, place orders)


Follow the on-screen instructions to see the events in action.
Example run through:

1) Open the exchange 				(ENTER in server window)
  -monitor should now be displaying price changes
  -the broker will process any queued market orders
	
2) Place stop orders 				(ENTER in client window)
	-client orders are transmitted to exchange
	-based on the current price, orders are either queued
	  or processed as market orders

3) Close the exchange 				(ENTER is server window)
	-monitor should no longer be reporting prices
	-client should be notified accordingly

4) Shutdown client					(ENTER in client window)

5) Shutdown monitor					(CTRL + C  in monitor window)

6) Shutdown server					(Q ENTER in server window)

## Documentation
<!-- <a href="https://astefanich.github.io/cp130/"> Javadoc</a> -->

## DIAGRAMS
<i>Class Diagram</i></br>
![class_diagram](img/class_diagram.png?raw=true "Class Diagram")
</br></br>

<i>Order Queues</i></br>
![order_queues](img/order_queues.png?raw=true "Order Queues")
</br></br>

<i>Broker Initialization</i></br>
![broker_initialization_sequence](img/broker_initialization_sequence.png?raw=true "Broker Initialization")
</br></br>

<i>Price Change Sequence</i></br>
![price_change_sequence](img/price_change_sequence.png?raw=true "Price Change Sequence")
</br></br>

<i>Command Sequence</i></br>
![command_sequence](img/command_sequence.png?raw=true "Command Sequence")
</br></br>

<i>Event Sequence</i></br>
![event_sequence](img/event_sequence.png?raw=true "Event Sequence")

