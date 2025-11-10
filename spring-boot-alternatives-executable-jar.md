# Alternatives au Spring Boot Maven Plugin pour créer des JARs/WARs Exécutables

## ⚠️ Réponse Directe

**NON, le `maven-jar-plugin` seul NE PEUT PAS** remplacer le `spring-boot-maven-plugin` pour créer des JARs exécutables avec dépendances.

Le `maven-jar-plugin` ne fait que créer un JAR classique contenant uniquement vos classes compilées, **sans les dépendances**.

## 🎯 Alternatives Viables

Voici les **3 alternatives principales** au `spring-boot-maven-plugin` :

| Plugin | Complexité | Spring Boot | Recommandé |
|--------|------------|-------------|------------|
| **maven-shade-plugin** | Moyenne | ✅ Avec config spéciale | **OUI** |
| **maven-assembly-plugin** | Élevée | ⚠️ Complexe | Parfois |
| **onejar-maven-plugin** | Faible | ❌ Obsolète | NON |

---

## 1️⃣ Maven Shade Plugin (⭐ RECOMMANDÉ)

### Concept : "Uber JAR" par fusion

Le **maven-shade-plugin** fusionne toutes les dépendances dans un seul JAR en **extrayant et fusionnant** le contenu de chaque JAR de dépendance.

### ✅ Avantages
- ✅ Crée un vrai "flat" JAR (pas de JAR imbriqués)
- ✅ Support du "shading" (renommage de packages pour éviter les conflits)
- ✅ Fusion intelligente des fichiers de ressources
- ✅ Compatible avec Spring Boot **avec configuration spéciale**
- ✅ Très performant au démarrage (pas de custom classloader)

### ❌ Inconvénients
- ❌ Configuration plus complexe pour Spring Boot
- ❌ JAR final plus gros que spring-boot-maven-plugin
- ❌ Nécessite des "transformers" pour gérer les conflits de fichiers

---

### Configuration Standard (Non Spring Boot)

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <!-- Spécifier la classe Main -->
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.example.Application</mainClass>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Utilisation :**
```bash
mvn clean package
java -jar target/my-app-1.0.0.jar
```

---

### Configuration SPRING BOOT (⚠️ Configuration Spéciale Requise)

Pour Spring Boot, vous **DEVEZ** ajouter des transformers spéciaux pour gérer les fichiers META-INF :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <dependencies>
                <!-- Dépendance nécessaire pour Spring Boot -->
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>3.2.0</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <!-- Classe Main Spring Boot -->
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.example.MySpringBootApplication</mainClass>
                            </transformer>
                            
                            <!-- CRITIQUE pour Spring Boot : Fusionner spring.handlers -->
                            <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                                <resource>META-INF/spring.handlers</resource>
                            </transformer>
                            
                            <!-- CRITIQUE pour Spring Boot : Fusionner spring.schemas -->
                            <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                                <resource>META-INF/spring.schemas</resource>
                            </transformer>
                            
                            <!-- CRITIQUE pour Spring Boot : Fusionner spring.factories -->
                            <transformer implementation="org.springframework.boot.maven.PropertiesMergingResourceTransformer">
                                <resource>META-INF/spring.factories</resource>
                            </transformer>
                            
                            <!-- Fusionner les fichiers de services (SPI) -->
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                        </transformers>
                        
                        <!-- Exclure les signatures pour éviter les erreurs -->
                        <filters>
                            <filter>
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                </excludes>
                            </filter>
                        </filters>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 🔍 Pourquoi ces Transformers sont CRITIQUES ?

Spring Boot utilise des fichiers META-INF spéciaux pour la configuration :

| Fichier | Rôle | Problème sans Transformer |
|---------|------|---------------------------|
| `META-INF/spring.handlers` | Mappage XML namespace → handlers | ❌ Écrasement → beans non chargés |
| `META-INF/spring.schemas` | Mappage XML schema URI | ❌ Écrasement → erreurs parsing XML |
| `META-INF/spring.factories` | Auto-configuration Spring Boot | ❌ Écrasement → auto-config cassée |

**Sans ces transformers**, votre application Spring Boot :
- ❌ Ne trouve pas les beans
- ❌ Les controllers retournent 404
- ❌ L'auto-configuration ne fonctionne pas
- ❌ Les ressources statiques ne sont pas servies

---

## 2️⃣ Maven Assembly Plugin

### Concept : "Uber JAR" par assemblage configurable

Le **maven-assembly-plugin** est le plus flexible mais aussi le plus complexe. Il permet de créer des archives personnalisées (JAR, ZIP, TAR.GZ) selon un descripteur XML.

### ✅ Avantages
- ✅ Extrêmement flexible (configuration complète)
- ✅ Peut créer plusieurs formats (JAR, ZIP, TAR.GZ)
- ✅ Utilisé par des projets majeurs (Nacos, ZooKeeper, Kafka)
- ✅ Peut inclure des scripts, configs, docs

