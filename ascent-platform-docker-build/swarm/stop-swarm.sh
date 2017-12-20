#!/usr/bin/env bash

# Disconnect shell from VMs
eval $(docker-machine env -u)

# Stop and Remove Created VMs
docker-machine stop $(docker-machine ls -q)
yes | docker-machine rm $(docker-machine ls -q)