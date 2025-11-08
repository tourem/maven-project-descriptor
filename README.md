# MavenFlow

**Intelligent Maven project analyzer** that generates comprehensive deployment descriptors with environment configurations, executable detection, and assembly artifacts for streamlined CI/CD automation.

Built with **Java 21**, **Spring Boot 3**, and modern Java features.

## 🎯 Purpose

This tool scans Maven projects (single or multi-module) and generates a detailed JSON descriptor containing:

- **Deployable modules** (JAR, WAR, EAR)
- **Spring Boot executables** with environment-specific configurations
- **Maven repository paths** for artifact deployment
- **Assembly artifacts** (ZIP, TAR.GZ configurations)
- **Build plugins** (Spring Boot, Quarkus, Shade, Jib, etc.)
- **Deployment metadata** (Java version, main class, server ports, Actuator endpoints)
- **Environment configurations** per profile (dev, staging, prod)
- **Local dependencies** between modules
- **Generation timestamp** (ISO 8601 format with LocalDateTime)

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**

### Build

```bash
./build.sh
```

Or manually:

```bash
mvn clean package
```

### Run

```bash
./run.sh /path/to/maven/project
```

Or manually:

```bash
java -jar target/mavenflow-1.0-SNAPSHOT.jar /path/to/maven/project
```

### Generate JSON File

```bash
# Generate descriptor.json in current directory
./run.sh /path/to/maven/project -o

# Generate with custom output path
./run.sh /path/to/maven/project /custom/path/output.json
```

## 📋 Usage

```
Usage:
  java -jar mavenflow.jar <project-root-path> [options]

Arguments:
  project-root-path  Path to the root directory of the Maven project

Options:
  -o, --output       Generate descriptor.json in current directory
  [output-file]      Custom path to output JSON file
                     If not specified, prints to stdout

Examples:
  java -jar mavenflow.jar /path/to/maven/project
  java -jar mavenflow.jar /path/to/maven/project -o
  java -jar mavenflow.jar /path/to/maven/project descriptor.json
  java -jar mavenflow.jar /path/to/maven/project /custom/path/output.json
```

## 📊 Output Example

```json
{
  "projectGroupId": "com.larbotech",
  "projectArtifactId": "github-actions-project",
  "projectVersion": "1.0-SNAPSHOT",
  "projectName": "github-actions-project",
  "projectDescription": "Multi-module project with REST API and Batch",
  "generatedAt": "2025-11-08T21:00:53.631563",
  "totalModules": 4,
  "deployableModulesCount": 3,
  "deployableModules": [
    {
      "groupId": "com.larbotech",
      "artifactId": "task-api",
      "version": "1.0-SNAPSHOT",
      "packaging": "jar",
      "repositoryPath": "com/larbotech/task-api/1.0-SNAPSHOT/task-api-1.0-SNAPSHOT.jar",
      "finalName": "task-api",
      "springBootExecutable": true,
      "modulePath": "task-api",
      "environments": [
        {
          "profile": "dev",
          "serverPort": 8080,
          "contextPath": "/api/v1",
          "actuatorEnabled": true,
          "actuatorBasePath": "/management",
          "actuatorHealthPath": "/management/health",
          "actuatorInfoPath": "/management/info"
        }
      ],
      "assemblyArtifacts": [
        {
          "assemblyId": "distribution",
          "format": "zip",
          "repositoryPath": "com/larbotech/task-api/1.0-SNAPSHOT/task-api-1.0-SNAPSHOT.zip"
        }
      ],
      "javaVersion": "21",
      "mainClass": "com.larbotech.taskapi.TaskApiApplication",
      "localDependencies": ["common"],
      "buildPlugins": [
        "spring-boot-maven-plugin",
        "maven-assembly-plugin"
      ]
    }
  ]
}
```

## 🔍 Features

### 1. Module Detection

- ✅ Detects deployable modules (JAR, WAR, EAR, EJB, RAR)
- ✅ Excludes non-deployable modules (POM, Maven plugins)
- ✅ Identifies Spring Boot executables
- ✅ Detects executable plugins (Spring Boot, Quarkus, Shade, Assembly, Jib, Dockerfile)

### 2. Environment Configurations

- ✅ Detects Spring Boot profiles from `application-{profile}.yml/properties`
- ✅ Extracts environment-specific configurations:
  - Server port (`server.port`)
  - Context path (`server.servlet.context-path`)
  - Actuator settings (`management.endpoints.web.base-path`)
