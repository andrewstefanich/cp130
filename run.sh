#!/bin/bash

echo 'compiling...'

if mvn clean compile -q ; then

	echo 'compilation successful, launching applications'
	
	xterm -T "EXCHANGE SERVER" -e "mvn exec:java -q -Dexec.mainClass=app.NetExchangeDriver -Djava.net.preferIPv4Stack=true"  &
	sleep 3

	xterm -T "MONITOR" -e "mvn exec:java -q -Dexec.mainClass=app.EventMonitor -Djava.net.preferIPv4Stack=true" &
	sleep 3

	xterm -T "CLIENT" -e "mvn exec:java -q -Dexec.mainClass=app.ExchangeProxyDriver -Djava.net.preferIPv4Stack=true" &
	sleep 3 
	
else
	echo 'Failed to compile. Applications cannot be launched'
	exit 1
	
fi


