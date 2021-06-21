#!/bin/bash

export JAVA_OPTIONS="-Xmx256m -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

nohup java $JAVA_OPTIONS -jar quarkus-app/quarkus-run.jar &