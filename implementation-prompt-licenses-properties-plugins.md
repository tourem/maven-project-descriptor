# Prompt d'Implémentation : Licenses, Properties & Plugins

## 🎯 Contexte

Je développe un plugin Maven (`deploy-manifest-plugin`) qui génère des descripteurs de déploiement au format JSON/YAML/HTML. Le plugin analyse automatiquement les projets Maven et extrait les métadonnées de déploiement.

**Repository GitHub** : https://github.com/tourem/descriptor-plugin

**Fonctionnalités actuelles** :
- ✅ Informations des modules (groupId, artifactId, version)
- ✅ Configuration Spring Boot / Quarkus / Micronaut
- ✅ Métadonnées Git et CI/CD
- ✅ Images Docker
- ✅ Arbre de dépendances (flat/tree)

## 🎯 Objectif

Ajouter **3 nouvelles fonctionnalités** au descripteur généré :

1. **Licenses des Dépendances** - Pour compliance et audits légaux
2. **Propriétés Maven Actives** - Pour traçabilité et reproduction du build
3. **Plugins Maven Utilisés** - Pour documentation complète du pipeline de build

Ces features doivent être **optionnelles** (désactivées par défaut) et configurables via CLI et POM.

---

## 📋 Feature 1 : Licenses des Dépendances

### Exigences Fonctionnelles

1. **Extraction des licences** pour chaque dépendance (directe et transitive)
2. **Agrégation par type de licence** (Apache-2.0, MIT, GPL, etc.)
3. **Détection de licences manquantes** ("unknown")
4. **Warnings pour licences incompatibles** (GPL-3.0, AGPL, etc.)
5. **Support multi-licenses** (ex: "MPL-2.0 OR EPL-1.0")

### Paramètres de Configuration

```xml
<configuration>
    <!-- Activer l'extraction des licences -->
    <includeLicenses>true</includeLicenses>
    
    <!-- Générer des warnings pour licences incompatibles -->
    <licenseWarnings>true</licenseWarnings>
    
    <!-- Liste de licences considérées comme incompatibles -->
    <incompatibleLicenses>
        <license>GPL-3.0</license>
        <license>AGPL-3.0</license>
        <license>SSPL</license>
    </incompatibleLicenses>
    
    <!-- Inclure les licences des dépendances transitives -->
    <includeTransitiveLicenses>true</includeTransitiveLicenses>
</configuration>
```

### CLI Usage

```bash
# Activer les licences
mvn deploy-manifest:generate -Ddescriptor.includeLicenses=true

# Avec warnings
mvn deploy-manifest:generate \
  -Ddescriptor.includeLicenses=true \
  -Ddescriptor.licenseWarnings=true

# Définir licences incompatibles via CLI
mvn deploy-manifest:generate \
  -Ddescriptor.includeLicenses=true \
  -Ddescriptor.incompatibleLicenses=GPL-3.0,AGPL-3.0
```

### Structure JSON Attendue

