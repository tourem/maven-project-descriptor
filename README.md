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

## Release Process

### Overview

The project uses GitHub Actions for automated releases to JFrog Artifactory. The release workflow:

1. ✅ Builds and tests the project
2. ✅ Sets the release version
3. ✅ Deploys artifacts to JFrog Artifactory
4. ✅ Creates a Git tag
5. ✅ Automatically calculates and sets the next SNAPSHOT version
6. ✅ Creates a GitHub Release

### Automatic Version Management

The release workflow automatically calculates the next SNAPSHOT version based on the release version:

| Release Version | Next SNAPSHOT Version |
|----------------|----------------------|
| `1.0.0` | `1.1.0-SNAPSHOT` |
| `1.5.0` | `1.6.0-SNAPSHOT` |
| `2.0.0` | `2.1.0-SNAPSHOT` |
| `2.3.5` | `2.4.0-SNAPSHOT` |

**Logic**: The workflow increments the **minor version** and resets the patch to 0.

### How to Create a Release

#### Prerequisites

1. **JFrog Artifactory Access**:
   - JFrog Artifactory URL (e.g., `https://myjfrog.com/artifactory`)
   - JFrog username
   - JFrog API token or password

2. **GitHub Permissions**:
   - Write access to the repository
   - Ability to trigger GitHub Actions workflows

#### Step-by-Step Release Process

1. **Navigate to GitHub Actions**:
   - Go to your repository on GitHub
   - Click on the **Actions** tab
   - Select **Release Descriptor Plugin** workflow

2. **Trigger the Release**:
   - Click **Run workflow** button
   - Fill in the required parameters:

   | Parameter | Description | Example |
   |-----------|-------------|---------|
   | **Release version** | The version to release (X.Y.Z format) | `1.0.0` |
   | **JFrog Artifactory URL** | Your JFrog instance URL | `https://myjfrog.com/artifactory` |
   | **JFrog username** | Your JFrog username | `admin` |
   | **JFrog token** | Your JFrog API token or password | `AKCp...` |

3. **Monitor the Release**:
   - The workflow will execute automatically
   - Monitor the progress in the Actions tab
   - Check for any errors in the logs

4. **Verify the Release**:
   - Check JFrog Artifactory for the deployed artifacts
   - Verify the Git tag was created: `v1.0.0`
   - Check the GitHub Releases page for the new release
   - Confirm the repository is now on the next SNAPSHOT version

### Release Workflow Details

The GitHub Actions workflow performs the following steps:

```yaml
# 1. Checkout code
# 2. Set up JDK 21
# 3. Calculate next SNAPSHOT version (e.g., 1.0.0 → 1.1.0-SNAPSHOT)
# 4. Set release version in POMs
# 5. Build and test the project
# 6. Configure Maven settings for JFrog
# 7. Deploy to JFrog Artifactory
# 8. Commit release version and create Git tag
# 9. Set next SNAPSHOT version in POMs
# 10. Commit next SNAPSHOT version
# 11. Push changes and tags to GitHub
# 12. Create GitHub Release
```

### Using Released Versions

After a successful release, you can use the plugin in your projects:

#### Maven Dependency
```xml
<dependency>
    <groupId>com.larbotech</groupId>
    <artifactId>descriptor-plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Maven Plugin
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.larbotech</groupId>
            <artifactId>descriptor-plugin</artifactId>
            <version>1.0.0</version>
        </plugin>
    </plugins>
</build>
```

#### Command Line
```bash
mvn com.larbotech:descriptor-plugin:1.0.0:generate
```

### Configuring Your Maven Settings

To use artifacts from your JFrog Artifactory, add this to your `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>jfrog-releases</id>
            <username>YOUR_JFROG_USERNAME</username>
            <password>YOUR_JFROG_TOKEN</password>
        </server>
    </servers>

    <profiles>
        <profile>
            <id>jfrog</id>
            <repositories>
                <repository>
                    <id>jfrog-releases</id>
                    <url>https://myjfrog.com/artifactory/libs-release-local</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>jfrog-releases</id>
                    <url>https://myjfrog.com/artifactory/libs-release-local</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>jfrog</activeProfile>
    </activeProfiles>
</settings>
```

### Release Best Practices

1. **Version Numbering**:
   - Follow [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`
   - Increment MAJOR for breaking changes
   - Increment MINOR for new features (backward compatible)
   - Increment PATCH for bug fixes

2. **Before Releasing**:
   - ✅ Ensure all tests pass
   - ✅ Update CHANGELOG.md with release notes
   - ✅ Review and merge all pending PRs
   - ✅ Verify the current SNAPSHOT version builds successfully

3. **After Releasing**:
   - ✅ Verify artifacts in JFrog Artifactory
   - ✅ Test the released version in a sample project
   - ✅ Update documentation if needed
   - ✅ Announce the release to your team

### Troubleshooting Releases

#### Release workflow fails at deployment
- **Cause**: Invalid JFrog credentials or URL
- **Solution**: Verify your JFrog username, token, and URL are correct

#### Version already exists in Artifactory
- **Cause**: Trying to release a version that already exists
- **Solution**: Use a different version number or delete the existing version in Artifactory

#### Git push fails
- **Cause**: Insufficient permissions or branch protection rules
- **Solution**: Ensure the GitHub Actions bot has write permissions and branch protection allows pushes from workflows

#### Tests fail during release
- **Cause**: Code issues or flaky tests
- **Solution**: Fix the failing tests before attempting the release again

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

