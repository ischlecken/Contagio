#!/bin/sh

java -Dspring.data.mongodb.uri=${MONGODB_URI} -Dspring.profiles.active=${ACTIVE_PROFILE} -jar contagio-server.jar