```json
{
  "project": {
    "groupId": "com.example",
    "artifactId": "my-app",
    "version": "1.0.0"
  },
  "modules": [
    {
      "artifactId": "backend-service",
      
      "licenses": {
        "summary": {
          "total": 87,
          "identified": 82,
          "unknown": 5,
          "byType": {
            "Apache-2.0": 45,
            "MIT": 23,
            "EPL-1.0": 8,
            "LGPL-2.1": 3,
            "BSD-3-Clause": 3,
            "unknown": 5
          }
        },
        
        "details": [
          {
            "groupId": "org.springframework.boot",
            "artifactId": "spring-boot-starter-web",
            "version": "3.2.0",
            "scope": "compile",
            "license": "Apache-2.0",
            "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
            "depth": 1
          },
          {
            "groupId": "com.h2database",
            "artifactId": "h2",
            "version": "2.2.224",
            "scope": "runtime",
            "license": "MPL-2.0 OR EPL-1.0",
            "licenseUrl": "https://h2database.com/html/license.html",
            "multiLicense": true,
            "depth": 1
          },
          {
            "groupId": "org.postgresql",
            "artifactId": "postgresql",
            "version": "42.7.1",
            "scope": "runtime",
            "license": "BSD-2-Clause",
            "licenseUrl": "https://opensource.org/licenses/BSD-2-Clause",
            "depth": 1
          },
          {
            "groupId": "commons-logging",
            "artifactId": "commons-logging",
            "version": "1.2",
            "scope": "compile",
            "license": "Apache-2.0",
            "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
            "depth": 3
          },
          {
            "groupId": "com.example.internal",
            "artifactId": "internal-lib",
            "version": "1.0.0",
            "scope": "compile",
            "license": "unknown",
            "licenseUrl": null,
            "depth": 1
          }
        ],
        
        "warnings": [
          {
            "severity": "HIGH",
            "artifact": "some-gpl-lib:some-artifact:2.1.0",
            "license": "GPL-3.0",
            "reason": "GPL-3.0 license - incompatible with commercial use without source code disclosure",
            "recommendation": "Replace with Apache-2.0 or MIT licensed alternative"
          },
          {
            "severity": "MEDIUM",
            "artifact": "com.example.internal:internal-lib:1.0.0",
            "license": "unknown",
            "reason": "License information not found in POM",
            "recommendation": "Add <licenses> section to POM"
          }
        ],
        
        "compliance": {
          "hasIncompatibleLicenses": true,
          "incompatibleCount": 1,
          "unknownCount": 5,
          "commerciallyViable": false,
          "requiresAttribution": true
        }
      }
    }
  ]
}
```

### Structure YAML Attendue

```yaml
modules:
  - artifactId: backend-service
    
    licenses:
      summary:
        total: 87
        identified: 82
        unknown: 5
        byType:
          Apache-2.0: 45
          MIT: 23
          EPL-1.0: 8
      
      details:
        - groupId: org.springframework.boot
          artifactId: spring-boot-starter-web
          version: 3.2.0
          license: Apache-2.0
          licenseUrl: https://www.apache.org/licenses/LICENSE-2.0
        
        - groupId: com.h2database
          artifactId: h2
          version: 2.2.224
          license: "MPL-2.0 OR EPL-1.0"
          multiLicense: true
      
      warnings:
        - severity: HIGH
          artifact: some-gpl-lib:some-artifact:2.1.0
          license: GPL-3.0
          reason: "GPL-3.0 license - incompatible with commercial use"
      
      compliance:
        hasIncompatibleLicenses: true
        commerciallyViable: false
```

### HTML Output Attendu

Section dans l'onglet "Licenses" avec :

1. **Graphique en camembert** : Distribution des licences (Apache-2.0: 45, MIT: 23, etc.)
2. **Tableau des warnings** : Liste des licences incompatibles avec sévérité (HIGH, MEDIUM, LOW)
3. **Tableau détaillé** : Toutes les dépendances avec leur licence
   - Colonnes : Artifact | Version | License | License URL | Depth
   - Filtrable par type de licence
   - Recherche par artifact
   - Tri par colonne
4. **Badge de compliance** : 
   - 🟢 "Commercially Viable" si aucune licence incompatible
   - 🔴 "License Issues Detected" si licences incompatibles
   - 🟡 "Unknown Licenses" si licences manquantes

### Use Cases

**Use Case 1 : Audit de Compliance**
```bash
# Générer le rapport avec licences
mvn deploy-manifest:generate -Ddescriptor.includeLicenses=true

# Extraire les licences GPL
jq '.modules[].licenses.details[] | select(.license | contains("GPL"))' descriptor.json

# Résultat attendu : Liste des dépendances GPL pour review légal
```

**Use Case 2 : Export pour Équipe Légale**
```bash
# Générer HTML pour partage
mvn deploy-manifest:generate \
  -Ddescriptor.includeLicenses=true \
  -Ddescriptor.generateHtml=true

# Résultat : Rapport HTML avec tableau des licences exportable en CSV
```

