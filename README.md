<!--
Copyright (c) 2010 Yahoo! Inc., 2012 - 2016 YCSB contributors.
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You
may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. See accompanying
LICENSE file.
-->

Yahoo! Cloud System Benchmark (YCSB)
====================================

Install redis, Java and Maven
--------------------------------


Start Server
--------------------------------

use Script Server.sh but check https://github.com/mdsalmeida92/SearchableEncryptedRedis/blob/master/README.md for redis and replicas startup

**$server_port** : server port
**$redis_port**: redis database port
**$type**: type of server -> "bft" for BFT server nothing if otherwise

    ./Server.sh $server_port $redis_port $type
    
examples:

    run server on port 8443 with redis on port 6380:
    ./Server.sh 8443 6380
    
    run BFTserver on port 8443:
    ./Server.sh 8443 6379 bft

Load data and run tests
--------------------------------

Set the server host and port:

server_host="redis.host="$1
server_port="redis.port="$2

Load the data:

    ./bin/ycsb load redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/all -p $server_host -p $server_port > outputLoad.txt

Run the workload test:
No encryption

    ./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port > outputRun.txt
    
Homomorphic encryption

    ./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=E" > outputRun.txt
    
Homomorphic encryption + onion encryption

    ./bin/ycsb run redis -jvm-args="-Djavax.net.ssl.trustStore=client.jks -Djavax.net.ssl.trustStorePassword=changeme" -s -P workloads/$test -p $server_host -p $server_port -p "redis.encryption=EE" > outputRun.txt
    
Scripts for running tests
--------------------------------

runs every workload in folder /workloads in server with ip 137.74.92.78 and port 8443

    ./Benchmark.sh 137.74.92.78 8443 normal 137.74.92.78

runs workload "inserts" in folder /workloads in server with ip 137.74.92.78 and port 8443

    ./BenchmarkXTest.sh 137.74.92.78 8443 normal 137.74.92.78 inserts

BFT test
runs every workload in folder /workloads for a BFTServer with ip 137.74.92.78 and port 8443

    ./Benchmark_bft.sh 137.74.92.78 8443 bft 137.74.92.78 4
    
runs workload "inserts" in folder /workloads for a BFTServer with ip 137.74.92.78 and port 8443

    ./BenchmarkXTest_bft.sh 137.74.92.78 8443 bft 137.74.92.78 4 inserts






