#!/bin/bash
docker-compose -f docker-compose.logging.yml \
               -f docker-compose.logging.override.yml \
               up --build