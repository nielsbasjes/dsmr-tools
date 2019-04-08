#!/bin/bash

mkfifo ttyUSB0
echo "Created fake device ttyUSB0"

function cleanup {
	rm ttyUSB0
	echo "Removed fake device ttyUSB0"
}
trap cleanup EXIT

echo "You can start listening now"

java -jar target/*jar > ttyUSB0

