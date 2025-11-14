# SBOM (Software Bill of Materials) - Guide Complet

## 🎯 Qu'est-ce qu'un SBOM ?

### Définition Simple

Un **SBOM (Software Bill of Materials)** est comme la **liste d'ingrédients** sur un produit alimentaire, mais pour un logiciel.

C'est un **inventaire formel et structuré** de tous les composants logiciels (bibliothèques, dépendances, frameworks) qui constituent une application.

### Analogie Concrète

```
Produit Alimentaire                    Application Logicielle
─────────────────────────────────────────────────────────────
🍪 Cookies au chocolat                 📦 backend-service v1.0.0
─────────────────────────────────────────────────────────────
Ingrédients:                           Composants:
• Farine (500g)                        • Spring Boot 3.2.0
• Chocolat (200g)                      • PostgreSQL Driver 42.7.1
• Sucre (150g)                         • Jackson 2.15.3
• Œufs (2)                             • Hibernate 6.3.1
• Beurre (100g)                        • Lombok 1.18.30

Allergènes: Gluten, Œufs               Vulnérabilités: CVE-2024-1234
Valeurs nutritionnelles: ...           Licences: Apache-2.0, MIT
Origine: France                        Fournisseur: Maven Central
Date de fabrication: 12/11/2025        Build timestamp: 12/11/2025
```

---

## 🏛️ Pourquoi c'est Devenu Critique ?

### 1. **Contexte Réglementaire (2023-2025)**

#### 🇺🇸 États-Unis
**Executive Order 14028** (Mai 2021) de Biden :
- Obligation pour les fournisseurs du gouvernement américain
- SBOM requis pour tous les logiciels vendus au gouvernement fédéral
- NIST publie des standards officiels

#### 🇪🇺 Union Européenne  
**Cyber Resilience Act** (2024) :
- SBOM obligatoire pour produits avec composants numériques
- Responsabilité des fabricants sur la chaîne d'approvisionnement
- Sanctions financières jusqu'à 15M€ ou 2.5% du CA mondial

#### 🌍 Standards Internationaux
- **ISO/IEC 5962** : Standard SBOM publié en 2023
- **OpenSSF** : Recommandations pour la supply chain security

### 2. **Supply Chain Attacks (Attaques de la Chaîne d'Approvisionnement)**

#### Cas Réels Célèbres

**🔴 SolarWinds (2020)**
- Compromission d'une mise à jour logicielle
- 18,000 entreprises et agences gouvernementales affectées
- Coût estimé : > $100 milliards
- **Si SBOM existait** : Détection rapide du composant compromis

**🔴 Log4Shell / Log4j (2021)**
- Vulnérabilité critique dans Log4j 2.x
- Millions d'applications affectées mondialement
- **Problème** : Beaucoup d'entreprises ne savaient pas si elles utilisaient Log4j
- **Avec SBOM** : Identification immédiate des applications à risque

**🔴 Colors.js / Faker.js (2022)**
- Maintainer sabote ses propres packages npm
- Millions de builds cassés
- **Avec SBOM** : Traçabilité de la provenance du code

**🔴 XZ Utils Backdoor (2024)**
- Tentative d'injection de backdoor dans un outil Linux critique
- Détecté par chance
- **Avec SBOM** : Audit automatique des changements suspects

### 3. **Compliance & Audits**

Aujourd'hui, de plus en plus d'entreprises exigent un SBOM :
- **Assurances Cyber** : Réduction des primes
- **Certifications** : ISO 27001, SOC 2, FedRAMP
- **Contrats B2B** : Clause obligatoire dans les appels d'offres
- **Due Diligence** : M&A, audits de sécurité

---

## 📊 Les Deux Formats Standards

### 1. **CycloneDX** (Recommandé pour Maven/Java)

