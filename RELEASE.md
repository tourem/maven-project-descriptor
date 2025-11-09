# Guide de Release - Descriptor Plugin

Ce document décrit le processus de release automatisé pour le Descriptor Plugin.

## 📋 Vue d'ensemble

Le projet utilise **GitHub Actions** pour automatiser les releases vers **JFrog Artifactory**. Le workflow de release :

1. ✅ Compile et teste le projet
2. ✅ Définit la version de release
3. ✅ Déploie les artifacts vers JFrog Artifactory
4. ✅ Crée un tag Git
5. ✅ Calcule et définit automatiquement la prochaine version SNAPSHOT
6. ✅ Crée une GitHub Release

## 🔢 Gestion Automatique des Versions

Le workflow calcule automatiquement la prochaine version SNAPSHOT basée sur la version de release :

| Version de Release | Prochaine Version SNAPSHOT |
|-------------------|---------------------------|
| `1.0.0` | `1.1.0-SNAPSHOT` |
| `1.5.0` | `1.6.0-SNAPSHOT` |
| `2.0.0` | `2.1.0-SNAPSHOT` |
| `2.3.5` | `2.4.0-SNAPSHOT` |

**Logique** : Le workflow incrémente la **version mineure** et remet le patch à 0.

### Exemple de Calcul

```bash
# Version de release fournie
RELEASE_VERSION="1.2.3"

# Extraction : MAJOR=1, MINOR=2, PATCH=3
# Calcul : NEXT_MINOR = 2 + 1 = 3
# Résultat : NEXT_SNAPSHOT = "1.3.0-SNAPSHOT"
```

## 🚀 Comment Créer une Release

### Prérequis

1. **Accès JFrog Artifactory** :
   - URL JFrog Artifactory (ex: `https://myjfrog.com/artifactory`)
   - Nom d'utilisateur JFrog
   - Token API JFrog ou mot de passe

2. **Permissions GitHub** :
   - Accès en écriture au repository
   - Capacité à déclencher les workflows GitHub Actions

### Processus Étape par Étape

#### 1. Naviguer vers GitHub Actions

- Allez sur votre repository GitHub
- Cliquez sur l'onglet **Actions**
- Sélectionnez le workflow **Release Descriptor Plugin**

#### 2. Déclencher la Release

- Cliquez sur le bouton **Run workflow**
- Remplissez les paramètres requis :

| Paramètre | Description | Exemple |
|-----------|-------------|---------|
| **Release version** | Version à publier (format X.Y.Z) | `1.0.0` |
| **JFrog Artifactory URL** | URL de votre instance JFrog | `https://myjfrog.com/artifactory` |
| **JFrog username** | Votre nom d'utilisateur JFrog | `admin` |
| **JFrog token** | Votre token API JFrog | `AKCp8k...` |

#### 3. Surveiller la Release

- Le workflow s'exécute automatiquement
- Surveillez la progression dans l'onglet Actions
- Vérifiez les logs en cas d'erreur

#### 4. Vérifier la Release

- ✅ Vérifiez que les artifacts sont dans JFrog Artifactory
- ✅ Vérifiez que le tag Git a été créé : `v1.0.0`
- ✅ Vérifiez la page GitHub Releases
- ✅ Confirmez que le repository est maintenant sur la prochaine version SNAPSHOT

## 🔄 Détails du Workflow de Release

Le workflow GitHub Actions effectue les étapes suivantes :

```yaml
Étapes du Workflow :
├── 1. Checkout du code
├── 2. Configuration JDK 21
├── 3. Configuration Git (user, email)
├── 4. Calcul de la prochaine version SNAPSHOT
│   └── Exemple : 1.0.0 → 1.1.0-SNAPSHOT
├── 5. Définition de la version de release dans les POMs
├── 6. Build et tests (mvn clean verify)
├── 7. Configuration Maven settings pour JFrog
├── 8. Déploiement vers JFrog Artifactory
├── 9. Commit de la version de release + création du tag Git
├── 10. Définition de la prochaine version SNAPSHOT dans les POMs
├── 11. Commit de la prochaine version SNAPSHOT
├── 12. Push des changements et tags vers GitHub
└── 13. Création de la GitHub Release
```

### Fichier Workflow

Le workflow est défini dans `.github/workflows/release.yml` :

```yaml
name: Release Descriptor Plugin

on:
  workflow_dispatch:
    inputs:
      release_version:
        description: 'Release version (e.g., 1.0.0)'
        required: true
        type: string
      jfrog_url:
        description: 'JFrog Artifactory URL'
        required: true
        type: string
      jfrog_user:
        description: 'JFrog username'
        required: true
        type: string
      jfrog_token:
        description: 'JFrog token/password'
        required: true
        type: string
```

## 📦 Utilisation des Versions Publiées

Après une release réussie, vous pouvez utiliser le plugin dans vos projets :

### Dépendance Maven

