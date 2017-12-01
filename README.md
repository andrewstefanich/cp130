# stock-exchange-simulator
Simulated stock exchange application; demonstrating concurrency, networking, generics


# USAGE
Execute the bash script </br>
<strong> ./run.sh </strong>

<i>Note:</i>
For launch script to run successfully, you must have xterm command installed.
First verify you have this by entering "which xterm".
If no result is printed to console, you must then install it.
(Linux users should already have this; OSX users might need to download XQuartz)

The script will auto launch three applications in separate windows..</br>

--The first window to open will be your server (the stock exchange) </br>
	this process accepts commands and emits events</br></br>

--The second window to open is a network monitor </br>
	this process will listen to and report events emitted from the exchange</br>
	(e.g. price changes, market open/close notifications) </br></br>

--The third window to open is the client </br>
	this proces will send commands to the server </br>
	(e.g. create account, request quotes, place orders) </br></br>


Follow the on-screen instructions to see the events in action.</br>
Example run through:</br>

1) Open the exchange &emsp;&emsp;&emsp;		(ENTER in server window)</br>
  -monitor should now be displaying price changes </br>
  -the broker will process any queued market orders </br>
	
2) Place stop orders &emsp;&emsp;&emsp;		(ENTER in client window)</br>
	-client orders are transmitted to exchange </br>
	-based on the current price, orders are either queued
	  or processed as market orders  </br>

3) Close the exchange &emsp;&emsp;&emsp;	(ENTER is server window) </br>
	-monitor should no longer be reporting prices </br>
	-client should be notified accordingly </br>

4) Shutdown client	&emsp;&emsp;&emsp;	(ENTER in client window) </br>

5) Shutdown monitor	&emsp;&emsp;&emsp;	(CTRL + C  in monitor window) </br>

6) Shutdown server	&emsp;&emsp;&emsp;	(Q ENTER in server window) </br>

## Documentation
<!-- <a href="https://astefanich.github.io/cp130/"> Javadoc</a> -->

## DIAGRAMS
<i>Class Diagram</i></br>
![class_diagram](img/class_diagram.png?raw=true "Class Diagram")
</br></br>

<i>Broker Initialization</i></br>
![broker_initialization_sequence](img/broker_initialization_sequence.png?raw=true "Broker Initialization")
</br></br>

<i>Price Change Sequence</i></br>
![price_change_sequence](img/price_change_sequence.png?raw=true "Price Change Sequence")
</br></br>

<i>Order Queues</i></br>
![order_queues](img/order_queues.png?raw=true "Order Queues")
</br></br>

<i>Command Sequence</i></br>
![command_sequence](img/command_sequence.png?raw=true "Command Sequence")
</br></br>

<i>Event Sequence</i></br>
![event_sequence](img/event_sequence.png?raw=true "Event Sequence")