**Use Case 3 : CI/CD Pipeline - Bloquer sur GPL**
```bash
# Générer avec warnings
mvn deploy-manifest:generate \
  -Ddescriptor.includeLicenses=true \
  -Ddescriptor.licenseWarnings=true \
  -Ddescriptor.incompatibleLicenses=GPL-3.0,AGPL-3.0

# Parser le JSON pour vérifier warnings
if jq -e '.modules[].licenses.warnings | length > 0' descriptor.json; then
  echo "❌ Incompatible licenses detected!"
  exit 1
fi
```

### Sources de Données

Les licences peuvent être extraites de :
1. **POM de chaque dépendance** : Section `<licenses>`
2. **Maven Central metadata** : API REST pour récupérer les licences
3. **Cache local** : Fichier JSON avec mapping artifact → license
4. **Fallback** : Marquer comme "unknown" si non trouvé

### Notes Importantes

- Si une dépendance a plusieurs licences (ex: "MPL-2.0 OR EPL-1.0"), indiquer `multiLicense: true`
- Pour les transitives, indiquer la `depth` pour traçabilité
- Les warnings doivent être clairs et actionnables
- Le champ `compliance.commerciallyViable` doit être `false` si au moins une licence incompatible

---

## 📋 Feature 2 : Propriétés Maven Actives

### Exigences Fonctionnelles

1. **Extraction des propriétés Maven** définies dans le POM et settings.xml
2. **Propriétés système** (java.version, os.name, etc.)
3. **Propriétés d'environnement** (variables ENV utilisées)
4. **Profils Maven actifs**
5. **Filtrage des propriétés sensibles** (password, secret, token, etc.)

### Paramètres de Configuration

```xml
<configuration>
    <!-- Activer l'extraction des propriétés -->
    <includeProperties>true</includeProperties>
    
    <!-- Inclure les propriétés système -->
    <includeSystemProperties>true</includeSystemProperties>
    
    <!-- Inclure les variables d'environnement -->
    <includeEnvironmentVariables>false</includeEnvironmentVariables>
    
    <!-- Filtrer les propriétés sensibles -->
    <filterSensitiveProperties>true</filterSensitiveProperties>
    
    <!-- Patterns à exclure (case-insensitive) -->
    <propertyExclusions>
        <exclude>password</exclude>
        <exclude>secret</exclude>
        <exclude>token</exclude>
        <exclude>apikey</exclude>
        <exclude>api-key</exclude>
        <exclude>api_key</exclude>
    </propertyExclusions>
    
    <!-- Masquer les valeurs sensibles au lieu de les supprimer -->
    <maskSensitiveValues>true</maskSensitiveValues>
</configuration>
```

### CLI Usage

```bash
# Activer les propriétés
mvn deploy-manifest:generate -Ddescriptor.includeProperties=true

# Avec propriétés système
mvn deploy-manifest:generate \
  -Ddescriptor.includeProperties=true \
  -Ddescriptor.includeSystemProperties=true

# Avec variables d'environnement (attention aux secrets!)
mvn deploy-manifest:generate \
  -Ddescriptor.includeProperties=true \
  -Ddescriptor.includeEnvironmentVariables=true \
  -Ddescriptor.filterSensitiveProperties=true
```

### Structure JSON Attendue

