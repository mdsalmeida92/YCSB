#!/bin/bash

redis_cli=redis-cli
server_host="redis.host="$1
server_port="redis.port="$2

serverType=$3
redis_host=$4
test=$5

echo $server_host
echo $server_port


$redis_cli -h $redis_host -p 6380 flushall

./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/all -p $server_host -p $server_port > outputLoad.txt


./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port > BenchmarkOutput/$test/$serverType"_normal".txt


$redis_cli -h $redis_host -p 6380 flushall

./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/all -p $server_host -p $server_port -p "redis.encryption=E" > outputLoad.txt


./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=E" > BenchmarkOutput/$test/$serverType"_E".txt


$redis_cli -h $redis_host -p 6380 flushall

./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/all -p $server_host -p $server_port -p "redis.encryption=EE" > outputLoad.txt


./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=EE" > BenchmarkOutput/$test/$serverType"_EE".txt


$redis_cli -h $redis_host -p 6380 flushall

