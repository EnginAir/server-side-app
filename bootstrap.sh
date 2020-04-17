#!/bin/sh

if [ -z $MAX_RAM ]; then
MAX_RAM=1G
fi

if [ -z $MIN_RAM ]; then
MIN_RAM=128M
fi

java -Xmx$MAX_RAM -Xms$START_RAM -jar server-side-app.jar $@