```json
{
  "build": {
    "timestamp": "2025-11-13T14:30:00Z",
    "maven": {
      "version": "3.9.6",
      "home": "/usr/local/maven"
    },
    
    "properties": {
      "project": {
        "groupId": "com.example",
        "artifactId": "my-app",
        "version": "1.0.0",
        "name": "My Application",
        "description": "Example application",
        "build.sourceEncoding": "UTF-8",
        "build.finalName": "my-app"
      },
      
      "maven": {
        "compiler.source": "21",
        "compiler.target": "21",
        "compiler.release": "21",
        "test.skip": "false",
        "skipTests": "false"
      },
      
      "custom": {
        "spring-boot.version": "3.2.0",
        "spring-cloud.version": "2023.0.0",
        "docker.registry": "ghcr.io",
        "docker.namespace": "mycompany",
        "docker.image.tag": "${project.version}",
        "application.name": "backend-service",
        "database.host": "localhost",
        "database.port": "5432",
        "database.name": "mydb",
        "database.password": "***MASKED***",
        "api.key": "***MASKED***",
        "jwt.secret": "***MASKED***"
      },
      
      "system": {
        "java.version": "21.0.1",
        "java.vendor": "Oracle Corporation",
        "java.home": "/usr/lib/jvm/java-21",
        "os.name": "Linux",
        "os.version": "5.15.0-1042-azure",
        "os.arch": "amd64",
        "user.name": "jenkins",
        "user.home": "/home/jenkins",
        "user.timezone": "UTC",
        "file.encoding": "UTF-8"
      },
      
      "environment": {
        "CI": "true",
        "GITHUB_ACTIONS": "true",
        "GITHUB_REF": "refs/heads/main",
        "GITHUB_SHA": "a3f5b2c8d9e1f4a7",
        "GITHUB_REPOSITORY": "mycompany/my-app",
        "DATABASE_URL": "***MASKED***",
        "API_TOKEN": "***MASKED***"
      }
    },
    
    "profiles": {
      "active": ["prod", "docker", "jib"],
      "default": "dev",
      "available": ["dev", "prod", "docker", "jib", "local"]
    },
    
    "goals": {
      "default": "package",
      "executed": ["clean", "compile", "test", "package"]
    }
  }
}
```

### Structure YAML Attendue

```yaml
build:
  timestamp: "2025-11-13T14:30:00Z"
  maven:
    version: "3.9.6"
  
  properties:
    project:
      groupId: com.example
      artifactId: my-app
      version: 1.0.0
    
    maven:
      compiler.source: "21"
      compiler.target: "21"
    
    custom:
      spring-boot.version: "3.2.0"
      docker.registry: ghcr.io
      database.password: "***MASKED***"
    
    system:
      java.version: "21.0.1"
      os.name: Linux
  
  profiles:
    active: [prod, docker]
    available: [dev, prod, docker]
```

### HTML Output Attendu

Section dans l'onglet "Build Info" avec :

1. **Cartes de résumé** :
   - Maven Version
   - Java Version
   - Active Profiles
   - Build Timestamp

2. **Tableaux organisés par catégorie** :
   - **Project Properties** (groupId, version, etc.)
   - **Maven Properties** (compiler.source, etc.)
   - **Custom Properties** (spring-boot.version, docker.*, etc.)
   - **System Properties** (java.*, os.*, etc.)
   - **Environment Variables** (CI, GITHUB_*, etc.)

3. **Fonctionnalités** :
   - Recherche/filtre par nom de propriété
   - Afficher/masquer les valeurs sensibles (toggle)
   - Export en .properties ou .env format
   - Badge "X masked properties" si des valeurs masquées

4. **Visual indicators** :
   - 🔒 Icône pour propriétés masquées
   - 🔧 Icône pour propriétés custom
   - ⚙️ Icône pour propriétés système

### Use Cases

**Use Case 1 : Reproduire un Build**
```bash
# Générer avec propriétés
mvn deploy-manifest:generate -Ddescriptor.includeProperties=true

# Extraire les propriétés Maven
jq '.build.properties.maven' descriptor.json > maven.properties

# Utiliser pour reproduire le build
mvn clean package -Dmaven.compiler.source=21 -Dmaven.compiler.target=21
```

**Use Case 2 : Audit de Sécurité - Vérifier Secrets**
```bash
# Vérifier qu'aucun secret n'a fuité
grep -i "password\|secret\|token" descriptor.json

# Si trouvé autre que ***MASKED***, il y a un problème
```

