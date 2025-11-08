# Descriptor Plugin - Guide d'utilisation

## Description

Le plugin Maven **Descriptor** génère automatiquement un descripteur JSON complet de votre projet Maven, incluant :
- Les modules déployables (JAR, WAR, EAR)
- Les exécutables Spring Boot
- Les configurations par environnement (dev, hml, prod)
- Les endpoints Actuator
- Les artefacts Maven Assembly
- Les métadonnées de déploiement

## Installation

Le plugin est disponible dans votre repository Maven local après installation.

```xml
<plugin>
    <groupId>com.larbotech</groupId>
    <artifactId>descriptor-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```

## Utilisation

### 1. Utilisation en ligne de commande

#### Génération simple (fichier à la racine du projet)
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate
```

Cela génère `descriptor.json` à la racine de votre projet.

#### Génération avec nom de fichier personnalisé
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.outputFile=deployment-info.json
```

#### Génération dans un répertoire spécifique
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.outputDirectory=target \
  -Ddescriptor.outputFile=deployment-descriptor.json
```

#### Désactiver le pretty print
```bash
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate \
  -Ddescriptor.prettyPrint=false
```

### 2. Configuration dans le POM

Vous pouvez configurer le plugin directement dans votre `pom.xml` :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.larbotech</groupId>
            <artifactId>descriptor-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <!-- Nom du fichier de sortie (défaut: descriptor.json) -->
                <outputFile>deployment-info.json</outputFile>

                <!-- Répertoire de sortie (défaut: racine du projet) -->
                <outputDirectory>target</outputDirectory>

                <!-- Pretty print JSON (défaut: true) -->
                <prettyPrint>true</prettyPrint>

                <!-- Skip l'exécution du plugin (défaut: false) -->
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

### 3. Exécution automatique pendant le build

Avec la configuration ci-dessus, le descripteur sera généré automatiquement lors de la phase `package` :

```bash
mvn clean package
```

## Paramètres de configuration

| Paramètre | Propriété système | Défaut | Description |
|-----------|------------------|--------|-------------|
| `outputFile` | `descriptor.outputFile` | `descriptor.json` | Nom du fichier de sortie |
| `outputDirectory` | `descriptor.outputDirectory` | Racine du projet | Répertoire de sortie (absolu ou relatif) |
| `prettyPrint` | `descriptor.prettyPrint` | `true` | Formater le JSON avec indentation |
| `skip` | `descriptor.skip` | `false` | Ignorer l'exécution du plugin |

## Exemple de sortie

```json
{
  "projectGroupId": "com.larbotech",
  "projectArtifactId": "github-actions-project",
  "projectVersion": "1.0-SNAPSHOT",
  "projectName": "github-actions-project",
  "projectDescription": "Projet multi-modules avec API REST et Batch",
  "generatedAt": [2025, 11, 9, 0, 20, 48, 83495000],
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
          "actuatorBasePath": "/actuator",
          "actuatorHealthPath": "/actuator/health",
          "actuatorInfoPath": "/actuator/info"
        }
      ],
      "assemblyArtifacts": [
        {
          "assemblyId": "distribution",
          "format": "zip",
          "repositoryPath": "com/larbotech/task-api/1.0-SNAPSHOT/task-api-1.0-SNAPSHOT.zip"
        }
      ],
      "mainClass": "com.larbotech.taskapi.TaskApiApplication",
      "buildPlugins": ["spring-boot-maven-plugin", "maven-assembly-plugin"]
    }
  ],
  "totalModules": 4,
  "deployableModulesCount": 3
}
```

## Cas d'usage

### CI/CD Pipeline

Utilisez le descripteur généré dans vos pipelines CI/CD pour automatiser le déploiement :

```yaml
# GitHub Actions example
- name: Generate deployment descriptor
  run: mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate

- name: Deploy using descriptor
  run: |
    DESCRIPTOR=$(cat descriptor.json)
    # Parse JSON and deploy modules
```

### Scripts de déploiement

```bash
#!/bin/bash
# deploy.sh

# Générer le descripteur
mvn com.larbotech:descriptor-plugin:1.0-SNAPSHOT:generate

# Parser et déployer chaque module
jq -r '.deployableModules[] | select(.springBootExecutable == true) | .artifactId' descriptor.json | while read module; do
    echo "Deploying $module..."
    # Logique de déploiement
done
```

## Fonctionnalités détectées

Le plugin détecte automatiquement :

✅ **Modules déployables** : JAR, WAR, EAR  
✅ **Spring Boot** : Exécutables, profils, configurations  
✅ **Environnements** : dev, hml, prod avec configurations spécifiques  
✅ **Actuator** : Endpoints health, info, métriques  
✅ **Maven Assembly** : Artefacts ZIP, TAR.GZ  
✅ **Métadonnées** : Version Java, classe principale, ports  

## Support

Pour toute question ou problème, veuillez créer une issue sur le repository GitHub.

