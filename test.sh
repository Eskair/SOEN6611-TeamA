#!/bin/sh
# Compiles iBank and its JUnit tests, then runs every test. Run from the project folder: ./test.sh
set -e
./build.sh
JUNIT=$(ls lib/junit-platform-console-standalone-*.jar | head -n 1)
rm -rf out-test
javac --release 17 -cp "out:$JUNIT" -d out-test $(find test -name '*.java')
java -jar "$JUNIT" execute --class-path "out:out-test" --scan-class-path --details=tree