**Créé par** : OWASP (Open Web Application Security Project)  
**Format** : JSON, XML  
**Focus** : Sécurité applicative, DevSecOps  
**Adoption** : Très forte dans l'écosystème Java/Maven  

**Avantages** :
- ✅ Support natif des vulnérabilités (VEX - Vulnerability Exploitability eXchange)
- ✅ Métadonnées riches (licenses, hashes, pedigree)
- ✅ Support des services (APIs, microservices)
- ✅ Léger et moderne
- ✅ Excellente intégration Maven

**Exemple CycloneDX** :
```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.5",
  "serialNumber": "urn:uuid:3e671687-395b-41f5-a30f-a58921a69b79",
  "version": 1,
  "metadata": {
    "timestamp": "2025-11-13T10:00:00Z",
    "tools": [
      {
        "vendor": "tourem",
        "name": "deploy-manifest-plugin",
        "version": "1.5.0"
      }
    ],
    "component": {
      "type": "application",
      "bom-ref": "pkg:maven/com.example/backend-service@1.0.0",
      "group": "com.example",
      "name": "backend-service",
      "version": "1.0.0",
      "description": "Backend REST API",
      "licenses": [
        {
          "license": {
            "id": "Apache-2.0"
          }
        }
      ]
    }
  },
  "components": [
    {
      "type": "library",
      "bom-ref": "pkg:maven/org.springframework.boot/spring-boot-starter-web@3.2.0",
      "group": "org.springframework.boot",
      "name": "spring-boot-starter-web",
      "version": "3.2.0",
      "description": "Starter for building web applications",
      "licenses": [
        {
          "license": {
            "id": "Apache-2.0",
            "url": "https://www.apache.org/licenses/LICENSE-2.0"
          }
        }
      ],
      "hashes": [
        {
          "alg": "SHA-256",
          "content": "708f3f24abcd4af8d05a6d85b888ea98f9d5e45c67e1e4f3e5d2c4e0c8b9f3d2"
        }
      ],
      "purl": "pkg:maven/org.springframework.boot/spring-boot-starter-web@3.2.0",
      "externalReferences": [
        {
          "type": "website",
          "url": "https://spring.io/projects/spring-boot"
        },
        {
          "type": "issue-tracker",
          "url": "https://github.com/spring-projects/spring-boot/issues"
        },
        {
          "type": "vcs",
          "url": "https://github.com/spring-projects/spring-boot"
        }
      ]
    },
    {
      "type": "library",
      "bom-ref": "pkg:maven/org.postgresql/postgresql@42.7.1",
      "group": "org.postgresql",
      "name": "postgresql",
      "version": "42.7.1",
      "licenses": [
        {
          "license": {
            "id": "BSD-2-Clause"
          }
        }
      ],
      "hashes": [
        {
          "alg": "SHA-256",
          "content": "a3f5b2c8d9e1f4a7b6c5d8e9f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0"
        }
      ],
      "purl": "pkg:maven/org.postgresql/postgresql@42.7.1"
    }
  ],
  "dependencies": [
    {
      "ref": "pkg:maven/com.example/backend-service@1.0.0",
      "dependsOn": [
        "pkg:maven/org.springframework.boot/spring-boot-starter-web@3.2.0",
        "pkg:maven/org.postgresql/postgresql@42.7.1"
      ]
    }
  ],
  "vulnerabilities": [
    {
      "bom-ref": "vuln-1",
      "id": "CVE-2024-1234",
      "source": {
        "name": "NVD",
        "url": "https://nvd.nist.gov/vuln/detail/CVE-2024-1234"
      },
      "ratings": [
        {
          "source": {
            "name": "NVD"
          },
          "severity": "high",
          "score": 8.2,
          "method": "CVSSv3"
        }
      ],
      "description": "SQL Injection vulnerability in PostgreSQL driver",
      "affects": [
        {
          "ref": "pkg:maven/org.postgresql/postgresql@42.7.1"
        }
      ],
      "recommendation": "Upgrade to version 42.7.2 or higher"
    }
  ]
}
```

