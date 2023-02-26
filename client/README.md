# Simple test client

A simple page that calls to the server to trigger a process. It then subscribes to receive progress notifications from the server.

## How to run

There is a Dockerfile that creates a simple http server to server the index.html file in this folder.
To run this you need two commands:

build the docker image

```bash
docker build -t busybox-static-files-server .
```

run the docker container

```bash
docker run --name static-server --rm -d -p 3000:3000 busybox-static-files-server
```

## How to stop

```bash
docker stop static-server
```