**Use Case 3 : Documentation d'Environnement**
```bash
# Générer HTML pour l'équipe
mvn deploy-manifest:generate \
  -Ddescriptor.includeProperties=true \
  -Ddescriptor.generateHtml=true

# Résultat : Documentation complète de l'environnement de build
```

### Sources de Données

1. **Project Properties** : `MavenProject.getProperties()`
2. **System Properties** : `System.getProperties()`
3. **Environment Variables** : `System.getenv()`
4. **Active Profiles** : `MavenSession.getRequest().getActiveProfiles()`
5. **Settings.xml Properties** : `Settings.getProperties()`

### Notes Importantes

- **Sécurité** : TOUJOURS filtrer les propriétés sensibles par défaut
- **Masquage** : Remplacer par `***MASKED***` au lieu de supprimer (traçabilité)
- **Patterns sensibles** : password, secret, token, key, credentials, auth
- **Case-insensitive** : database.PASSWORD et database.password doivent être filtrés
- **Interpolation** : Afficher les valeurs interpolées (ex: `${project.version}` → `1.0.0`)

---

## 📋 Feature 3 : Plugins Maven Utilisés

### Exigences Fonctionnelles

1. **Liste des plugins Maven** utilisés dans le build
2. **Versions des plugins**
3. **Configuration des plugins** (sans données sensibles)
4. **Executions** (goals, phases)
5. **Détection de plugins obsolètes** (optionnel)
6. **Plugins de management** vs plugins effectifs

### Paramètres de Configuration

```xml
<configuration>
    <!-- Activer l'extraction des plugins -->
    <includePlugins>true</includePlugins>
    
    <!-- Inclure la configuration des plugins -->
    <includePluginConfiguration>true</includePluginConfiguration>
    
    <!-- Inclure les plugins de management (pluginManagement) -->
    <includePluginManagement>true</includePluginManagement>
    
    <!-- Vérifier les versions obsolètes -->
    <checkPluginUpdates>false</checkPluginUpdates>
    
    <!-- Filtrer les configurations sensibles -->
    <filterSensitivePluginConfig>true</filterSensitivePluginConfig>
</configuration>
```

### CLI Usage

```bash
# Activer les plugins
mvn deploy-manifest:generate -Ddescriptor.includePlugins=true

# Avec configuration des plugins
mvn deploy-manifest:generate \
  -Ddescriptor.includePlugins=true \
  -Ddescriptor.includePluginConfiguration=true

# Avec vérification des updates
mvn deploy-manifest:generate \
  -Ddescriptor.includePlugins=true \
  -Ddescriptor.checkPluginUpdates=true
```

### Structure JSON Attendue

