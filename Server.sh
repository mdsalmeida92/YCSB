#!/bin/bash

server_port=$1
redis_port=$2
SEC_TYPE=$3

if [[ $SEC_TYPE == "bft" ]]; then
	BFT_ID=$2
	echo "BFT version" 
	java -Djavax.net.ssl.keyStore=Server/server.jks -Djavax.net.ssl.keyStorePassword=changeme -jar Server/AppServer.jar -port $server_port -p $redis_port -bft -id $BFT_ID

else
	echo "normal version"
	java -Djavax.net.ssl.keyStore=Server/server.jks -Djavax.net.ssl.keyStorePassword=changeme -jar Server/AppServer.jar -port $server_port -p $redis_port

fi

