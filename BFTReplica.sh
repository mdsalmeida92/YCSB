#!/bin/bash
trap 'kill $(jobs -p)' EXIT

redis_port=$1
n_replicas=$2
config_path=$3

for ((number=0;number < $n_replicas;number++))
{
	echo $redis_port
	echo $number
	echo $config_path
	java -jar Server/BFTreplica.jar -port $redis_port -id $number -path $config_path &
	((redis_port++))
	PID=$!
	sleep 1
}

sleep 2
while :
do
	echo "Please type something in (^C to quit)"
	read -p "Press enter to continue"
	kill $PID
	echo "You typed: $INPUT_STRING" 
done

