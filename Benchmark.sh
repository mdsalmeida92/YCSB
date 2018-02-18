#!/bin/bash

redis_cli=redis-cli
server_host="redis.host="$1
server_port="redis.port="$2

serverType="Server:"$3
redis_host=$4

echo $server_host
echo $server_port


for i in workloads/* ; 
do
test=$(basename $i)
echo $test"_"$serverType"_normal"



$redis_cli -h $redis_host -p 6380 flushall

./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port > outputLoad.txt

./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port > BenchmarkOutput/$test/$test"_"$serverType"_normal".txt

$redis_cli -h $redis_host -p 6380 flushall


./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=E" > outputLoad.txt

./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=E" > BenchmarkOutput/$test/$test"_"$serverType"_E".txt

$redis_cli -h $redis_host -p 6380 flushall


./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=EE" > outputLoad.txt

./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=EE" > BenchmarkOutput/$test/$test"_"$serverType"_EE".txt

$redis_cli -h $redis_host -p 6380 flushall

done

