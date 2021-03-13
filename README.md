# Csaba Kavai TNT Assignment

## Prerequisites
* JDK 11
* Maven

## Building & Running the Tests
`mvn test`

## Running
Run the docker app on port 8080:
```
docker run -p 8080:8080 xyzassessment/backend-services
```
Start the aggregator app:
```
mvn spring-boot:run
```
It starts on port 8081 by default.

Make an example request:
```
curl "http://localhost:8081/aggregation?pricing=NL,CN&track=109347263,123456891&shipments=109347263,123456891"
```