#!/bin/bash

# Build script for MavenFlow

set -e

echo "🔨 Building MavenFlow..."
echo ""

mvn clean package

echo ""
echo "✅ Build complete!"
echo "📦 JAR location: target/mavenflow-1.0-SNAPSHOT.jar"
echo ""
echo "Run with: ./run.sh /path/to/maven/project"

