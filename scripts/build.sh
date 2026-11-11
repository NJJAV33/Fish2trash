#!/bin/sh
# Compiles the game into out/. Works from any folder.
set -e
cd "$(dirname "$0")/.."
. scripts/jdk.sh

rm -rf out
mkdir -p out
# --release 17 keeps the code compatible with Java 17 even on a newer JDK.
"$JAVAC" --release 17 -Xlint:all -d out $(find src -name '*.java')
