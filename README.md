## Bob Wilsons Garage ##

Table of Contents

 * [What is it](#what)
 * [Description](#description)
 * [Usage](#usage)
 * [Resources](#resources)

<a name="what">
What is it?
--------
</a>
The demo from the **SOA on Steroids** presentation that highlights The Actor Service Registry for Akka.

<a name="description">
Description
--------
</a>

Bob Wilson's Garage is a contrived Akka cluster microservice example.

*We repair your car (if our staff showed up today) and detail it for free!*

<a name="usage">
Usage
--------
</a>

### Install Docker

Install Docker Toolbox: [https://www.docker.com/products/docker-toolbox](https://www.docker.com/products/docker-toolbox "Docker Toolbox")

### Start a 3 node Docker Swarm cluster

```
// start the machine to host the key value store for the cluster
docker-machine create -d virtualbox consul

// switch docker client to the consul machine
eval "$(docker-machine env consul)"

// run the consul container
docker run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap

// start up the swarm-master node
docker-machine create -d virtualbox --swarm --swarm-master --swarm-discovery=consul://$(docker-machine ip consul):8500 --engine-opt="cluster-store=consul://$(docker-machine ip consul):8500" --engine-opt="cluster-advertise=eth1:2376" swarm-master

// start up the swarm-agent-00 node
docker-machine create -d virtualbox --swarm --swarm-discovery=consul://$(docker-machine ip consul):8500 --engine-label instance=java --engine-opt="cluster-store=consul://$(docker-machine ip consul):8500" --engine-opt="cluster-advertise=eth1:2376" swarm-agent-00

// start up the swarm-agent-01 node
docker-machine create -d virtualbox --swarm --swarm-discovery=consul://$(docker-machine ip consul):8500 --engine-label instance=db --engine-opt="cluster-store=consul://$(docker-machine ip consul):8500" --engine-opt="cluster-advertise=eth1:2376" swarm-agent-01

// switch docker client to the swarm master machine
eval $(docker-machine env --swarm swarm-master)

// list the machines
docker-machine ls

// get info on the cluster
docker info

```

### Switch to the project directory

```
cd ~/path/to/project/BobWilsonsGarage
```

### Create an overlay network called 'back'

```
docker network create --driver overlay --subnet=10.0.9.0/24 back
```

### Start a single node cassandra container with ephimeral persistence suitable for testing

```
docker run -d -p 9042:9042 --name cassandra --net=back cassandra

// wait for cassandra to initialize (ctrl+z to stop log tailing)
docker logs -f cassandra

// create the keyspaces
docker exec -it cassandra cqlsh
CREATE KEYSPACE bobwilsonsgaragejournal WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE KEYSPACE bobwilsonsgaragesnapshot WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
exit;
```

### Start sbt 

```
sbt
```

### Publish the docker images 

```
// publish docker images to the swarm cluster
docker:publishLocal

// exit sbt
exit
```

### Run the containers 

```
docker run -d -p 2551:2551  -p 25519:9999 --name backend1 --net=back bobwilsonsgaragebackend:1.0 -Dbobwilsonsgarage.port=2551 -Dbobwilsonsgarage.hostname=backend1 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

docker run -d -p 2552:2552 -p 25529:9999 --name backend2 --net=back bobwilsonsgaragebackend:1.0 -Dbobwilsonsgarage.port=2552 -Dbobwilsonsgarage.hostname=backend2 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

docker run -d -p 2554:2554 -p 25549:9999 --name staffing --net=back bobwilsonsgaragestaffing:1.0 -Dbobwilsonsgarage.port=2554 -Dbobwilsonsgarage.hostname=staffing -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

docker run -d -p 2555:2555 -p 25559:9999 --name detailing --net=back bobwilsonsgaragedetailing:1.0 -Dbobwilsonsgarage.port=2555 -Dbobwilsonsgarage.hostname=detailing -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

docker run -d -p 2556:2556 -p 25569:9999 --name carrepair --net=back bobwilsonsgaragecarrepair:1.0 -Dbobwilsonsgarage.port=2556 -Dbobwilsonsgarage.hostname=carrepai r-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

docker run -d -p 2553:2553 -p 8080:8080 -p 25539:9999 --name frontend --net=back bobwilsonsgaragefrontend:1.0 -Dbobwilsonsgarage.port=2553 -Dbobwilsonsgarage.hostname=frontend -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

```
// list the running containers
docker ps -a
```

```
// inspect the akka cluster through jmx via the akka cluster CLI
docker run --net=back --rm dbolene/akkaclusterbin:1.0 $(docker inspect -f {{.Node.IP}} backend1) 25519 cluster-status
```


### Take note of the IP for the frontend container
```
docker inspect -f {{.Node.IP}} frontend
```

Will return something like:

```
192.168.99.103
```
### Use the REST api with the frontend ip from above

Post a car repair fulfillment request

```
curl -i -H "Content-Type: application/json" -X POST -d '{"car":"HotRod"}' http://192.168.99.103:8080/BobWilsonsGarage/orders
```

Will return something like:

```
HTTP/1.1 202 Accepted
Server: spray-can/1.3.3
Date: Thu, 17 Sep 2015 19:56:08 GMT
Location: http://192.168.99.103:8080/BobWilsonsGarage/orders/7d25459a-9ff8-4752-ae3f-636b32cbe21a
Content-Length: 0
```

Do a GET on the url returned in the Location header:

```
curl http://192.168.99.103:8080/BobWilsonsGarage/orders/7d25459a-9ff8-4752-ae3f-636b32cbe21a
```

returns:

```
{
  "state": "Fulfilled",
  "orderId": "e4e3fc03-fb84-4eb7-98fd-a77aff4870a8",
  "car": "HotRod",
  "repairedYN": true,
  "detailedYN": true
}
```
Knock down the CarRepairService node (first down the akka cluster node via the Akka-cluster ClI and then stop the docker container)

```
docker run --net=back --rm dbolene/akkaclusterbin:1.0 $(docker inspect -f {{.Node.IP}} backend1) 25519 down akka.tcp://ClusterSystem@detailing:2555

docker stop detailing
```

Post a car repair service order:

```
curl -i -H "Content-Type: application/json" -X POST -d '{"car":"HotRod"}' http://192.168.99.103:8080/BobWilsonsGarage/orders
```

Will return something like:

```
HTTP/1.1 202 Accepted
Server: spray-can/1.3.3
Date: Thu, 17 Sep 2015 19:56:08 GMT
Location: http://192.168.99.103:8080/BobWilsonsGarage/orders/54586309-d2a9-400d-b374-674922b44a3a
Content-Length: 0
```

Do a GET on the url returned in the Location header:

```
curl http://192.168.99.103:8080/BobWilsonsGarage/orders/54586309-d2a9-400d-b374-674922b44a3a
```

returns (because the carrepair service is unavailable):

```
{
  "state": "ServiceUnavailable",
  "orderId": "54586309-d2a9-400d-b374-674922b44a3a",
  "car": "HotRod",
  "repairedYN": false,
  "detailedYN": false
}
```

Remove stopped containers:

```
docker rm -v $(docker ps -aq -f status=exited)
```

Restart the CarRepairService node:

```
docker run -d -p 2555:2555 -p 25559:9999 --name detailing --net=back bobwilsonsgaragedetailing:1.0 -Dbobwilsonsgarage.port=2555 -Dbobwilsonsgarage.hostname=detailing -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

Try to POST again, rinse, repeat, experiment (like try just stopping the detailing container/service).

### Shutdown the swarm cluster 

```
docker-machine stop swarm-agent-00
docker-machine stop swarm-agent-01
docker-machine stop swarm-master
docker-machine stop consul
docker-machine rm -f swarm-agent-00
docker-machine rm -f swarm-agent-01
docker-machine rm -f swarm-master
docker-machine rm -f consul
```


<a name="resources">
Resources
--------
</a>

Akka Service Registry Repo: [https://github.com/Comcast/ActorServiceRegistry](https://github.com/Comcast/ActorServiceRegistry "The Akka Service Registry Repo")

SOA on Steroids presentation: [http://www.slideshare.net/dbolene/soa-on-steroids](http://www.slideshare.net/dbolene/soa-on-steroids "SOA On Steroids")

Presentation Video (starts at minute 27): [https://www.youtube.com/watch?v=QRnHuBL-aSU](https://www.youtube.com/watch?v=QRnHuBL-aSU "Presentation Video")

Akka Cluster JMX CLI invoker container: [https://hub.docker.com/r/dbolene/akkaclusterbin/](https://hub.docker.com/r/dbolene/akkaclusterbin/ "Akka Cluster JMX CLI invoker container")

