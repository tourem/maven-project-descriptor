# Dependency Versions & Consolidated Reports Feature

## 📋 Vue d'ensemble

Cette fonctionnalité ajoute deux capacités majeures au plugin :

1. **Lookup des versions disponibles** : Affiche les versions disponibles pour chaque dépendance
2. **Rapports consolidés** : Regroupe toutes les informations de dépendances et plugins dans des rapports dédiés

## 🎯 Objectifs

### Phase 1 : Versions Disponibles
- Aider les développeurs à identifier les mises à jour disponibles pour leurs dépendances
- Afficher max 3 versions après la version actuelle
- Utiliser les repositories configurés (JFrog/Nexus) sans API externe
- Fallback vers Maven Central si aucun repository configuré

### Phase 2 : Rapports Consolidés
- Regrouper dependency tree, analysis et plugin info dans un seul rapport
- Générer des rapports dédiés (JSON/YAML/HTML) séparés du manifest principal
- Faciliter l'analyse globale des dépendances et plugins d'un projet

## 🚀 Utilisation

### Analyse avec versions disponibles

```bash
# Analyse complète avec versions disponibles
mvn io.github.tourem:deploy-manifest-plugin:2.4.0-SNAPSHOT:analyze-dependencies

# Désactiver le lookup de versions
mvn analyze-dependencies -Ddescriptor.lookupAvailableVersions=false

# Personnaliser le nombre de versions
mvn analyze-dependencies -Ddescriptor.maxAvailableVersions=5

# Personnaliser le timeout
mvn analyze-dependencies -Ddescriptor.versionLookupTimeoutMs=10000
```

### Rapport consolidé

```bash
# Rapport consolidé (JSON + HTML)
mvn io.github.tourem:deploy-manifest-plugin:2.4.0-SNAPSHOT:dependency-report

# Formats personnalisés
mvn dependency-report -Ddependency.report.formats=json,yaml,html

# Répertoire de sortie personnalisé
mvn dependency-report -Ddependency.report.outputDir=reports

# Nom de fichier personnalisé
mvn dependency-report -Ddependency.report.outputFile=my-report

# Exclure certaines sections
mvn dependency-report \
  -Ddependency.report.includeAnalysis=false \
  -Ddependency.report.includeDependencyTree=true \
  -Ddependency.report.includePlugins=true
```

## 📊 Exemples de sortie

### 1. Versions disponibles (dependency-analysis.json)

```json
{
  "rawResults": {
    "unused": [
      {
        "groupId": "org.springframework.boot",
        "artifactId": "spring-boot-starter-web",
        "version": "3.3.4",
        "availableVersions": [
          "4.0.0-M1",
          "4.0.0-M2",
          "4.0.0-M3"
        ],
        "suspectedFalsePositive": true
      }
    ]
  }
}
```

### 2. Rapport consolidé (dependency-report.json)

```json
{
  "reportType": "dependency-report",
  "version": "1.0",
  "timestamp": "2025-11-15T18:25:16Z",
  "project": {
    "groupId": "com.larbotech",
    "artifactId": "analyse-dependencies-test",
    "version": "1.0.0",
    "packaging": "jar"
  },
  "dependencyTree": {
    "summary": {
      "total": 8,
      "direct": 8,
      "transitive": 0,
      "scopes": {
        "compile": 6,
        "runtime": 2
      }
    }
  },
  "analysis": {
    "healthScore": {
      "overall": 96,
      "grade": "A+",
      "breakdown": {
        "cleanliness": 95,
        "security": 100,
        "maintainability": 90,
        "licenses": 100
      }
    },
    "summary": {
      "issues": {
        "unused": 11,
        "undeclared": 0
      }
    }
  },
  "plugins": {
    "summary": {
      "totalPlugins": 40,
      "buildPlugins": 10,
      "managementPlugins": 30,
      "updatesAvailable": 0
    },
    "build": [
      {
        "groupId": "org.apache.maven.plugins",
        "artifactId": "maven-compiler-plugin",
        "version": "3.11.0"
      }
    ]
  }
}
```

## 🔧 Architecture technique

### Phase 1 : DependencyVersionLookup

**Service** : `DependencyVersionLookup.java`

**Fonctionnalités** :
- Extraction des URLs de repositories depuis `<repositories>` et `<pluginRepositories>`
- Requêtes HTTP vers `maven-metadata.xml`
- Parsing XML des versions disponibles
- Comparaison sémantique de versions (gère SNAPSHOT, RELEASE, GA, Final, etc.)
- Filtrage des versions après la version actuelle
- Timeout configurable (default: 5000ms)

**Algorithme de comparaison de versions** :
```
Version format: major.minor.patch-qualifier
Qualifiers order: SNAPSHOT < ALPHA < BETA < RC < M < RELEASE < GA < FINAL
```

### Phase 2 : Rapports Consolidés

**Modèle** : `DependencyReport.java`

**Structure** :
- `ProjectInfo` : Métadonnées du projet
- `DependencyTreeInfo` : Arbre complet des dépendances
- `DependencyAnalysisResult` : Résultats de l'analyse
- `PluginReport` : Informations sur les plugins

**Mojo** : `GenerateDependencyReportMojo.java`

**Formats de sortie** :
- JSON (Jackson avec pretty-print)
- YAML (SnakeYAML)
- HTML (template moderne avec CSS Grid)

## 🔍 Détails d'implémentation

### Extraction des repositories

Le service `DependencyVersionLookup` extrait les URLs de repositories depuis le `pom.xml` :

