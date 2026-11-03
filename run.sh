#!/bin/sh
# Builds and starts the iBank ABM. Run from the project folder: ./run.sh
set -e
./build.sh
java -cp out ibank.Main
