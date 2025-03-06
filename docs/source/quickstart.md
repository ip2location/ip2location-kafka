# Quickstart

## Dependencies

This library requires IP2Location BIN database to function. You may download the BIN database at

-   IP2Location LITE BIN Data (Free): <https://lite.ip2location.com>
-   IP2Location Commercial BIN Data (Comprehensive):
    <https://www.ip2location.com>

## IPv4 BIN vs IPv6 BIN

Use the IPv4 BIN file if you just need to query IPv4 addresses.

Use the IPv6 BIN file if you need to query BOTH IPv4 and IPv6 addresses.

## Installation

Download the latest IP2Location Java jar file ip2location-java-x.y.z.jar from <https://search.maven.org/artifact/com.ip2location/ip2location-java>

Download the latest IP2Location Kafka jar file ip2location-kafka-x.y.z.jar from <https://search.maven.org/artifact/com.ip2location/ip2location-kafka>

Copy both jar files into whichever plugin folder your Kafka installation is using.

Download the IP2Location BIN database file into the folder of your choice.

## Configure your connector properties

This transformation is used to lookup data from a IP2Location BIN database file and appending the data into an existing struct.

```properties
transforms=insertip2location
transforms.insertip2location.type=com.ip2location.kafka.connect.smt.InsertIP2Location$Value

# Set these required values
transforms.insertip2location.ip2location.bin.path=
transforms.insertip2location.ip2location.input=
```

|Name|Description|
|---|---|
|ip2location.bin.path|The full path to the IP2Location BIN database file.|
|ip2location.input|The field name in the record containing the IP address to query for geolocation.|