```xml
<dependency>
    <groupId>com.larbotech</groupId>
    <artifactId>descriptor-plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Plugin Maven

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

### Ligne de Commande

```bash
mvn com.larbotech:descriptor-plugin:1.0.0:generate
```

## ⚙️ Configuration Maven Settings

Pour utiliser les artifacts depuis votre JFrog Artifactory, ajoutez ceci à votre `~/.m2/settings.xml` :

```xml
<settings>
    <servers>
        <server>
            <id>jfrog-releases</id>
            <username>VOTRE_USERNAME_JFROG</username>
            <password>VOTRE_TOKEN_JFROG</password>
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

## 📝 Bonnes Pratiques

### 1. Numérotation des Versions

Suivez le [Semantic Versioning](https://semver.org/) : `MAJOR.MINOR.PATCH`

- **MAJOR** : Changements incompatibles avec les versions précédentes
- **MINOR** : Nouvelles fonctionnalités (rétrocompatibles)
- **PATCH** : Corrections de bugs

### 2. Avant de Publier

- ✅ Assurez-vous que tous les tests passent
- ✅ Mettez à jour CHANGELOG.md avec les notes de release
- ✅ Revoyez et mergez toutes les PRs en attente
- ✅ Vérifiez que la version SNAPSHOT actuelle compile correctement

### 3. Après la Publication

- ✅ Vérifiez les artifacts dans JFrog Artifactory
- ✅ Testez la version publiée dans un projet exemple
- ✅ Mettez à jour la documentation si nécessaire
- ✅ Annoncez la release à votre équipe

## 🔧 Dépannage

### Le workflow échoue au déploiement

**Cause** : Identifiants JFrog invalides ou URL incorrecte

**Solution** :
- Vérifiez votre nom d'utilisateur JFrog
- Vérifiez votre token JFrog (pas expiré)
- Vérifiez l'URL JFrog (format : `https://domain.com/artifactory`)

### La version existe déjà dans Artifactory

**Cause** : Tentative de publier une version qui existe déjà

**Solution** :
- Utilisez un numéro de version différent
- Ou supprimez la version existante dans Artifactory (si autorisé)

### Le push Git échoue

**Cause** : Permissions insuffisantes ou règles de protection de branche

**Solution** :
- Vérifiez que le bot GitHub Actions a les permissions d'écriture
- Vérifiez que les règles de protection de branche autorisent les pushs depuis les workflows

### Les tests échouent pendant la release

**Cause** : Problèmes de code ou tests instables

**Solution** :
- Corrigez les tests qui échouent avant de réessayer la release
- Exécutez `mvn clean verify` localement pour reproduire le problème

### Erreur "Invalid version format"

**Cause** : Format de version incorrect

**Solution** :
- Utilisez le format `X.Y.Z` (ex: `1.0.0`, `2.3.5`)
- Ne pas inclure de préfixe `v` ou de suffixe `-SNAPSHOT`

## 📊 Workflow CI (Intégration Continue)

En plus du workflow de release, le projet dispose d'un workflow CI qui s'exécute automatiquement :

### Déclencheurs

- Push sur les branches `main` et `develop`
- Pull Requests vers `main` et `develop`

### Actions

1. Checkout du code
2. Configuration JDK 21
3. Build avec Maven (`mvn clean verify`)
4. Exécution des tests
5. Génération du rapport de tests
6. Upload des artifacts de build

### Fichier Workflow

Défini dans `.github/workflows/ci.yml`

## 🎯 Exemple Complet de Release

### Scénario

Vous voulez publier la version `1.0.0` du plugin.

### Étapes

1. **Vérification pré-release** :
   ```bash
   # Localement, vérifiez que tout compile
   mvn clean verify
   
   # Vérifiez la version actuelle
   grep "<version>" pom.xml
   # Devrait afficher : <version>1.0-SNAPSHOT</version>
   ```

2. **Déclenchement de la release** :
   - GitHub → Actions → Release Descriptor Plugin → Run workflow
   - Release version: `1.0.0`
   - JFrog URL: `https://myjfrog.com/artifactory`
   - JFrog user: `admin`
   - JFrog token: `AKCp8k...`

3. **Surveillance** :
   - Suivez l'exécution dans l'onglet Actions
   - Durée estimée : 2-5 minutes

4. **Vérification post-release** :
   ```bash
   # Pull les changements
   git pull origin main
   
   # Vérifiez la nouvelle version SNAPSHOT
   grep "<version>" pom.xml
   # Devrait afficher : <version>1.1.0-SNAPSHOT</version>
   
   # Vérifiez le tag
   git tag
   # Devrait inclure : v1.0.0
   ```

5. **Test de la version publiée** :
   ```bash
   # Dans un projet test
   mvn com.larbotech:descriptor-plugin:1.0.0:generate
   ```

## 📞 Support

Pour toute question ou problème avec le processus de release :

1. Consultez les logs du workflow GitHub Actions
2. Vérifiez la section Troubleshooting ci-dessus
3. Contactez l'équipe de développement

---

**Dernière mise à jour** : 2025-11-09

