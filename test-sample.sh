#!/bin/bash

# Test script for analyzing a sample Maven project with MavenFlow

JAR_FILE="target/mavenflow-1.0-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Please run ./build.sh first"
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Usage: ./test-sample.sh <project-root-path> [options]"
    echo ""
    echo "Examples:"
    echo "  ./test-sample.sh /path/to/sample/maven/project"
    echo "  ./test-sample.sh /path/to/sample/maven/project -o"
    echo "  ./test-sample.sh /path/to/sample/maven/project output.json"
    exit 1
fi

echo "🔍 Analyzing Maven project: $1"
echo ""

java -jar "$JAR_FILE" "$@"

echo ""
echo "✅ Analysis complete!"

