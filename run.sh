#!/bin/sh

set -e # Exit early if any commands fail

(
	cd "$(dirname "$0")" # Ensure compile steps are run within the repository directory
	mvn -q -B package
)

exec java -cp target/MySh-1.0-SNAPSHOT.jar MySh.Main
