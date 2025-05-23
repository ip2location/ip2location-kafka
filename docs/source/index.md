# IP2Location Kafka Transform

This Apache Kafka Transform provides a fast lookup of country, region, city, latitude, longitude, ZIP code, time zone, ISP, domain name, connection type, IDD code, area code, weather station code, station name, mcc, mnc, mobile brand, elevation, usage type, address type, IAB category, district, autonomous system number (ASN) and autonomous system (AS) from IP address by using IP2Location database. It uses a file-based database available at IP2Location.com. This database simply contains IP blocks as keys, and other information such as country, region, city, latitude, longitude, ZIP code, time zone, ISP, domain name, connection type, IDD code, area code, weather station code, station name, mcc, mnc, mobile brand, elevation, usage type, address type, IAB category, district, autonomous system number (ASN) and autonomous system (AS) as values. It supports both IP address in IPv4 and IPv6.

The database will be updated on a monthly basis for greater accuracy.

The complete database is available at <https://www.ip2location.com> under Premium subscription package.
The free LITE database is available at <https://lite.ip2location.com>.

## Table of contents
 ```{eval-rst}
 .. toctree::

   self
   quickstart
   code
 ```