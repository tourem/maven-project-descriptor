# Changelog

All notable changes to this project will be documented in this file.

## [1.0-SNAPSHOT] - 2025-11-09

### Added
- Initial release of the Descriptor Plugin
- Multi-module Maven project structure
- Core library (`descriptor-core`) for Maven project analysis
- Maven plugin (`descriptor-plugin`) for generating deployment descriptors
- Automatic detection of deployable modules (JAR, WAR, EAR)
- Spring Boot executable detection
- Environment-specific configuration extraction (dev, hml, prod)
- Actuator endpoint discovery
- Maven Assembly artifact detection
- Deployment metadata extraction (Java version, main class, server ports)
- Configurable output file name and directory
- Pretty print JSON option
- Skip execution option

### Changed
- Renamed core library from `mavenflow-core` to `descriptor-core`
- Renamed plugin from `mavenflow-maven-plugin` to `descriptor-plugin`
- Changed system properties prefix from `mavenflow.*` to `descriptor.*`
- Removed Spring Boot dependencies from core library
- Converted to pure Java library with manual dependency injection

### Technical Details

#### Module Structure
```
descriptor-parent/
├── descriptor-core/         # Core analysis library
└── descriptor-plugin/       # Maven plugin
```

#### Plugin Configuration
- **groupId**: `com.larbotech`
- **artifactId**: `descriptor-plugin`
- **version**: `1.0-SNAPSHOT`

#### System Properties
- `descriptor.outputFile` - Output file name (default: `descriptor.json`)
- `descriptor.outputDirectory` - Output directory (default: project root)
- `descriptor.prettyPrint` - Pretty print JSON (default: `true`)
- `descriptor.skip` - Skip execution (default: `false`)

#### Usage
```bash
# Basic usage
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate

# With custom output
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.outputFile=deployment.json \
  -Ddescriptor.outputDirectory=target
```

### Requirements
- Java 21 or higher
- Maven 3.6.0 or higher

### Dependencies
- Jackson 2.15.3 (JSON serialization)
- Jackson JSR310 (Java 8 date/time support)
- Maven Plugin API 3.9.5
- Maven Core 3.9.5
- Lombok 1.18.30
- SnakeYAML 2.0 (YAML parsing)
- JUnit Jupiter 5.10.1 (testing)
- AssertJ 3.24.2 (testing)

### Known Issues
- None

### Future Enhancements
- Support for additional packaging types
- Docker configuration detection
- Kubernetes manifest generation
- Custom configuration profiles
- Plugin prefix registration for shorter commands

