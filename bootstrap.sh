#!/bin/sh

if [ -z $MAX_RAM ]; then
MAX_RAM=1G
fi

if [ -z $START_RAM ]; then
START_RAM=128M
fi

java -Xmx$MAX_RAM -Xms$START_RAM -jar server-side-app.jar $@
