# SSE example with Quarkus

Simple SSE Broadcaster with Quarkus that simulates a background process. It notifies the client whenever there is a change in the process status (processing percentage has increased).

## Prerequisites

- Java 17
- Maven 3.9.0
- Docker 20.10.17

## How to run

```bash
mvn quarkus:dev
```

## Client

You can find a simple client in [./client/](./client/)