### 2. **SPDX (Software Package Data Exchange)**

**Créé par** : Linux Foundation  
**Format** : JSON, XML, YAML, Tag-Value  
**Focus** : Licenses, compliance légale  
**Adoption** : Standard ISO/IEC 5962:2023  

**Avantages** :
- ✅ Standard officiel ISO
- ✅ Excellent pour compliance légale
- ✅ Supporté par les grands acteurs (Microsoft, Google, etc.)
- ✅ Focus sur les licences

**Exemple SPDX** :
```json
{
  "spdxVersion": "SPDX-2.3",
  "dataLicense": "CC0-1.0",
  "SPDXID": "SPDXRef-DOCUMENT",
  "name": "backend-service-1.0.0",
  "documentNamespace": "https://example.com/sbom/backend-service-1.0.0",
  "creationInfo": {
    "created": "2025-11-13T10:00:00Z",
    "creators": [
      "Tool: deploy-manifest-plugin-1.5.0"
    ]
  },
  "packages": [
    {
      "SPDXID": "SPDXRef-Package-spring-boot-starter-web",
      "name": "spring-boot-starter-web",
      "versionInfo": "3.2.0",
      "supplier": "Organization: Spring",
      "downloadLocation": "https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-web/3.2.0/",
      "licenseConcluded": "Apache-2.0",
      "licenseDeclared": "Apache-2.0",
      "copyrightText": "Copyright 2023 Spring"
    }
  ],
  "relationships": [
    {
      "spdxElementId": "SPDXRef-DOCUMENT",
      "relatedSpdxElement": "SPDXRef-Package-spring-boot-starter-web",
      "relationshipType": "DEPENDS_ON"
    }
  ]
}
```

---

## 💼 Use Cases Concrets

### 1. **Réponse Rapide aux Vulnérabilités** 🚨

**Scénario** : CVE critique annoncée sur Log4j

**Sans SBOM** :
```
❓ Est-ce qu'on utilise Log4j ?
❓ Dans quelles applications ?
❓ Quelle version ?
❓ Direct ou transitif ?

⏱️ Temps de réponse : 2-3 jours
😰 Stress élevé
💰 Risque d'exploitation pendant la recherche
```

**Avec SBOM** :
```bash
# Recherche instantanée dans tous les SBOMs
grep -r "log4j" sboms/*.json

# Résultat en 2 secondes :
✅ backend-service v1.0.0 : log4j 2.14.1 (VULNERABLE)
✅ frontend-api v2.1.0 : log4j 2.17.0 (SAFE)
✅ batch-processor v1.5.0 : N/A

⏱️ Temps de réponse : 2 secondes
😌 Stress minimal
💰 Patch immédiat sur les apps concernées
```

### 2. **Audit de Compliance Légale** ⚖️

**Scénario** : Audit de licences avant acquisition (M&A)

**Sans SBOM** :
```
❓ Quelles licences sont utilisées ?
❓ Y a-t-il des licences GPL (copyleft) ?
❓ Quelles sont les obligations légales ?

⏱️ Temps d'audit : 2-4 semaines
💰 Coût : 50K€ - 100K€
🎲 Risque : Découverte de GPL après l'acquisition
```

**Avec SBOM** :
```bash
# Analyse instantanée
cyclonedx-cli analyze sbom.json --license-risk

# Résultat :
⚠️  GPL-3.0 detected: some-gpl-lib:2.1.0
⚠️  AGPL-3.0 detected: mongodb-driver:4.5.0
✅  45 Apache-2.0 dependencies
✅  23 MIT dependencies

⏱️ Temps d'audit : 1 heure
💰 Coût : Automatique
✅ Risque : Identifié avant l'acquisition
```

### 3. **Supply Chain Security** 🔒

**Scénario** : Vérifier l'intégrité des dépendances

