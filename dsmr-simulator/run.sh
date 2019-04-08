#!/bin/bash

mkfifo ttyDSMR
echo "Created fake device ttyDSMR"

function cleanup {
	rm ttyDSMR
	echo "Removed fake device ttyDSMR"
}
trap cleanup EXIT

echo "You can start listening now"

java -jar target/*jar > ttyDSMR

