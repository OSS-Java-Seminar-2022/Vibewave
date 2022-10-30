#!/bin/bash
# important: we recommend using docker-compose.yml file to run the app (command: docker compose up -d); only use this if you experience issues
# note: needs to be run with sudo

# create database
# docker pull postgres
# docker run --name vibewavedb -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
# sudo docker exec -it --user postgres vibewavedb sh -c "psql -c 'CREATE DATABASE vibewave OWNER postgres;'"

# create network
# docker network create vibewave-net
# docker network connect vibewave-net vibewavedb

# build project
# ./mvnw install # note: path needs to be ammended to correct one

# build app container
# docker build -t vibewave-app ../vibewave-app/

# run app with docker
# docker run -p 9090:8080 --name vibewave-app --net vibewave-net -e DB_HOSTNAME='vibewavedb' vibewave-app
