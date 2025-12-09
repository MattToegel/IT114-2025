#!/bin/bash

cd "$1" || exit 1

# Compile all source files
find . -name "*.java" > sources.txt
javac @sources.txt

cd ..
project="${1%/}" # remove trailing slash
mkdir -p "$project/build"

# Create Server JAR (include Server and Common packages)
jar cfe "$project/build/${project}_Server.jar" "${project}.Server.Server" \
  $project/Server/*.class $(find "$project/Common" -name "*.class") $project/Exceptions/*.class

# Create Client JAR (include Client and Common packages)
jar cfe "$project/build/${project}_Client.jar" "${project}.Client.ClientUI" \
  $(find "$project/Client" -name "*.class") $(find "$project/Common" -name "*.class")

# Create run.bat for Windows users
{ 
  echo "java -jar ${project}_Client.jar"
  echo "PAUSE"
} > "$project/build/run.bat"
