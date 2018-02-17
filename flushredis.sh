/bin/bash

n_redis=$1

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
