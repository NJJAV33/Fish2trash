#!/bin/sh
# Builds the game, compiles the tests in test/, and runs them.
# Exits with 1 if any test fails.
set -e
cd "$(dirname "$0")/.."
. scripts/jdk.sh

sh scripts/build.sh
rm -rf out-test
mkdir -p out-test
"$JAVAC" --release 17 -Xlint:all -cp out -d out-test test/*.java
# Headless: the tests never open a window, so they also run without a display.
"$JAVA" -Djava.awt.headless=true -cp out:out-test TestRunner
