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

### Start sbt 

```
cd ~/path/to/project/BobWilsonsGarage
sbt
```
#### Setup

Build the cluster node jars (from sbt)

```
assembly
```

Install a single development node version of Cassandra and then run it (from the console)

```
cd ~/path/to/cassandra/dsc-cassandra-2.1.4
bin/cassandra -f
```

#####Run the cluster nodes (from the console)

Run the first backend shard node

```
cd ~/path/to/project/BobWilsonsGarage

java -jar backEnd/target/scala-2.11/BackEnd.jar 2551
```

Run the second backend shard node

```
cd ~/path/to/project/BobWilsonsGarage

java -jar backEnd/target/scala-2.11/BackEnd.jar 2552
```

Run the frontend rest node


```
cd ~/path/to/project/BobWilsonsGarage

java -jar frontEnd/target/scala-2.11/FrontEnd.jar 2553
```

Run the staffing service microservice rest node

```
cd ~/path/to/project/BobWilsonsGarage

java -jar frontEnd/target/scala-2.11/StaffingService 2554
```

Run the staffing service microservice rest node

```
cd ~/path/to/project/BobWilsonsGarage

java -jar frontEnd/target/scala-2.11/DetailingService.jar 2555
```

Run the staffing service microservice rest node

```
cd ~/path/to/project/BobWilsonsGarage

java -jar frontEnd/target/scala-2.11/CarRepairService.jar 2556
```

Use ctrl+c to stop cluster nodes.


#### Use the REST api:

Post a car repair service order:

```
curl -i -H "Content-Type: application/json" -X POST -d '{"car":"HotRod"}' http://localhost:8080/BobWilsonsGarage/orders
```

Will return something like:

```
HTTP/1.1 202 Accepted
Server: spray-can/1.3.3
Date: Thu, 17 Sep 2015 19:56:08 GMT
Location: http://localhost:8080/BobWilsonsGarage/orders/7d25459a-9ff8-4752-ae3f-636b32cbe21a
Content-Length: 0
```

Do a GET on the url returned in the Location header:

```
curl http://localhost:8080/BobWilsonsGarage/orders/7d25459a-9ff8-4752-ae3f-636b32cbe21a
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
Knock down the CarRepairService node (ctrl+c its console)

Post a car repair service order:

```
curl -i -H "Content-Type: application/json" -X POST -d '{"car":"HotRod"}' http://localhost:8080/BobWilsonsGarage/orders
```

Will return something like:

```
HTTP/1.1 202 Accepted
Server: spray-can/1.3.3
Date: Thu, 17 Sep 2015 19:56:08 GMT
Location: http://localhost:8080/BobWilsonsGarage/orders/54586309-d2a9-400d-b374-674922b44a3a
Content-Length: 0```

Do a GET on the url returned in the Location header:

```
curl http://localhost:8080/BobWilsonsGarage/orders/54586309-d2a9-400d-b374-674922b44a3a
```

returns:

```
{
  "state": "ServiceUnavailable",
  "orderId": "54586309-d2a9-400d-b374-674922b44a3a",
  "car": "HotRod",
  "repairedYN": false,
  "detailedYN": false
}
```

Restart the CarRepairService node:

```
cd ~/path/to/project/BobWilsonsGarage

java -jar frontEnd/target/scala-2.11/CarRepairService.jar 2556
```

Try to POST again, rinse, repeat, experiment.


<a name="resources">
Resources
--------
</a>

Akka Service Registry Repo: [https://github.com/Comcast/ActorServiceRegistry](https://github.com/Comcast/ActorServiceRegistry "The Akka Service Registry Repo")

SOA on Steroids presentation: [http://www.slideshare.net/dbolene/soa-on-steroids](http://www.slideshare.net/dbolene/soa-on-steroids "SOA On Steroids")

