#!/bin/bash
wd=$(pwd)
cd $1
# delete all .class files
find . -name "*.class" -type f -delete
find . -name "*.java" > sources.txt # generate list of files
javac @sources.txt # compile from list of files
rm sources.txt # cleanup
cd "$wd"
