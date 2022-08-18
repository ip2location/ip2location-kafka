# IP2Location Kafka Transform

This Apache Kafka Transform provides a fast lookup of country, region, city, latitude, longitude, ZIP code, time zone, ISP, domain name, connection type, IDD code, area code, weather station code, station name, mcc, mnc, mobile brand, elevation, usage type, address type and IAB category from IP address by using IP2Location database. It uses a file-based database available at IP2Location.com. This database simply contains IP blocks as keys, and other information such as country, region, city, latitude, longitude, ZIP code, time zone, ISP, domain name, connection type, IDD code, area code, weather station code, station name, mcc, mnc, mobile brand, elevation, usage type, address type and IAB category as values. It supports both IP address in IPv4 and IPv6.

The database will be updated on a monthly basis for greater accuracy.

The complete database is available at https://www.ip2location.com under Premium subscription package.
The free LITE database is available at https://lite.ip2location.com.

## Installation

Download the latest IP2Location Java jar file ip2location-java-x.y.z.jar from https://search.maven.org/artifact/com.ip2location/ip2location-java

Download the latest IP2Location Kafka jar file ip2location-kafka-x.y.z.jar from https://search.maven.org/artifact/com.ip2location/ip2location-kafka

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

## Schema

|Name|Schema|
|---|---|
|ip2location_country_code|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_country_name|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_region|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_city|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_latitude|[Float32](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#FLOAT32)|
|ip2location_longitude|[Float32](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#FLOAT32)|
|ip2location_zip_code|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_time_zone|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_isp|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_domain|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_net_speed|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_idd_code|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_area_code|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_weather_station_code|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_weather_station_name|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_mcc|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_mnc|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_mobile_brand|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_elevation|[Float32](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#FLOAT32)|
|ip2location_usage_type|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_address_type|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_category|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
|ip2location_error|[String](https://kafka.apache.org/0102/javadoc/org/apache/kafka/connect/data/Schema.Type.html#STRING)|
