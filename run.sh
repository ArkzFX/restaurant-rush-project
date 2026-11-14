#!/bin/sh
# Builds (if needed) and starts the game. Extra arguments are passed to rush.Main.
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
java -cp out rush.Main "$@"
