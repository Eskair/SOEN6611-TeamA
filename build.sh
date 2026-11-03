#!/bin/sh
# Builds iBank with the plain JDK (no IDE needed). Run from the project folder: ./build.sh
set -e
rm -rf out
javac --release 17 -d out $(find src -name '*.java')
# javac only compiles .java files, so the English/French text files are copied separately.
cp src/ibank/i18n/*.properties out/ibank/i18n/
echo "Build OK: classes are in out/"
