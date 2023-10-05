#!/usr/bin/bash
#echo "Removing docker image"
docker image rm -f zbyna64/lra-coordinator:latest
#echo "Building the docker image"
docker build -t zbyna64/lra-coordinator .
#echo "Running the docker image with port-forwarding 8080"
docker run --network=host zbyna64/lra-coordinator
