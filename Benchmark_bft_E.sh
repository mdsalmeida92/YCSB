#!/bin/bash

redis_cli=redis-cli
server_host="redis.host="$1
server_port="redis.port="$2

serverType=$3
redis_host=$4
x=$5
redis_port=6379

echo $server_host
echo $server_port

for ((n=0;n<$x;n++))
do 
$redis_cli -h $redis_host -p $redis_port flushall
((redis_port++))
done
redis_port=6379

./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/all -p $server_host -p $server_port -p "redis.encryption=E" > outputLoad.txt

for i in workloads/* ; 
do
test=$(basename $i)


./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=E" > BenchmarkOutput/$test/$serverType"_E".txt

done

for ((n=0;n<$x;n++))
do 
$redis_cli -h $redis_host -p $redis_port flushall
((redis_port++))
done
redis_port=6379