**Sans SBOM** :
```
❓ Est-ce que mes dépendances sont celles attendues ?
❓ Quelqu'un a-t-il modifié un JAR ?
❓ Y a-t-il eu un "dependency confusion" attack ?

⏱️ Détection : Jamais (sauf si problème visible)
💰 Risque : Backdoor non détecté
```

**Avec SBOM (avec hashes)** :
```bash
# Vérifier l'intégrité
cyclonedx-cli verify sbom.json --check-hashes

# Résultat :
✅ spring-boot-starter-web@3.2.0 : SHA-256 OK
❌ postgresql@42.7.1 : SHA-256 MISMATCH!
    Expected: 708f3f24abcd4af8...
    Got:      XXXXXXXXXXXXXX...

🚨 ALERTE : Fichier modifié ou attaque supply chain !

⏱️ Détection : Immédiate
💰 Risque : Backdoor détecté avant déploiement
```

### 4. **Conformité Contractuelle B2B** 📝

**Scénario** : Client exige un SBOM dans le contrat

**Sans SBOM** :
```
❌ Pas de SBOM = Pas de contrat
💰 Perte de deal : 500K€ - 5M€
```

**Avec SBOM** :
```bash
# Génération automatique
mvn deploy-manifest:generate -Ddescriptor.generateSBOM=true

# Livrable
✅ backend-service-1.0.0-sbom.json (CycloneDX)
✅ backend-service-1.0.0-sbom.spdx.json (SPDX)

📧 Email au client : "SBOM ci-joint"
💰 Deal signé ✅
```

### 5. **Automatisation DevSecOps** 🤖

**Intégration dans le Pipeline CI/CD** :

```yaml
# .github/workflows/build.yml
name: Build & Security Scan

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Build
        run: mvn clean package
      
      - name: Generate SBOM
        run: mvn deploy-manifest:generate -Ddescriptor.generateSBOM=true
      
      - name: Scan SBOM for vulnerabilities
        run: |
          # Utiliser Grype, Trivy, ou Snyk
          grype sbom:descriptor-sbom.json --fail-on high
      
      - name: Upload SBOM to Dependency Track
        run: |
          curl -X POST "https://dtrack.company.com/api/v1/bom" \
            -H "X-Api-Key: ${{ secrets.DTRACK_API_KEY }}" \
            -F "bom=@descriptor-sbom.json"
      
      - name: Archive SBOM
        uses: actions/upload-artifact@v3
        with:
          name: sbom
          path: descriptor-sbom.json
```

**Résultat** :
- ✅ SBOM généré automatiquement à chaque build
- ✅ Scan de vulnérabilités automatique
- ✅ Build échoue si vulnérabilité critique
- ✅ SBOM archivé pour traçabilité

### 6. **Transparency & Trust** 🔍

**Pour les Clients Finaux** :

```
Logiciel Open Source ou Commercial

📦 Application v1.0.0
├─ 📄 SBOM.json (téléchargeable)
├─ ✅ "Voir les composants utilisés"
└─ 🔒 "Aucune vulnérabilité connue"

→ Confiance accrue
→ Transparence totale
→ Différenciation concurrentielle
```

---

## 🔧 Intégration dans Votre Plugin

### Configuration Proposée

```xml
<plugin>
    <groupId>io.github.tourem</groupId>
    <artifactId>deploy-manifest-plugin</artifactId>
    <version>1.5.0</version>
    <configuration>
        <!-- Activer SBOM -->
        <generateSBOM>true</generateSBOM>
        
        <!-- Format : cyclonedx (recommandé) ou spdx -->
        <sbomFormat>cyclonedx</sbomFormat>
        
        <!-- Version du format -->
        <sbomSpecVersion>1.5</sbomSpecVersion>
        
        <!-- Inclure les hashes (SHA-256) -->
        <includeHashes>true</includeHashes>
        
        <!-- Inclure les licences -->
        <includeLicenses>true</includeLicenses>
        
        <!-- Scan de vulnérabilités (optionnel, nécessite API) -->
        <scanVulnerabilities>false</scanVulnerabilities>
        <vulnerabilityDatabase>nvd</vulnerabilityDatabase>
        
        <!-- Fichier de sortie -->
        <sbomOutputFile>target/sbom.json</sbomOutputFile>
    </configuration>
</plugin>
```