```json
{
  "build": {
    "plugins": {
      "summary": {
        "total": 12,
        "withConfiguration": 8,
        "fromManagement": 5,
        "outdated": 3
      },
      
      "list": [
        {
          "groupId": "org.apache.maven.plugins",
          "artifactId": "maven-compiler-plugin",
          "version": "3.11.0",
          "source": "effective",
          "inherited": false,
          "phase": "compile",
          "goals": ["compile", "testCompile"],
          "configuration": {
            "source": "21",
            "target": "21",
            "release": "21",
            "encoding": "UTF-8",
            "showWarnings": true,
            "showDeprecation": true
          },
          "executions": [
            {
              "id": "default-compile",
              "phase": "compile",
              "goals": ["compile"]
            },
            {
              "id": "default-testCompile",
              "phase": "test-compile",
              "goals": ["testCompile"]
            }
          ]
        },
        {
          "groupId": "org.springframework.boot",
          "artifactId": "spring-boot-maven-plugin",
          "version": "3.2.0",
          "source": "effective",
          "inherited": false,
          "phase": "package",
          "goals": ["repackage"],
          "configuration": {
            "mainClass": "com.example.Application",
            "excludeDevtools": true,
            "executable": true,
            "layout": "JAR"
          },
          "executions": [
            {
              "id": "repackage",
              "phase": "package",
              "goals": ["repackage"]
            },
            {
              "id": "build-info",
              "phase": "generate-resources",
              "goals": ["build-info"]
            }
          ]
        },
        {
          "groupId": "com.google.cloud.tools",
          "artifactId": "jib-maven-plugin",
          "version": "3.4.0",
          "source": "effective",
          "inherited": false,
          "phase": "package",
          "goals": ["build"],
          "configuration": {
            "from": {
              "image": "eclipse-temurin:21-jre"
            },
            "to": {
              "image": "ghcr.io/mycompany/my-app",
              "tags": ["1.0.0", "latest"],
              "auth": {
                "username": "***MASKED***",
                "password": "***MASKED***"
              }
            },
            "container": {
              "jvmFlags": ["-Xms512m", "-Xmx1024m"],
              "ports": ["8080"],
              "creationTime": "USE_CURRENT_TIMESTAMP"
            }
          },
          "executions": [
            {
              "id": "build-and-push",
              "phase": "package",
              "goals": ["build"]
            }
          ]
        },
        {
          "groupId": "org.apache.maven.plugins",
          "artifactId": "maven-surefire-plugin",
          "version": "3.0.0",
          "source": "effective",
          "inherited": false,
          "phase": "test",
          "goals": ["test"],
          "configuration": {
            "skipTests": false,
            "testFailureIgnore": false,
            "parallel": "methods",
            "threadCount": 4
          },
          "outdated": {
            "current": "3.0.0",
            "latest": "3.2.5",
            "behind": 2
          }
        },
        {
          "groupId": "org.jacoco",
          "artifactId": "jacoco-maven-plugin",
          "version": "0.8.11",
          "source": "effective",
          "inherited": false,
          "goals": ["prepare-agent", "report"],
          "executions": [
            {
              "id": "jacoco-initialize",
              "phase": "initialize",
              "goals": ["prepare-agent"]
            },
            {
              "id": "jacoco-report",
              "phase": "test",
              "goals": ["report"]
            }
          ]
        },
        {
          "groupId": "org.apache.maven.plugins",
          "artifactId": "maven-deploy-plugin",
          "version": "3.1.1",
          "source": "management",
          "inherited": true
        }
      ],
      
      "management": [
        {
          "groupId": "org.apache.maven.plugins",
          "artifactId": "maven-deploy-plugin",
          "version": "3.1.1",
          "usedInBuild": false
        },
        {
          "groupId": "org.apache.maven.plugins",
          "artifactId": "maven-install-plugin",
          "version": "3.1.1",
          "usedInBuild": true
        }
      ],
      
      "outdated": [
        {
          "artifactId": "maven-surefire-plugin",
          "current": "3.0.0",
          "latest": "3.2.5",
          "releaseDate": "2024-09-15",
          "severity": "MEDIUM"
        },
        {
          "artifactId": "maven-compiler-plugin",
          "current": "3.11.0",
          "latest": "3.12.1",
          "releaseDate": "2024-10-20",
          "severity": "LOW"
        }
      ]
    }
  }
}
```

### Structure YAML Attendue

```yaml
build:
  plugins:
    summary:
      total: 12
      outdated: 3
    
    list:
      - groupId: org.apache.maven.plugins
        artifactId: maven-compiler-plugin
        version: 3.11.0
        phase: compile
        goals: [compile, testCompile]
        configuration:
          source: "21"
          target: "21"
      
      - groupId: org.springframework.boot
        artifactId: spring-boot-maven-plugin
        version: 3.2.0
        configuration:
          mainClass: com.example.Application
          excludeDevtools: true
      
      - groupId: com.google.cloud.tools
        artifactId: jib-maven-plugin
        version: 3.4.0
        configuration:
          to:
            image: ghcr.io/mycompany/my-app
            tags: [1.0.0, latest]
            auth:
              username: "***MASKED***"
    
    outdated:
      - artifactId: maven-surefire-plugin
        current: 3.0.0
        latest: 3.2.5
```

### HTML Output Attendu

