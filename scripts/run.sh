#!/bin/sh
# Builds the game, then starts it.
set -e
cd "$(dirname "$0")/.."
. scripts/jdk.sh

sh scripts/build.sh
"$JAVA" -cp out Main