- ✅ Supports configuration inheritance (common config + profile overrides)
- ✅ Deep merge of nested YAML/properties structures

### 3. Assembly Artifacts

- ✅ Detects `maven-assembly-plugin` configurations
- ✅ Parses assembly descriptors (XML files)
- ✅ Generates repository paths for each assembly artifact
- ✅ Supports multiple formats (ZIP, TAR.GZ, TAR.BZ2, etc.)

### 4. Deployment Metadata

- ✅ Java version (from `maven.compiler.source`, `maven.compiler.target`, or `maven.compiler.release`)
- ✅ Main class for Spring Boot applications
- ✅ Local module dependencies
- ✅ Maven repository paths for artifact deployment

### 5. Actuator Endpoints

- ✅ Detects Spring Boot Actuator presence
- ✅ Extracts custom base paths
- ✅ Generates health and info endpoint paths
- ✅ Per-environment Actuator configurations

### 6. Metadata & Traceability

- ✅ **Generation timestamp** - Automatically captures when the descriptor was generated
- ✅ **ISO 8601 format** - Uses `LocalDateTime` for precise timestamp (e.g., `2025-11-08T22:21:36.63808`)
- ✅ **Project statistics** - Total modules count and deployable modules count
- ✅ **Project information** - GroupId, ArtifactId, Version, Name, Description

## 🛠️ Development

### Run Tests

```bash
./test.sh
```

Or manually:

```bash
mvn test
```

### Test on Sample Project

```bash
# Analyze a sample project and display JSON
./test-sample.sh /path/to/sample/maven/project

# Generate descriptor.json for a sample project
./test-sample.sh /path/to/sample/maven/project -o
```

### Project Structure

```
maven-project-descriptor/
├── src/main/java/com/larbotech/maven/descriptor/
│   ├── MavenProjectDescriptorApplication.java  # Main application
│   ├── model/                                   # Data models (Records)
│   │   ├── AssemblyArtifact.java
│   │   ├── DeployableModule.java
│   │   ├── EnvironmentConfig.java
│   │   ├── PackagingType.java
│   │   └── ProjectDescriptor.java
│   └── service/                                 # Business logic
│       ├── DeploymentMetadataDetector.java
│       ├── EnvironmentConfigDetector.java
│       ├── ExecutablePluginDetector.java
│       ├── MavenAssemblyDetector.java
│       ├── MavenProjectAnalyzer.java
│       ├── MavenRepositoryPathGenerator.java
│       ├── SpringBootDetector.java
│       └── SpringBootProfileDetector.java
└── src/test/java/                               # Tests (81 tests)
```

## 🎨 Modern Java Features (JDK 21)

This project leverages modern Java 21 features:

- ✅ **Records** - Immutable data carriers (`AssemblyArtifact`, `EnvironmentConfig`, `ProjectDescriptor`)
- ✅ **Pattern Matching for instanceof** - Type checks with automatic casting
- ✅ **Switch Expressions** - Modern switch with return values
- ✅ **Text Blocks** - Multi-line string literals
- ✅ **Stream API enhancements** - `.toList()` instead of `Collectors.toList()`
- ✅ **Try-with-resources with var** - Automatic resource management

## 📦 Dependencies

- **Spring Boot 3.2.0** - Application framework
- **Maven Model 3.9.5** - POM parsing
- **SnakeYAML 2.0** - YAML configuration parsing
- **Jackson** - JSON serialization
- **Lombok** - Code generation (@Slf4j, @Builder)
- **JUnit 5** - Testing framework

## 🔧 Detected Plugins

The tool detects the following executable plugins:

| Plugin | Group ID | Purpose |
|--------|----------|---------|
| `spring-boot-maven-plugin` | `org.springframework.boot` | Spring Boot applications |
| `quarkus-maven-plugin` | `io.quarkus` | Quarkus applications |
| `maven-shade-plugin` | `org.apache.maven.plugins` | Uber JAR creation |
| `maven-assembly-plugin` | `org.apache.maven.plugins` | Custom assemblies |
| `jib-maven-plugin` | `com.google.cloud.tools` | Container images |
| `dockerfile-maven-plugin` | `com.spotify` | Docker images |

## 📝 License

This project is licensed under the MIT License.

## 👥 Authors

- **Larbotech** - Initial work

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