Section dans l'onglet "Build Info" avec :

1. **Cartes de statistiques** :
   - Total Plugins: 12
   - Outdated: 3 (avec badge rouge si > 0)
   - From Management: 5

2. **Tableau des plugins** :
   - Colonnes : Plugin | Version | Phase | Goals | Configuration
   - Badge "OUTDATED" pour plugins obsolètes
   - Badge "FROM MANAGEMENT" pour plugins de pluginManagement
   - Expandable row pour voir la configuration complète
   - Filtrable par groupId

3. **Section "Outdated Plugins"** (si présente) :
   - Liste des plugins obsolètes
   - Version actuelle vs version latest
   - Date de release de la nouvelle version
   - Lien vers release notes
   - Bouton "Update Command" qui génère la commande Maven

4. **Visual indicators** :
   - 🔧 Icône pour plugins de build
   - 📦 Icône pour plugins de packaging
   - 🧪 Icône pour plugins de test
   - ⚠️ Badge rouge pour outdated

### Use Cases

**Use Case 1 : Audit de Build Pipeline**
```bash
# Générer avec plugins
mvn deploy-manifest:generate -Ddescriptor.includePlugins=true

# Lister tous les plugins
jq '.build.plugins.list[].artifactId' descriptor.json

# Résultat : Documentation complète du pipeline de build
```

**Use Case 2 : Détecter Plugins Obsolètes**
```bash
# Avec vérification des updates
mvn deploy-manifest:generate \
  -Ddescriptor.includePlugins=true \
  -Ddescriptor.checkPluginUpdates=true

# Extraire les plugins obsolètes
jq '.build.plugins.outdated[]' descriptor.json

# Résultat : Liste des plugins à mettre à jour
```

**Use Case 3 : Reproduire Configuration**
```bash
# Extraire configuration Spring Boot plugin
jq '.build.plugins.list[] | select(.artifactId == "spring-boot-maven-plugin") | .configuration' descriptor.json

# Résultat : Configuration exacte utilisée pour le build
```

**Use Case 4 : CI/CD - Bloquer sur Plugins Obsolètes**
```bash
# Vérifier si des plugins sont obsolètes
if jq -e '.build.plugins.outdated | length > 0' descriptor.json; then
  echo "⚠️ Outdated plugins detected. Please update."
fi
```

### Sources de Données

1. **Build Plugins** : `MavenProject.getBuild().getPlugins()`
2. **Plugin Management** : `MavenProject.getBuild().getPluginManagement().getPlugins()`
3. **Effective POM** : Pour résoudre les versions héritées
4. **Maven Central** : Pour vérifier les dernières versions (optionnel)

### Notes Importantes

- **Configuration sensible** : Filtrer username, password, token, apiKey dans les configs
- **Source** : Indiquer si le plugin vient de `effective` (utilisé) ou `management` (défini mais pas forcément utilisé)
- **Executions** : Important pour comprendre le lifecycle
- **Inherited** : Indiquer si le plugin vient du parent POM
- **Outdated check** : Optionnel car nécessite appel réseau à Maven Central

---

## 🎨 Intégration HTML

### Nouvel Onglet "Compliance"

Ajouter un onglet "Compliance" dans la navigation qui regroupe :

1. **Section Licenses** :
   - Graphique camembert des licences
   - Tableau des warnings
   - Liste complète des dépendances avec licences

2. **Section Build Properties** :
   - Cartes de résumé (Maven version, Java version, etc.)
   - Tableaux par catégorie (Project, Maven, Custom, System)
   - Toggle pour afficher/masquer valeurs sensibles

3. **Section Build Plugins** :
   - Statistiques des plugins
   - Tableau des plugins avec configuration
   - Alerte pour plugins obsolètes

### Exemple de Navigation

```
📊 Overview | 🔨 Build Info | 📦 Modules | 🌳 Dependencies | ⚖️ Compliance | 🌍 Environments | 📚 Assemblies
                                                              ↑ NOUVEAU
```

---

