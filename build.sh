#!/bin/sh
# Compiles all sources into out/ (Java 17 or newer).
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac --release 17 -d out $(find src -name "*.java")
echo "Build OK -> out/"
