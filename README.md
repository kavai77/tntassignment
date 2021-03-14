# Himadri Csaba Kavai TNT Assignment

## Prerequisites
* OpenJDK 11
* Maven

## Building & Running the tests
`mvn compile test`

### Running the integration and load tests
`INTEGRATIONTEST=enabled mvn test`

Only works with a running `backend-services` container on the port 8080.

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

# Design decisions

## Framework selections
* Spring Boot: very easy to spin up a webserver and create web services. Ideal framework for this exercise.
* Jackson: default choice for Spring boot to serialize/deserialize JSON. For this json schema mostly `@JsonValue` and `@JsonCreator` were used on Entity classes
* Testing: JUnit5 with `@SpringBootTest` for integration level testing, and Mockito for mocked testing. `@RepeatedTest` for load testing and some `@ParameterizedTest`
* Configuration: all important parameters are maintained in `application.properties` and injected into the classes

## Project Reactive
This was a major design decision. I was more familiar with the classic Java `ExecutorService` and `Future`s to implement concurrency.

Project Reactive is well supported by Spring Boot:
* `WebClient` is used to create non-blocking HTTP calls towards the APIs by returning a `Mono`
* `Mono` in supported to be returned from `@RestController` methods

Timeouts, aggregation, error handling and push notifications from Buffer was easier and cleaner to implement with Project Reactive.

## API Gateway
Initially 3 API Gateways are implemented: Shipment, Track and Pricing.
However, the API Gateway is a general purpose class and is easy to extend with new APIs.

The `ApiGateway` is an interface which has 2 implementors:
1. `DefaultApiGateway`
2. `ThrottlingApiGateway`

The `DefaultApiGateway` only implements a `WebClient` based gateway and applies a timout.

`ThrottlingApiGateway` builds upon `DefaultApiGateway` and implements a buffer and a scheduler.
The scheduler ticks every second to release "old" requests (>5 sec, configurable) from the buffer.
New requests only fill up the buffer and return a pending `Mono`.
We also save the `sink` of this `Mono` inside the buffer, in order to push a notification to all the pending `Mono`s once we actually release the buffer and make the HTTP call at a later time.

It is only the scheduler which can flush the buffer, but if we detect during an incoming request, that the buffer has enough elements, it makes a forced push for the scheduler (outside the regular 1 sec ticks).

## Aggregation Service
`AggregationController` implements the aggregation. We have 3 `Mono`s for the 3 different API Gateways.

A `Flux.merge` is used to merge the 3 `Mono`s. `Flux` completes once all the 3 `Mono`s are completed.

There is no `timeout` on the `Flux` itself, because every individual `Mono` has a timeout.
Plus a load test was implemented to have some stats of the running times.

## Testing
There are three kinds of tests:
* Mocked tests
* Integration tests
* Load test

Integration and load tests use the spring boot testing framework and inject the actual beans without mocking.
They are enabled only with a specific environment variable, because they need the running API services (on port 8080) to complete.