### ❌ Inconvénients
- ❌ Configuration très complexe (fichier descriptor XML séparé)
- ❌ Risque de conflits de noms de classes
- ❌ Pas optimal pour Spring Boot (complexe)
- ❌ Pas de "shading" (renommage de packages)

---

### Configuration avec Descripteur Intégré

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.6.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>com.example.Application</mainClass>
                    </manifest>
                </archive>
                <descriptorRefs>
                    <!-- Utilise le descripteur prédéfini jar-with-dependencies -->
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Résultat :**
```
target/my-app-1.0.0-jar-with-dependencies.jar
```

---

### Configuration Avancée avec Descripteur Personnalisé

**Créer `src/assembly/custom-assembly.xml` :**

```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.1.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.1.0 
          http://maven.apache.org/xsd/assembly-2.1.0.xsd">
    
    <id>distribution</id>
    
    <formats>
        <format>jar</format>
        <format>zip</format>
    </formats>
    
    <includeBaseDirectory>false</includeBaseDirectory>
    
    <!-- Inclure les dépendances -->
    <dependencySets>
        <dependencySet>
            <unpack>true</unpack> <!-- Extraire le contenu des JARs -->
            <scope>runtime</scope>
        </dependencySet>
    </dependencySets>
    
    <!-- Inclure les classes du projet -->
    <fileSets>
        <fileSet>
            <directory>${project.build.outputDirectory}</directory>
            <outputDirectory>/</outputDirectory>
        </fileSet>
    </fileSets>
</assembly>
```

**POM :**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.example.Application</mainClass>
            </manifest>
        </archive>
        <descriptors>
            <descriptor>src/assembly/custom-assembly.xml</descriptor>
        </descriptors>
    </configuration>
    <executions>
        <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## 3️⃣ OneJar Maven Plugin (❌ Obsolète)

### Concept : JAR dans JAR avec ClassLoader custom

**⚠️ NE PAS UTILISER** - Plugin obsolète et non maintenu depuis 2012.

---

## 🔥 Cas Spécial : Utiliser spring-boot-maven-plugin pour des Projets NON Spring Boot

### Surprenant mais POSSIBLE !

Le `spring-boot-maven-plugin` peut être utilisé pour packager **n'importe quel projet Java**, pas seulement Spring Boot !

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>3.2.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                    <configuration>
                        <!-- Spécifier votre classe Main (non Spring Boot) -->
                        <mainClass>com.example.MyPlainJavaApp</mainClass>
                        <!-- Désactiver les fonctionnalités Spring Boot -->
                        <layout>JAR</layout>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Avantages :**
- ✅ Structure JAR optimisée (JAR dans JAR)
- ✅ Démarrage rapide
- ✅ JAR plus petit que maven-shade-plugin
- ✅ Fonctionne même sans Spring Boot

---

## 📊 Comparaison Complète

### Structure du JAR Résultant

| Plugin | Structure | Taille | Démarrage |
|--------|-----------|--------|-----------|
| **spring-boot-maven-plugin** | JAR-in-JAR (imbriqué) | Moyenne | ⚡ Rapide |
| **maven-shade-plugin** | Flat JAR (tout fusionné) | Grande | ⚡⚡ Très rapide |
| **maven-assembly-plugin** | Flat JAR (tout fusionné) | Grande | ⚡⚡ Très rapide |

### Structure spring-boot-maven-plugin
```
my-app.jar
├── BOOT-INF/
│   ├── classes/           ← Vos classes
│   │   └── com/example/...
│   └── lib/               ← JARs des dépendances (non extraits)
│       ├── spring-boot-3.2.0.jar
│       ├── spring-core-6.1.0.jar
│       └── ...
├── META-INF/
│   └── MANIFEST.MF        ← Pointe vers JarLauncher
└── org/springframework/boot/loader/  ← Custom ClassLoader
```

### Structure maven-shade-plugin
```
my-app.jar
├── com/
│   └── example/           ← Vos classes
│       └── Application.class
├── org/
│   └── springframework/   ← Classes Spring extraites
│       └── ...
├── META-INF/
│   ├── MANIFEST.MF        ← Pointe vers votre Main
│   ├── spring.handlers    ← Fusionné de tous les JARs
│   ├── spring.schemas     ← Fusionné de tous les JARs
│   └── spring.factories   ← Fusionné de tous les JARs
└── (toutes les classes extraites et fusionnées)
```

---

## 🎯 Quel Plugin Choisir ?

### Pour Spring Boot (WAR ou JAR)