### CLI Usage

```bash
# Générer SBOM CycloneDX
mvn deploy-manifest:generate -Ddescriptor.generateSBOM=true

# Générer SBOM SPDX
mvn deploy-manifest:generate \
  -Ddescriptor.generateSBOM=true \
  -Ddescriptor.sbomFormat=spdx

# Avec scan de vulnérabilités
mvn deploy-manifest:generate \
  -Ddescriptor.generateSBOM=true \
  -Ddescriptor.scanVulnerabilities=true
```

### Fichiers Générés

```
target/
├── descriptor.json              # Votre descripteur actuel
├── descriptor.html              # Vue HTML
├── descriptor-sbom.json         # SBOM CycloneDX
├── descriptor-sbom.spdx.json    # SBOM SPDX (si demandé)
└── descriptor-sbom.xml          # SBOM CycloneDX XML (optionnel)
```

---

## 📊 Outils qui Consomment les SBOMs

### 1. **Scanners de Vulnérabilités**

- **Grype** (Anchore) : Scanner gratuit et rapide
- **Trivy** (Aqua Security) : Scanner polyvalent
- **Snyk** : Plateforme commerciale populaire
- **OWASP Dependency-Check** : Gratuit, intégration Maven

**Exemple** :
```bash
# Scanner un SBOM avec Grype
grype sbom:descriptor-sbom.json

# Résultat :
NAME                    INSTALLED  VULNERABILITY   SEVERITY
postgresql              42.7.1     CVE-2024-1234   High
spring-boot-starter     3.2.0      CVE-2024-5678   Medium
```

### 2. **Plateformes de Gestion**

- **Dependency-Track** (OWASP) : Plateforme open-source de gestion SBOMs
- **JFrog Xray** : Analyse de dépendances et vulnérabilités
- **Sonatype Nexus IQ** : Gestion de supply chain security
- **GitHub Dependency Graph** : Intégration native GitHub

**Exemple Dependency-Track** :
```bash
# Upload SBOM to Dependency-Track
curl -X POST "https://dtrack.company.com/api/v1/bom" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -F "project=backend-service" \
  -F "bom=@descriptor-sbom.json"

# Dashboard affiche :
- 87 composants
- 3 vulnérabilités High
- 12 vulnérabilités Medium
- License compliance: 95%
```

### 3. **Outils de Compliance**

- **FOSSA** : Compliance légale et licences
- **Snyk License Compliance** : Détection de licences incompatibles
- **Black Duck** : Analyse de code open-source

### 4. **Registries & Artefacts**

- **Docker Hub** : Support SBOM pour images
- **GitHub Container Registry** : Attestations SBOM
- **AWS ECR** : Inspection de vulnérabilités via SBOM
- **Azure Container Registry** : Analyse de sécurité

---

## 💰 ROI (Return on Investment)

### Gains Quantifiables

| Aspect | Sans SBOM | Avec SBOM | Gain |
|--------|-----------|-----------|------|
| **Temps de réponse CVE** | 2-3 jours | 2 secondes | 99.9% |
| **Coût audit licences** | 50K€ | Automatique | 100% |
| **Risque breach supply chain** | Élevé | Faible | -80% |
| **Temps compliance B2B** | 2 semaines | 1 minute | 99.9% |
| **Coût assurance cyber** | Baseline | -10 à -30% | Variable |

### Valeur Business

