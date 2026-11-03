#!/bin/sh
# Starts iBank again from data/cardholders.csv and a full dispenser,
# by deleting the state saved by previous runs. Run from the project folder: ./reset.sh
rm -rf data/state
echo "Saved state deleted: the next ./run.sh starts from the beginning."