| Cas d'usage | Plugin Recommandé | Raison |
|-------------|-------------------|---------|
| **Projet Spring Boot standard** | `spring-boot-maven-plugin` | ✅ Optimisé, supporté, simple |
| **Besoin de shading** (renommer packages) | `maven-shade-plugin` | ✅ Évite conflits de versions |
| **CI/CD avec contraintes** | `maven-shade-plugin` | ✅ Flat JAR, plus compatible |
| **Distribution multi-format** | `maven-assembly-plugin` | ✅ ZIP, TAR.GZ + scripts |

### Pour Projets Non Spring Boot

| Cas d'usage | Plugin Recommandé |
|-------------|-------------------|
| **Simple application CLI** | `maven-shade-plugin` |
| **Distribution complexe** | `maven-assembly-plugin` |
| **Besoin de simplicité** | `spring-boot-maven-plugin` (oui !) |

---

## 🚀 Exemple Complet : Projet Spring Boot avec maven-shade-plugin

**pom.xml complet :**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>spring-boot-shade-demo</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Désactiver le repackage de spring-boot-maven-plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
            
            <!-- Utiliser maven-shade-plugin à la place -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-maven-plugin</artifactId>
                        <version>3.2.0</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.example.Application</mainClass>
                                </transformer>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                                    <resource>META-INF/spring.handlers</resource>
                                </transformer>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                                    <resource>META-INF/spring.schemas</resource>
                                </transformer>
                                <transformer implementation="org.springframework.boot.maven.PropertiesMergingResourceTransformer">
                                    <resource>META-INF/spring.factories</resource>
                                </transformer>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            </transformers>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

**Build et exécution :**
```bash
mvn clean package
java -jar target/spring-boot-shade-demo-1.0.0.jar
```

---

## 🔧 Génération de WAR Exécutable

### Avec spring-boot-maven-plugin

```xml
<packaging>war</packaging>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>3.2.0</version>
        </plugin>
    </plugins>
</build>
```

**Exécution :**
```bash
java -jar target/my-app.war
```

### Avec maven-shade-plugin (Plus complexe)

Pour WAR, il est **fortement recommandé** d'utiliser `spring-boot-maven-plugin` car :
- ❌ Shade ne gère pas bien la structure WAR
- ❌ Complexité excessive pour le servlet container embarqué
- ❌ Peu de bénéfices par rapport à spring-boot-maven-plugin

---

## ⚠️ Pièges Courants

### 1. Spring Boot + Shade : Oublier les Transformers
❌ **Erreur :** Controllers retournent 404, beans non trouvés
✅ **Solution :** Ajouter TOUS les transformers META-INF (voir config complète ci-dessus)

### 2. Conflits de versions de dépendances
❌ **Erreur :** `NoSuchMethodError`, `ClassNotFoundException`
✅ **Solution :** Utiliser le "shading" pour renommer les packages :

```xml
<configuration>
    <relocations>
        <relocation>
            <pattern>com.google.common</pattern>
            <shadedPattern>shaded.com.google.common</shadedPattern>
        </relocation>
    </relocations>
</configuration>
```

### 3. Signatures JAR invalides
❌ **Erreur :** `SecurityException: Invalid signature file digest`
✅ **Solution :** Exclure les fichiers de signature :

```xml
<filters>
    <filter>
        <artifact>*:*</artifact>
        <excludes>
            <exclude>META-INF/*.SF</exclude>
            <exclude>META-INF/*.DSA</exclude>
            <exclude>META-INF/*.RSA</exclude>
        </excludes>
    </filter>
</filters>
```

---

## 📚 Ressources

- [Maven Shade Plugin Documentation](https://maven.apache.org/plugins/maven-shade-plugin/)
- [Maven Assembly Plugin Documentation](https://maven.apache.org/plugins/maven-assembly-plugin/)
- [Spring Boot Executable JARs](https://docs.spring.io/spring-boot/specification/executable-jar/)
- [Spring Boot Alternative Single Jar Solutions](https://docs.spring.io/spring-boot/specification/executable-jar/alternatives.html)

---

## 🎯 Conclusion

| Question | Réponse |
|----------|---------|
| **Peut-on remplacer spring-boot-maven-plugin ?** | ✅ OUI avec maven-shade-plugin + config spéciale |
| **maven-jar-plugin suffit-il ?** | ❌ NON, ne gère pas les dépendances |
| **Quelle est la meilleure alternative ?** | `maven-shade-plugin` pour Spring Boot |
| **Peut-on utiliser spring-boot-maven-plugin hors Spring Boot ?** | ✅ OUI, étonnamment ! |

**Recommandation générale :**
- 🥇 **Spring Boot** → Garder `spring-boot-maven-plugin` (optimisé)
- 🥈 **Besoin de shading** → `maven-shade-plugin`
- 🥉 **Distribution complexe** → `maven-assembly-plugin`

---

**Besoin d'aide pour configurer votre projet ?** N'hésitez pas à demander ! 🚀