```java
// Extraction depuis <repositories>
if (model.getRepositories() != null) {
    for (Repository repo : model.getRepositories()) {
        if (repo.getUrl() != null) {
            urls.add(repo.getUrl());
        }
    }
}

// Extraction depuis <pluginRepositories>
if (model.getPluginRepositories() != null) {
    for (Repository repo : model.getPluginRepositories()) {
        if (repo.getUrl() != null) {
            urls.add(repo.getUrl());
        }
    }
}

// Fallback vers Maven Central
urls.add("https://repo1.maven.org/maven2");
```

### Requête HTTP vers maven-metadata.xml

```java
String path = groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml";
String url = repoUrl + "/" + path;

HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofMillis(timeoutMs))
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(url))
    .timeout(Duration.ofMillis(timeoutMs))
    .GET()
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

### Parsing XML

```xml
<!-- Exemple de maven-metadata.xml -->
<metadata>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <versioning>
    <latest>4.0.0-M3</latest>
    <release>3.3.5</release>
    <versions>
      <version>3.3.0</version>
      <version>3.3.1</version>
      <version>3.3.4</version>
      <version>4.0.0-M1</version>
      <version>4.0.0-M2</version>
      <version>4.0.0-M3</version>
    </versions>
  </versioning>
</metadata>
```

## 📈 Bénéfices

### Pour les développeurs
- ✅ Identification rapide des mises à jour disponibles
- ✅ Pas besoin de chercher manuellement sur Maven Central
- ✅ Respect des repositories d'entreprise (JFrog/Nexus)
- ✅ Vue consolidée de toutes les informations de dépendances

### Pour les Tech Leads
- ✅ Rapport unique pour auditer les dépendances et plugins
- ✅ Détection des dépendances obsolètes
- ✅ Planification des mises à jour
- ✅ Export en JSON/YAML pour intégration CI/CD

### Pour les DevOps
- ✅ Automatisation de la détection de mises à jour
- ✅ Intégration dans les pipelines CI/CD
- ✅ Rapports standardisés pour tous les projets
- ✅ Pas de dépendance à des APIs externes

## 🧪 Tests

### Test manuel sur projet de test

```bash
cd /Users/mtoure/dev/analyse-dependencies-test

# Test Phase 1
mvn io.github.tourem:deploy-manifest-plugin:2.4.0-SNAPSHOT:analyze-dependencies
cat target/dependency-analysis.json | jq '.rawResults.unused[0] | {groupId, artifactId, version, availableVersions}'

# Test Phase 2
mvn io.github.tourem:deploy-manifest-plugin:2.4.0-SNAPSHOT:dependency-report
cat target/dependency-report.json | jq '{reportType, project, dependencyTreeSummary: .dependencyTree.summary}'
open target/dependency-report.html
```

### Résultats attendus

**Phase 1** :
```json
{
  "groupId": "org.springframework.boot",
  "artifactId": "spring-boot-starter-web",
  "version": "3.3.4",
  "availableVersions": ["4.0.0-M1", "4.0.0-M2", "4.0.0-M3"]
}
```

**Phase 2** :
- ✅ `dependency-report.json` créé
- ✅ `dependency-report.html` créé
- ✅ Contient project info, dependency tree, analysis, plugins

## 🚧 Limitations connues

1. **Timeout** : Si un repository est lent, le timeout peut être atteint (configurable)
2. **Repositories privés** : Nécessite que les credentials soient configurés dans `settings.xml`
3. **Versions SNAPSHOT** : La comparaison de versions SNAPSHOT peut être imprécise
4. **Plugin updates** : La détection de mises à jour pour les plugins n'est pas encore implémentée (TODO)

## 🔮 Améliorations futures

1. **Plugin version lookup** : Ajouter le lookup de versions pour les plugins Maven
2. **Vulnerability check** : Intégrer avec des bases de données de vulnérabilités (CVE)
3. **License check** : Vérifier les licences des nouvelles versions
4. **Breaking changes detection** : Détecter les breaking changes entre versions (via changelog)
5. **HTML amélioré** : Ajouter des graphiques, tableaux interactifs, filtres
6. **Recommendations** : Suggérer automatiquement les mises à jour sûres
7. **Batch mode** : Analyser plusieurs projets en une seule commande

## 📝 Commits

### Commit 1 : Phase 1 - Version Lookup
```
feat(analysis): add available versions lookup for dependencies

- Created DependencyVersionLookup service (300 lines)
- Enhanced AnalyzedDependency model with availableVersions field
- Added 3 new parameters to AnalyzeDependenciesMojo
- Respects project's configured repositories (JFrog/Nexus)
- Falls back to Maven Central
- Semantic version comparison and filtering
- Shows max 3 newer versions after current version
```

### Commit 2 : Phase 2 - Consolidated Reports
```
refactor(reports): create dedicated dependency and plugin reports

- Created DependencyReport model (90 lines)
- Created GenerateDependencyReportMojo (267 lines)
- New Maven goal: dependency-report
- Generates JSON/YAML/HTML reports
- Consolidates dependency tree, analysis, and plugin info
- Separate from main deployment manifest
- Configurable output formats and location
```

## 🔗 Liens utiles

- **Branch** : `feat/dependency-versions`
- **Pull Request** : https://github.com/tourem/deploy-manifest-plugin/pull/new/feat/dependency-versions
- **Documentation principale** : [README.md](README.md)
- **Documentation française** : [doc.md](doc.md)
- **Documentation anglaise** : [doc-en.md](doc-en.md)

## 👥 Contributeurs

- **@tourem** - Implémentation complète (Phase 1 & 2)

## 📅 Historique

- **2025-11-15** : Implémentation Phase 1 (Version Lookup)
- **2025-11-15** : Implémentation Phase 2 (Consolidated Reports)
- **2025-11-15** : Push to GitHub (branch `feat/dependency-versions`)