**Pour les Startups** :
- ✅ Crédibilité auprès des investisseurs (due diligence)
- ✅ Requis pour certains appels d'offres
- ✅ Différenciation concurrentielle

**Pour les Scale-ups** :
- ✅ Conformité réglementaire (Cyber Resilience Act)
- ✅ Réduction des primes d'assurance
- ✅ Facilite les audits de sécurité

**Pour les Entreprises** :
- ✅ Gestion de risque supply chain
- ✅ Conformité SOC 2, ISO 27001
- ✅ Transparence pour les clients

---

## 🎯 Recommandation pour Votre Plugin

### Phase 1 : MVP (v1.5.0)

**Objectif** : Générer SBOM basique CycloneDX

**Features** :
- ✅ Format CycloneDX 1.5 (JSON)
- ✅ Liste des dépendances (directes + transitives)
- ✅ Licences (si disponibles)
- ✅ Hashes SHA-256 (optionnel)
- ✅ Métadonnées basiques (timestamp, tool, etc.)

**Effort** : Moyen (déjà 60% des données disponibles)  
**Impact** : Énorme (positionne le plugin comme leader)

### Phase 2 : Advanced (v1.6.0)

**Objectif** : Enrichir le SBOM avec données avancées

**Features** :
- ✅ Scan de vulnérabilités (intégration NVD API)
- ✅ Format SPDX en plus de CycloneDX
- ✅ Pedigree (provenance des composants)
- ✅ External references (VCS, website, docs)

### Phase 3 : Enterprise (v2.0.0)

**Objectif** : Plateforme complète de supply chain security

**Features** :
- ✅ Intégration Dependency-Track
- ✅ SBOM signing (signatures cryptographiques)
- ✅ VEX (Vulnerability Exploitability eXchange)
- ✅ SBOM diff (comparaison entre versions)

---

## 📚 Ressources Officielles

### Standards
- **CycloneDX** : https://cyclonedx.org/
- **SPDX** : https://spdx.dev/
- **NTIA SBOM** : https://www.ntia.gov/SBOM

### Outils
- **CycloneDX Maven Plugin** : https://github.com/CycloneDX/cyclonedx-maven-plugin
- **SPDX Maven Plugin** : https://github.com/spdx/spdx-maven-plugin
- **Grype** : https://github.com/anchore/grype
- **Dependency-Track** : https://dependencytrack.org/

### Réglementation
- **Executive Order 14028** : https://www.whitehouse.gov/briefing-room/presidential-actions/2021/05/12/executive-order-on-improving-the-nations-cybersecurity/
- **Cyber Resilience Act** : https://digital-strategy.ec.europa.eu/en/library/cyber-resilience-act

---

## 🎬 Conclusion

### SBOM en 3 Points

1. **Inventaire formel** de tous les composants logiciels
2. **Requis légalement** (EU, US) et contractuellement (B2B)
3. **Essentiel** pour supply chain security et réponse aux CVEs

### Valeur pour Votre Plugin

**Aujourd'hui** : Votre plugin est **très bon** pour la documentation de déploiement  
**Avec SBOM** : Votre plugin devient **indispensable** pour compliance et sécurité

**Positionnement** :
```
Maven Descriptor Plugin → Maven Descriptor & Security Plugin
         ou
Maven Deployment Plugin → Maven Supply Chain Security Platform
```

### Next Steps

1. ✅ **v1.4.0** : Licenses, Properties, Plugins (en cours)
2. 🎯 **v1.5.0** : SBOM CycloneDX basique (game-changer)
3. 🚀 **v2.0.0** : SBOM avancé + vulnerability scanning

**Impact attendu** : 📈 Adoption massive + 🏆 Reconnaissance communauté + 💼 Use cases enterprise

---

**TL;DR** : SBOM = Liste d'ingrédients pour logiciels. Obligatoire légalement, critique pour sécurité, énorme valeur ajoutée pour votre plugin ! 🚀