## 📊 Résumé des Paramètres

### Paramètres Globaux

```xml
<configuration>
    <!-- Feature 1: Licenses -->
    <includeLicenses>false</includeLicenses>
    <licenseWarnings>false</licenseWarnings>
    <incompatibleLicenses>GPL-3.0,AGPL-3.0,SSPL</incompatibleLicenses>
    <includeTransitiveLicenses>true</includeTransitiveLicenses>
    
    <!-- Feature 2: Properties -->
    <includeProperties>false</includeProperties>
    <includeSystemProperties>false</includeSystemProperties>
    <includeEnvironmentVariables>false</includeEnvironmentVariables>
    <filterSensitiveProperties>true</filterSensitiveProperties>
    <maskSensitiveValues>true</maskSensitiveValues>
    
    <!-- Feature 3: Plugins -->
    <includePlugins>false</includePlugins>
    <includePluginConfiguration>true</includePluginConfiguration>
    <includePluginManagement>true</includePluginManagement>
    <checkPluginUpdates>false</checkPluginUpdates>
    <filterSensitivePluginConfig>true</filterSensitivePluginConfig>
</configuration>
```

### CLI Complet

```bash
# Tout activer
mvn deploy-manifest:generate \
  -Ddescriptor.includeLicenses=true \
  -Ddescriptor.licenseWarnings=true \
  -Ddescriptor.includeProperties=true \
  -Ddescriptor.includeSystemProperties=true \
  -Ddescriptor.includePlugins=true \
  -Ddescriptor.checkPluginUpdates=true \
  -Ddescriptor.generateHtml=true
```

---

## ✅ Checklist d'Implémentation

### Feature 1: Licenses
- [ ] Extraire licences des POMs de dépendances
- [ ] Détecter licences multiples (OR, AND)
- [ ] Agréger par type de licence
- [ ] Générer warnings pour licences incompatibles
- [ ] Calculer compliance (commerciallyViable, etc.)
- [ ] Supporter depth pour transitives
- [ ] Générer section HTML avec graphique et tableau

### Feature 2: Properties
- [ ] Extraire propriétés du projet
- [ ] Extraire propriétés système
- [ ] Extraire variables d'environnement
- [ ] Filtrer patterns sensibles (password, secret, token, etc.)
- [ ] Masquer valeurs sensibles (***MASKED***)
- [ ] Extraire profils actifs
- [ ] Interpoler valeurs (${project.version} → 1.0.0)
- [ ] Générer section HTML avec tableaux par catégorie

### Feature 3: Plugins
- [ ] Extraire plugins effectifs du build
- [ ] Extraire plugin management
- [ ] Récupérer configuration des plugins
- [ ] Filtrer config sensible (username, password, etc.)
- [ ] Extraire executions (goals, phases)
- [ ] Vérifier versions obsolètes (optionnel)
- [ ] Générer section HTML avec tableau et alertes

### Général
- [ ] Tests unitaires pour chaque feature
- [ ] Documentation README mise à jour
- [ ] Exemples CLI dans la doc
- [ ] Validation JSON schema
- [ ] Performance acceptable (pas de ralentissement >30%)

---

## 🎯 Critères de Succès

1. **Licenses** : 
   - ✅ Toutes les licences sont identifiées ou marquées "unknown"
   - ✅ Warnings générés pour licences incompatibles
   - ✅ HTML affiche graphique + tableau lisible

2. **Properties** :
   - ✅ Aucune propriété sensible n'est exposée en clair
   - ✅ Toutes les catégories sont présentes (project, maven, custom, system)
   - ✅ HTML permet toggle show/hide des valeurs masquées

3. **Plugins** :
   - ✅ Tous les plugins effectifs sont listés
   - ✅ Configuration est lisible et complète
   - ✅ Plugins obsolètes sont identifiés (si option activée)
   - ✅ HTML affiche tableau avec configuration expandable

---

**Date** : Novembre 2025  
**Version Plugin** : 1.4.0
