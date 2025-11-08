#!/bin/bash

# Run script for MavenFlow

JAR_FILE="target/mavenflow-1.0-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Please run ./build.sh first"
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Usage: ./run.sh <project-root-path> [options]"
    echo ""
    echo "Examples:"
    echo "  ./run.sh /path/to/maven/project"
    echo "  ./run.sh /path/to/maven/project -o"
    echo "  ./run.sh /path/to/maven/project descriptor.json"
    exit 1
fi

java -jar "$JAR_FILE" "$@"

