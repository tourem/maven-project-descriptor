# Descriptor Plugin

A Maven plugin that automatically generates comprehensive JSON deployment descriptors for your Maven projects, including Spring Boot applications, multi-module projects, and environment-specific configurations.

## Features

✅ **Automatic Module Detection**: Identifies deployable modules (JAR, WAR, EAR)  
✅ **Spring Boot Support**: Detects Spring Boot executables, profiles, and configurations  
✅ **Environment Configurations**: Extracts dev, hml, prod environment settings  
✅ **Actuator Endpoints**: Discovers health, info, and metrics endpoints  
✅ **Maven Assembly**: Detects assembly artifacts (ZIP, TAR.GZ)  
✅ **Deployment Metadata**: Java version, main class, server ports, context paths  
✅ **Multi-Module Projects**: Full support for Maven reactor builds  

## Quick Start

### Installation

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.larbotech</groupId>
            <artifactId>descriptor-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

### Basic Usage

Generate a deployment descriptor at your project root:

```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate
```

This creates a `descriptor.json` file containing all deployment information.

## Usage Examples

### Command Line

#### 1. Generate with default settings
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate
```
Output: `descriptor.json` at project root

#### 2. Custom output file name
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.outputFile=deployment-info.json
```

#### 3. Custom output directory
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.outputDirectory=target \
  -Ddescriptor.outputFile=deployment-descriptor.json
```

#### 4. Compact JSON (no pretty print)
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.prettyPrint=false
```

### POM Configuration

Configure the plugin to run automatically during the build:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.larbotech</groupId>
            <artifactId>descriptor-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <!-- Output file name (default: descriptor.json) -->
                <outputFile>deployment-info.json</outputFile>

                <!-- Output directory (default: project root) -->
                <outputDirectory>target</outputDirectory>

                <!-- Pretty print JSON (default: true) -->
                <prettyPrint>true</prettyPrint>

                <!-- Skip execution (default: false) -->
                <skip>false</skip>
            </configuration>
            <executions>
                <execution>
                    <id>generate-descriptor</id>
                    <phase>package</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Then simply run:
```bash
mvn clean package
```

## Configuration Parameters

| Parameter | System Property | Default | Description |
|-----------|----------------|---------|-------------|
| `outputFile` | `descriptor.outputFile` | `descriptor.json` | Name of the output file |
| `outputDirectory` | `descriptor.outputDirectory` | Project root | Output directory (absolute or relative path) |
| `prettyPrint` | `descriptor.prettyPrint` | `true` | Format JSON with indentation |
| `skip` | `descriptor.skip` | `false` | Skip plugin execution |

## Output Example

```json
{
  "projectGroupId": "com.example",
  "projectArtifactId": "my-application",
  "projectVersion": "1.0.0",
  "projectName": "My Application",
  "projectDescription": "Multi-module Spring Boot application",
  "generatedAt": [2025, 11, 9, 0, 20, 48, 83495000],
  "deployableModules": [
    {
      "groupId": "com.example",
      "artifactId": "api-service",
      "version": "1.0.0",
      "packaging": "jar",
      "repositoryPath": "com/example/api-service/1.0.0/api-service-1.0.0.jar",
      "finalName": "api-service",
      "springBootExecutable": true,
      "modulePath": "api-service",
      "environments": [
        {
          "profile": "dev",
          "serverPort": 8080,
          "contextPath": "/api",
          "actuatorEnabled": true,
          "actuatorBasePath": "/actuator",
          "actuatorHealthPath": "/actuator/health",
          "actuatorInfoPath": "/actuator/info"
        }
      ],
      "assemblyArtifacts": [
        {
          "assemblyId": "distribution",
          "format": "zip",
          "repositoryPath": "com/example/api-service/1.0.0/api-service-1.0.0.zip"
        }
      ],
      "mainClass": "com.example.api.ApiApplication",
      "buildPlugins": ["spring-boot-maven-plugin", "maven-assembly-plugin"]
    }
  ],
  "totalModules": 5,
  "deployableModulesCount": 3
}
```

## Use Cases

### CI/CD Integration

#### GitHub Actions
```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          
      - name: Build and generate descriptor
        run: mvn clean package
        
      - name: Parse and deploy
        run: |
          MODULES=$(jq -r '.deployableModules[] | select(.springBootExecutable == true) | .artifactId' target/descriptor.json)
          for module in $MODULES; do
            echo "Deploying $module..."
            # Your deployment logic here
          done
```

#### GitLab CI
```yaml
deploy:
  stage: deploy
  script:
    - mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate
    - |
      jq -r '.deployableModules[]' descriptor.json | while read -r module; do
        echo "Processing module: $module"
        # Deployment logic
      done
```

### Deployment Scripts

```bash
#!/bin/bash
# deploy.sh - Automated deployment script

# Generate deployment descriptor
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate

# Extract deployable modules
MODULES=$(jq -r '.deployableModules[] | select(.springBootExecutable == true)' descriptor.json)

# Deploy each module
echo "$MODULES" | jq -c '.' | while read -r module; do
    ARTIFACT_ID=$(echo "$module" | jq -r '.artifactId')
    REPO_PATH=$(echo "$module" | jq -r '.repositoryPath')
    MAIN_CLASS=$(echo "$module" | jq -r '.mainClass')
    
    echo "Deploying $ARTIFACT_ID..."
    echo "  Repository path: $REPO_PATH"
    echo "  Main class: $MAIN_CLASS"
    
    # Your deployment commands here
    # scp, kubectl, docker, etc.
done
```

## Project Structure

```
descriptor-parent/
├── descriptor-core/             # Core library
│   └── src/main/java/
│       └── com/larbotech/maven/descriptor/
│           ├── model/           # Data models
│           └── service/         # Analysis services
│
└── descriptor-plugin/           # Maven plugin
    └── src/main/java/
        └── com/larbotech/maven/plugin/
            └── GenerateDescriptorMojo.java
```

## Requirements

- **Java**: 21 or higher
- **Maven**: 3.6.0 or higher

## Building from Source

```bash
# Clone the repository
git clone https://github.com/larbotech/mavenflow.git
cd mavenflow

# Build and install
mvn clean install

# Run tests
mvn test
```

## What Gets Detected

The plugin automatically analyzes your Maven project and detects:

- **Module Information**: Group ID, Artifact ID, Version, Packaging type
- **Spring Boot Applications**: Executables, main class, profiles
- **Environment Configurations**: Server port, context path, actuator settings
- **Actuator Endpoints**: Health, info, metrics endpoints
- **Maven Assembly**: Assembly descriptors, formats, repository paths
- **Build Plugins**: spring-boot-maven-plugin, maven-assembly-plugin

## Troubleshooting

### Plugin not found
Make sure the plugin is installed in your local Maven repository:
```bash
mvn clean install
```

### Multi-module projects
Run the plugin from the parent POM directory. It will analyze all modules in the reactor.

## License

This project is licensed under the Apache License 2.0.

---

**Made with ❤️ by LarboTech**

