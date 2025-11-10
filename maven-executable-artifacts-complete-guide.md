# Guide Complet : Plugins Maven pour Générer des Artefacts Exécutables

## 🎯 Objectif du Document

Ce document recense **TOUTES** les combinaisons de plugins Maven qui permettent de générer des **artefacts exécutables** :
- ✅ **JAR exécutables** (standalone applications)
- ✅ **WAR exécutables** (avec serveur embarqué)
- ✅ **EAR exécutables** (pour application servers)

**Cas d'usage :** Détection automatique des modules Maven déployables/exécutables dans un projet multi-modules.

---

## 📦 Table des Matières

1. [JAR Exécutables](#jar-exécutables)
   - spring-boot-maven-plugin
   - maven-shade-plugin
   - maven-assembly-plugin
   - maven-jar-plugin + maven-dependency-plugin
   - onejar-maven-plugin (obsolète)
2. [WAR Exécutables](#war-exécutables)
   - spring-boot-maven-plugin (WAR)
   - maven-war-plugin + Jetty/Tomcat embarqué
3. [EAR Exécutables](#ear-exécutables)
   - maven-ear-plugin
4. [Tableau Récapitulatif](#tableau-récapitulatif-complet)
5. [Patterns de Détection](#patterns-de-détection-pour-plugin-descriptor)

---

# JAR Exécutables

## 1. spring-boot-maven-plugin

### 📋 Description
Plugin officiel Spring Boot qui crée un JAR "fat" avec structure JAR-in-JAR.

### 🔍 Détection (pour votre plugin descriptor)

#### Pattern Maven
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <!-- version peut varier -->
</plugin>
```

#### Caractéristiques du JAR généré
```
Structure du JAR :
my-app.jar
├── BOOT-INF/
│   ├── classes/           ← Vos classes
│   └── lib/               ← Dépendances (JARs entiers)
├── META-INF/
│   └── MANIFEST.MF        
└── org/springframework/boot/loader/  ← Spring Boot Loader
```

#### MANIFEST.MF
```
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: com.example.Application
Spring-Boot-Version: 3.2.0
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
```

### 📝 Configuration Minimale

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>3.2.0</version>
            <!-- Pas de configuration nécessaire par défaut -->
        </plugin>
    </plugins>
</build>
```

### 📝 Configuration Complète

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>3.2.0</version>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>  ← Goal principal
            </goals>
            <configuration>
                <mainClass>com.example.Application</mainClass>
                <layout>JAR</layout>
                <executable>true</executable>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isSpringBootExecutable = 
    hasPlugin("org.springframework.boot", "spring-boot-maven-plugin") &&
    hasGoal("repackage") &&
    packaging.equals("jar");

// Fichiers à vérifier
- pom.xml contient spring-boot-maven-plugin
- JAR généré contient BOOT-INF/ directory
- MANIFEST.MF contient Main-Class: org.springframework.boot.loader.JarLauncher
```

---

## 2. maven-shade-plugin

### 📋 Description
Crée un "uber JAR" en fusionnant toutes les dépendances dans un JAR flat.

### 🔍 Détection (pour votre plugin descriptor)

#### Pattern Maven
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>shade</goal>  ← Goal à détecter
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Caractéristiques du JAR généré
```
Structure du JAR :
my-app.jar
├── com/
│   ├── example/           ← Vos classes
│   └── google/            ← Classes des dépendances (extraites)
├── org/
│   └── springframework/   ← Classes Spring (extraites)
└── META-INF/
    ├── MANIFEST.MF
    ├── spring.handlers    ← Fusionné
    ├── spring.schemas     ← Fusionné
    └── spring.factories   ← Fusionné
```

#### MANIFEST.MF
```
Main-Class: com.example.Application
```

### 📝 Configuration Minimale (Non Spring Boot)

```xml
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
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.example.Application</mainClass>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 📝 Configuration Spring Boot (Complète)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.1</version>
    <dependencies>
        <!-- IMPORTANT pour Spring Boot -->
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
                    <!-- Main class -->
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.example.Application</mainClass>
                    </transformer>
                    
                    <!-- CRITIQUE pour Spring Boot -->
                    <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                        <resource>META-INF/spring.handlers</resource>
                    </transformer>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                        <resource>META-INF/spring.schemas</resource>
                    </transformer>
                    <transformer implementation="org.springframework.boot.maven.PropertiesMergingResourceTransformer">
                        <resource>META-INF/spring.factories</resource>
                    </transformer>
                    
                    <!-- Services SPI -->
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                </transformers>
                
                <!-- Exclure les signatures -->
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
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isShadedExecutable = 
    hasPlugin("org.apache.maven.plugins", "maven-shade-plugin") &&
    hasGoal("shade") &&
    hasTransformer("ManifestResourceTransformer") &&
    packaging.equals("jar");

// Vérification mainClass
String mainClass = extractMainClassFromShadeConfig();

// Fichiers à vérifier
- pom.xml contient maven-shade-plugin avec goal shade
- Configuration contient ManifestResourceTransformer
- MANIFEST.MF contient Main-Class
```

---

## 3. maven-assembly-plugin

### 📋 Description
Crée des archives personnalisables (JAR, ZIP, TAR.GZ) avec dépendances.

### 🔍 Détection (pour votre plugin descriptor)

#### Pattern Maven
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>single</goal>  ← Goal à détecter
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Caractéristiques du JAR généré
```
Structure du JAR :
my-app-jar-with-dependencies.jar
├── com/
│   └── example/           ← Vos classes
├── org/
│   └── apache/            ← Classes des dépendances
└── META-INF/
    └── MANIFEST.MF
```

### 📝 Configuration avec Descripteur Prédéfini

```xml
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
            <!-- Descripteur prédéfini -->
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
    </configuration>
</plugin>
```

### 📝 Configuration avec Descripteur Personnalisé

**pom.xml :**
```xml
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.example.Application</mainClass>
            </manifest>
        </archive>
        <descriptors>
            <descriptor>src/assembly/executable-jar.xml</descriptor>
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

**src/assembly/executable-jar.xml :**
```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.1.0">
    <id>executable</id>
    <formats>
        <format>jar</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    
    <!-- Inclure les dépendances -->
    <dependencySets>
        <dependencySet>
            <unpack>true</unpack>
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

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isAssemblyExecutable = 
    hasPlugin("org.apache.maven.plugins", "maven-assembly-plugin") &&
    hasGoal("single") &&
    (hasDescriptorRef("jar-with-dependencies") || hasCustomDescriptor()) &&
    hasManifestMainClass();

// Fichiers à vérifier
- pom.xml contient maven-assembly-plugin
- Configuration contient descriptorRef ou descriptor
- archive/manifest/mainClass est défini
- JAR généré : {artifactId}-{version}-{descriptorId}.jar
```

---

## 4. maven-jar-plugin + maven-dependency-plugin ⭐ NOUVELLE MÉTHODE

### 📋 Description
Combinaison de plugins pour extraire les dépendances puis repackager dans un JAR flat.

### 🔍 Détection (pour votre plugin descriptor)

#### Pattern Maven (2 plugins combinés)
```xml
<!-- Plugin 1 : Extraire les dépendances -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>unpack-dependencies</goal>  ← Goal clé
            </goals>
        </execution>
    </executions>
</plugin>

<!-- Plugin 2 : Repackager avec maven-jar-plugin -->
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <classesDirectory>${project.build.directory}/unpack</classesDirectory>
        <archive>
            <manifestFile>...</manifestFile>
        </archive>
    </configuration>
</plugin>
```

#### Caractéristiques du JAR généré
```
Structure du JAR :
my-app.jar
├── com/
│   ├── example/           ← Vos classes
│   └── google/            ← Classes des dépendances (extraites)
├── org/
│   └── springframework/   ← Classes Spring (extraites)
└── META-INF/
    └── MANIFEST.MF
```

### 📝 Configuration Complète

```xml
<build>
    <plugins>
        <!-- 1. Extraire toutes les dépendances -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.6.1</version>
            <executions>
                <execution>
                    <id>unpack-dependencies</id>
                    <phase>prepare-package</phase>
                    <goals>
                        <goal>unpack-dependencies</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${project.build.directory}/unpack</outputDirectory>
                        <!-- Exclure les signatures -->
                        <excludes>META-INF/*.SF,META-INF/*.DSA,META-INF/*.RSA</excludes>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- 2. Copier vos classes compilées -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.3.1</version>
            <executions>
                <execution>
                    <id>copy-classes</id>
                    <phase>prepare-package</phase>
                    <goals>
                        <goal>copy-resources</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${project.build.directory}/unpack</outputDirectory>
                        <resources>
                            <resource>
                                <directory>${project.build.outputDirectory}</directory>
                            </resource>
                        </resources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- 3. Créer le JAR exécutable -->
        <plugin>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.4.2</version>
            <executions>
                <!-- Désactiver le JAR par défaut -->
                <execution>
                    <id>default-jar</id>
                    <phase>none</phase>
                </execution>
                
                <!-- Créer le JAR exécutable -->
                <execution>
                    <id>repackage-integration</id>
                    <phase>package</phase>
                    <goals>
                        <goal>jar</goal>
                    </goals>
                    <configuration>
                        <!-- Pointer vers le répertoire avec tout le contenu -->
                        <classesDirectory>${project.build.directory}/unpack</classesDirectory>
                        
                        <archive>
                            <!-- Option 1 : Utiliser un MANIFEST existant -->
                            <manifestFile>${project.build.directory}/unpack/META-INF/MANIFEST.MF</manifestFile>
                            
                            <!-- Option 2 : Générer le MANIFEST -->
                            <!-- <manifest>
                                <mainClass>com.example.Application</mainClass>
                                <addClasspath>false</addClasspath>
                            </manifest> -->
                        </archive>
                        
                        <excludes>
                            <!-- Exclusions optionnelles -->
                            <exclude>**/lib/log4j-to-slf4j-*.jar</exclude>
                            <exclude>**/lib/logback-classic-*.jar</exclude>
                        </excludes>
                        
                        <finalName>${project.build.finalName}</finalName>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 📝 Variante : Avec copy-dependencies au lieu de unpack

```xml
<!-- Alternative : Copier les JARs sans les extraire -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.6.1</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-dependencies</goal>  ← Copie au lieu d'extraire
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/lib</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>

<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.4.2</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.example.Application</mainClass>
                <addClasspath>true</addClasspath>
                <classpathPrefix>lib/</classpathPrefix>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isJarDependencyExecutable = 
    hasPlugin("org.apache.maven.plugins", "maven-dependency-plugin") &&
    (hasGoal("unpack-dependencies") || hasGoal("copy-dependencies")) &&
    hasPlugin("org.apache.maven.plugins", "maven-jar-plugin") &&
    hasCustomClassesDirectory() &&
    packaging.equals("jar");

// Vérifications spécifiques
- maven-dependency-plugin avec unpack-dependencies OU copy-dependencies
- maven-jar-plugin avec classesDirectory personnalisé
- Configuration de archive/manifest/mainClass
- Execution phase = prepare-package ou package
```

---

## 5. onejar-maven-plugin (❌ Obsolète - Pour Référence)

### 📋 Description
Plugin obsolète utilisant un classloader custom pour charger les JARs imbriqués.

### ⚠️ État
- ❌ Non maintenu depuis 2012
- ❌ Ne fonctionne pas avec Java 9+
- ❌ **NE PAS UTILISER** dans de nouveaux projets

### 📝 Configuration (Pour Référence)

```xml
<plugin>
    <groupId>com.jolira</groupId>
    <artifactId>onejar-maven-plugin</artifactId>
    <version>1.4.4</version>
    <executions>
        <execution>
            <goals>
                <goal>one-jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection (pour legacy projects)
boolean isOneJarExecutable = 
    hasPlugin("com.jolira", "onejar-maven-plugin") &&
    hasGoal("one-jar");

// Recommandation : Signaler comme obsolète
if (isOneJarExecutable) {
    warnings.add("onejar-maven-plugin is obsolete. Consider migrating to spring-boot-maven-plugin or maven-shade-plugin");
}
```

---

# WAR Exécutables

## 1. spring-boot-maven-plugin (WAR)

### 📋 Description
Crée un WAR exécutable avec serveur embarqué (Tomcat, Jetty, Undertow).

### 🔍 Détection (pour votre plugin descriptor)

#### Pattern Maven
```xml
<packaging>war</packaging>

<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

#### Caractéristiques du WAR généré
```
Structure du WAR :
my-app.war
├── WEB-INF/
│   ├── classes/           ← Vos classes
│   ├── lib/               ← Dépendances (JARs)
│   └── web.xml (optionnel)
├── META-INF/
│   └── MANIFEST.MF
└── org/springframework/boot/loader/  ← Spring Boot Loader
```

#### MANIFEST.MF
```
Main-Class: org.springframework.boot.loader.WarLauncher
Start-Class: com.example.Application
Spring-Boot-Version: 3.2.0
Spring-Boot-Classes: WEB-INF/classes/
Spring-Boot-Lib: WEB-INF/lib/
```

### 📝 Configuration

```xml
<project>
    <packaging>war</packaging>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Serveur embarqué (Tomcat par défaut) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <scope>provided</scope>  ← Important pour WAR traditionnel
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>3.2.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 📝 Classe Application pour WAR

```java
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isSpringBootWarExecutable = 
    hasPlugin("org.springframework.boot", "spring-boot-maven-plugin") &&
    packaging.equals("war") &&
    extendsSpringBootServletInitializer();

// Vérifications
- packaging = war
- spring-boot-maven-plugin présent
- MANIFEST.MF contient Main-Class: org.springframework.boot.loader.WarLauncher
- Classe principale extends SpringBootServletInitializer
```

### 🚀 Exécution

```bash
# Exécution standalone (serveur embarqué)
java -jar my-app.war

# OU déploiement traditionnel sur Tomcat/Jetty
cp my-app.war /tomcat/webapps/
```

---

## 2. maven-war-plugin + Serveur Embarqué

### 📋 Description
Crée un WAR avec serveur embarqué via Jetty ou Tomcat plugins.

### 🔍 Détection - Option A : Jetty Embedded

#### Pattern Maven
```xml
<packaging>war</packaging>

<plugin>
    <groupId>org.eclipse.jetty</groupId>
    <artifactId>jetty-maven-plugin</artifactId>
</plugin>
```

#### Configuration Jetty Embedded

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.4.0</version>
            <configuration>
                <failOnMissingWebXml>false</failOnMissingWebXml>
            </configuration>
        </plugin>
        
        <plugin>
            <groupId>org.eclipse.jetty</groupId>
            <artifactId>jetty-maven-plugin</artifactId>
            <version>11.0.18</version>
            <configuration>
                <webApp>
                    <contextPath>/</contextPath>
                </webApp>
                <httpConnector>
                    <port>8080</port>
                </httpConnector>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 🔍 Détection - Option B : Tomcat Embedded

#### Pattern Maven
```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
</plugin>
```

#### Configuration Tomcat Embedded

```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <port>8080</port>
        <path>/</path>
    </configuration>
</plugin>
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isEmbeddedServerWar = 
    packaging.equals("war") &&
    (hasPlugin("org.eclipse.jetty", "jetty-maven-plugin") ||
     hasPlugin("org.apache.tomcat.maven", "tomcat7-maven-plugin"));

// Note: Ces WARs ne sont PAS exécutables avec java -jar
// Ils nécessitent mvn jetty:run ou mvn tomcat7:run
```

---

## 3. maven-war-plugin Traditionnel (Non Exécutable)

### 📋 Description
Crée un WAR standard pour déploiement sur serveur d'application externe.

### Configuration

```xml
<packaging>war</packaging>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-war-plugin</artifactId>
            <version>3.4.0</version>
            <configuration>
                <failOnMissingWebXml>false</failOnMissingWebXml>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isTraditionalWar = 
    packaging.equals("war") &&
    !hasPlugin("org.springframework.boot", "spring-boot-maven-plugin") &&
    !hasPlugin("org.eclipse.jetty", "jetty-maven-plugin") &&
    !hasPlugin("org.apache.tomcat.maven", "tomcat7-maven-plugin");

// Ce WAR n'est PAS exécutable standalone
// Il nécessite un serveur d'application externe (Tomcat, JBoss, WebLogic, etc.)
```

---

# EAR Exécutables

## 1. maven-ear-plugin

### 📋 Description
Crée un EAR (Enterprise ARchive) pour déploiement sur serveur d'application Java EE.

### 🔍 Détection (pour votre plugin descriptor)

#### Pattern Maven
```xml
<packaging>ear</packaging>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-ear-plugin</artifactId>
</plugin>
```

#### Caractéristiques du EAR
```
Structure du EAR :
my-app.ear
├── META-INF/
│   ├── application.xml    ← Descripteur EAR
│   └── MANIFEST.MF
├── my-web.war             ← Module WAR
├── my-ejb.jar             ← Module EJB
└── lib/                   ← Dépendances partagées
    └── commons-*.jar
```

### 📝 Configuration

```xml
<project>
    <packaging>ear</packaging>
    
    <dependencies>
        <!-- Modules de l'application -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>my-web</artifactId>
            <version>1.0.0</version>
            <type>war</type>
        </dependency>
        
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>my-ejb</artifactId>
            <version>1.0.0</version>
            <type>ejb</type>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-ear-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <version>10</version>  <!-- Java EE version -->
                    <defaultLibBundleDir>lib</defaultLibBundleDir>
                    <modules>
                        <webModule>
                            <groupId>com.example</groupId>
                            <artifactId>my-web</artifactId>
                            <contextRoot>/myapp</contextRoot>
                        </webModule>
                        <ejbModule>
                            <groupId>com.example</groupId>
                            <artifactId>my-ejb</artifactId>
                        </ejbModule>
                    </modules>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 📝 application.xml généré

```xml
<?xml version="1.0" encoding="UTF-8"?>
<application xmlns="https://jakarta.ee/xml/ns/jakartaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/application_10.xsd"
             version="10">
    <display-name>my-app</display-name>
    
    <module>
        <web>
            <web-uri>my-web-1.0.0.war</web-uri>
            <context-root>/myapp</context-root>
        </web>
    </module>
    
    <module>
        <ejb>my-ejb-1.0.0.jar</ejb>
    </module>
    
    <library-directory>lib</library-directory>
</application>
```

### ✅ Critères de Détection (Plugin Descriptor)

```java
// Pattern de détection
boolean isEarApplication = 
    packaging.equals("ear") &&
    hasPlugin("org.apache.maven.plugins", "maven-ear-plugin");

// Vérifications
- packaging = ear
- maven-ear-plugin présent
- Contient des modules (WAR, EJB, JAR)
- Génère application.xml

// Note: Les EARs ne sont PAS exécutables standalone
// Ils nécessitent un serveur d'application:
// - WildFly / JBoss
// - WebLogic
// - WebSphere
// - GlassFish / Payara
```

### 🚀 Déploiement

```bash
# Déploiement sur WildFly
cp my-app.ear /opt/wildfly/standalone/deployments/

# Déploiement sur WebLogic
# Via console d'administration

# Déploiement sur WebSphere
# Via console d'administration
```

---

# Tableau Récapitulatif Complet

## JAR Exécutables

| Plugin(s) | Packaging | Structure JAR | Exécutable | Complexité | Spring Boot |
|-----------|-----------|---------------|------------|------------|-------------|
| **spring-boot-maven-plugin** | jar | JAR-in-JAR | ✅ `java -jar` | Faible | ✅ Optimisé |
| **maven-shade-plugin** | jar | Flat JAR | ✅ `java -jar` | Moyenne | ⚠️ Config spéciale |
| **maven-assembly-plugin** | jar | Flat JAR | ✅ `java -jar` | Élevée | ⚠️ Complexe |
| **maven-jar + maven-dependency** | jar | Flat JAR | ✅ `java -jar` | Moyenne | ⚠️ Config manuelle |
| **onejar-maven-plugin** ❌ | jar | JAR-in-JAR | ✅ `java -jar` | Faible | ❌ Obsolète |

## WAR Exécutables

| Plugin(s) | Packaging | Serveur Embarqué | Exécutable | Déploiement Serveur |
|-----------|-----------|------------------|------------|---------------------|
| **spring-boot-maven-plugin** | war | ✅ Tomcat/Jetty/Undertow | ✅ `java -jar` | ✅ Oui |
| **maven-war + jetty-maven-plugin** | war | ✅ Jetty | ❌ `mvn jetty:run` | ✅ Oui |
| **maven-war + tomcat7-maven-plugin** | war | ✅ Tomcat | ❌ `mvn tomcat7:run` | ✅ Oui |
| **maven-war-plugin (seul)** | war | ❌ Non | ❌ Non | ✅ Oui |

## EAR

| Plugin | Packaging | Serveur Requis | Exécutable Standalone |
|--------|-----------|----------------|----------------------|
| **maven-ear-plugin** | ear | ✅ JBoss/WebLogic/WebSphere | ❌ Non |

---

# Patterns de Détection pour Plugin Descriptor

## 🎯 Algorithme de Détection Complet

```java
public class ExecutableModuleDetector {
    
    /**
     * Détecte si un module Maven génère un artefact exécutable
     */
    public ExecutableInfo detectExecutable(MavenProject project) {
        String packaging = project.getPackaging();
        List<Plugin> plugins = project.getBuildPlugins();
        
        // JAR Exécutables
        if ("jar".equals(packaging)) {
            return detectExecutableJar(project, plugins);
        }
        
        // WAR Exécutables
        if ("war".equals(packaging)) {
            return detectExecutableWar(project, plugins);
        }
        
        // EAR (non exécutable standalone)
        if ("ear".equals(packaging)) {
            return detectEar(project, plugins);
        }
        
        return ExecutableInfo.notExecutable();
    }
    
    /**
     * Détecte les JARs exécutables
     */
    private ExecutableInfo detectExecutableJar(MavenProject project, List<Plugin> plugins) {
        // 1. Spring Boot Maven Plugin
        if (hasPlugin(plugins, "org.springframework.boot", "spring-boot-maven-plugin")) {
            return ExecutableInfo.builder()
                .type(ExecutableType.JAR)
                .method("spring-boot-maven-plugin")
                .executable(true)
                .structure("jar-in-jar")
                .mainClass(extractSpringBootMainClass(project))
                .launcherClass("org.springframework.boot.loader.JarLauncher")
                .build();
        }
        
        // 2. Maven Shade Plugin
        Plugin shadePlugin = findPlugin(plugins, "org.apache.maven.plugins", "maven-shade-plugin");
        if (shadePlugin != null && hasGoal(shadePlugin, "shade")) {
            return ExecutableInfo.builder()
                .type(ExecutableType.JAR)
                .method("maven-shade-plugin")
                .executable(true)
                .structure("flat-jar")
                .mainClass(extractShadeMainClass(shadePlugin))
                .transformers(extractTransformers(shadePlugin))
                .build();
        }
        
        // 3. Maven Assembly Plugin
        Plugin assemblyPlugin = findPlugin(plugins, "org.apache.maven.plugins", "maven-assembly-plugin");
        if (assemblyPlugin != null && hasGoal(assemblyPlugin, "single")) {
            return ExecutableInfo.builder()
                .type(ExecutableType.JAR)
                .method("maven-assembly-plugin")
                .executable(true)
                .structure("flat-jar")
                .mainClass(extractAssemblyMainClass(assemblyPlugin))
                .descriptors(extractAssemblyDescriptors(assemblyPlugin))
                .build();
        }
        
        // 4. Maven Jar + Maven Dependency Plugin
        Plugin dependencyPlugin = findPlugin(plugins, "org.apache.maven.plugins", "maven-dependency-plugin");
        Plugin jarPlugin = findPlugin(plugins, "org.apache.maven.plugins", "maven-jar-plugin");
        
        if (dependencyPlugin != null && jarPlugin != null) {
            boolean hasUnpackDeps = hasGoal(dependencyPlugin, "unpack-dependencies") ||
                                   hasGoal(dependencyPlugin, "copy-dependencies");
            boolean hasCustomClassesDir = hasCustomClassesDirectory(jarPlugin);
            
            if (hasUnpackDeps && hasCustomClassesDir) {
                return ExecutableInfo.builder()
                    .type(ExecutableType.JAR)
                    .method("maven-jar-plugin + maven-dependency-plugin")
                    .executable(true)
                    .structure("flat-jar")
                    .mainClass(extractJarPluginMainClass(jarPlugin))
                    .build();
            }
        }
        
        // 5. OneJar (obsolète)
        if (hasPlugin(plugins, "com.jolira", "onejar-maven-plugin")) {
            return ExecutableInfo.builder()
                .type(ExecutableType.JAR)
                .method("onejar-maven-plugin")
                .executable(true)
                .structure("jar-in-jar")
                .obsolete(true)
                .warning("onejar-maven-plugin is obsolete, consider migrating")
                .build();
        }
        
        // JAR non exécutable
        return ExecutableInfo.notExecutable();
    }
    
    /**
     * Détecte les WARs exécutables
     */
    private ExecutableInfo detectExecutableWar(MavenProject project, List<Plugin> plugins) {
        // 1. Spring Boot WAR
        if (hasPlugin(plugins, "org.springframework.boot", "spring-boot-maven-plugin")) {
            boolean extendsServletInitializer = checkSpringBootServletInitializer(project);
            
            return ExecutableInfo.builder()
                .type(ExecutableType.WAR)
                .method("spring-boot-maven-plugin")
                .executable(true)
                .embeddedServer("Tomcat/Jetty/Undertow")
                .mainClass(extractSpringBootMainClass(project))
                .launcherClass("org.springframework.boot.loader.WarLauncher")
                .servletInitializer(extendsServletInitializer)
                .build();
        }
        
        // 2. Jetty Embedded
        if (hasPlugin(plugins, "org.eclipse.jetty", "jetty-maven-plugin")) {
            return ExecutableInfo.builder()
                .type(ExecutableType.WAR)
                .method("jetty-maven-plugin")
                .executable(false)  // Nécessite mvn jetty:run
                .embeddedServer("Jetty")
                .runCommand("mvn jetty:run")
                .build();
        }
        
        // 3. Tomcat Embedded
        if (hasPlugin(plugins, "org.apache.tomcat.maven", "tomcat7-maven-plugin")) {
            return ExecutableInfo.builder()
                .type(ExecutableType.WAR)
                .method("tomcat7-maven-plugin")
                .executable(false)  // Nécessite mvn tomcat7:run
                .embeddedServer("Tomcat")
                .runCommand("mvn tomcat7:run")
                .build();
        }
        
        // WAR traditionnel (non exécutable)
        return ExecutableInfo.builder()
            .type(ExecutableType.WAR)
            .method("maven-war-plugin")
            .executable(false)
            .deploymentOnly(true)
            .requiresExternalServer(true)
            .build();
    }
    
    /**
     * Détecte les EARs
     */
    private ExecutableInfo detectEar(MavenProject project, List<Plugin> plugins) {
        if (hasPlugin(plugins, "org.apache.maven.plugins", "maven-ear-plugin")) {
            Plugin earPlugin = findPlugin(plugins, "org.apache.maven.plugins", "maven-ear-plugin");
            
            return ExecutableInfo.builder()
                .type(ExecutableType.EAR)
                .method("maven-ear-plugin")
                .executable(false)
                .deploymentOnly(true)
                .requiresExternalServer(true)
                .modules(extractEarModules(earPlugin))
                .javaEEVersion(extractJavaEEVersion(earPlugin))
                .build();
        }
        
        return ExecutableInfo.notExecutable();
    }
    
    // Helper methods
    private boolean hasPlugin(List<Plugin> plugins, String groupId, String artifactId) {
        return plugins.stream()
            .anyMatch(p -> groupId.equals(p.getGroupId()) && 
                          artifactId.equals(p.getArtifactId()));
    }
    
    private Plugin findPlugin(List<Plugin> plugins, String groupId, String artifactId) {
        return plugins.stream()
            .filter(p -> groupId.equals(p.getGroupId()) && 
                        artifactId.equals(p.getArtifactId()))
            .findFirst()
            .orElse(null);
    }
    
    private boolean hasGoal(Plugin plugin, String goal) {
        return plugin.getExecutions().stream()
            .flatMap(e -> e.getGoals().stream())
            .anyMatch(g -> goal.equals(g));
    }
}
```

---

## 📝 Classe ExecutableInfo

```java
@Data
@Builder
public class ExecutableInfo {
    
    // Type d'artefact
    private ExecutableType type;  // JAR, WAR, EAR
    
    // Méthode de génération
    private String method;  // Nom du plugin ou combinaison
    
    // Est-ce exécutable standalone ?
    private boolean executable;
    
    // Structure du JAR/WAR
    private String structure;  // jar-in-jar, flat-jar
    
    // Main Class
    private String mainClass;
    
    // Launcher Class (pour Spring Boot)
    private String launcherClass;
    
    // Serveur embarqué (pour WAR)
    private String embeddedServer;
    
    // Commande d'exécution
    private String runCommand;  // java -jar, mvn jetty:run, etc.
    
    // Nécessite un serveur externe ?
    private boolean requiresExternalServer;
    
    // Déploiement uniquement (pas exécutable standalone)
    private boolean deploymentOnly;
    
    // Modules EAR
    private List<String> modules;
    
    // Version Java EE
    private String javaEEVersion;
    
    // Transformers (pour Shade)
    private List<String> transformers;
    
    // Descripteurs (pour Assembly)
    private List<String> descriptors;
    
    // Servlet Initializer (pour Spring Boot WAR)
    private boolean servletInitializer;
    
    // Obsolète ?
    private boolean obsolete;
    
    // Warnings
    private String warning;
    
    public static ExecutableInfo notExecutable() {
        return ExecutableInfo.builder()
            .executable(false)
            .build();
    }
}

enum ExecutableType {
    JAR,
    WAR,
    EAR
}
```

---

## 🔍 Exemples d'Utilisation dans Votre Plugin Descriptor

### Exemple 1 : Génération du Descripteur

```java
ExecutableModuleDetector detector = new ExecutableModuleDetector();

for (MavenProject module : reactorProjects) {
    ExecutableInfo execInfo = detector.detectExecutable(module);
    
    if (execInfo.isExecutable()) {
        // Ajouter au descripteur JSON
        descriptor.addModule(ModuleDescriptor.builder()
            .artifactId(module.getArtifactId())
            .type(execInfo.getType())
            .executable(true)
            .method(execInfo.getMethod())
            .mainClass(execInfo.getMainClass())
            .runCommand(execInfo.getRunCommand())
            .build());
    }
}
```

### Exemple 2 : Descripteur JSON Généré

```json
{
  "project": {
    "groupId": "com.example",
    "artifactId": "my-project",
    "version": "1.0.0"
  },
  "modules": [
    {
      "artifactId": "api-service",
      "packaging": "jar",
      "executable": true,
      "type": "JAR",
      "method": "spring-boot-maven-plugin",
      "structure": "jar-in-jar",
      "mainClass": "com.example.api.Application",
      "launcherClass": "org.springframework.boot.loader.JarLauncher",
      "runCommand": "java -jar target/api-service-1.0.0.jar"
    },
    {
      "artifactId": "batch-processor",
      "packaging": "jar",
      "executable": true,
      "type": "JAR",
      "method": "maven-shade-plugin",
      "structure": "flat-jar",
      "mainClass": "com.example.batch.BatchApplication",
      "runCommand": "java -jar target/batch-processor-1.0.0.jar",
      "transformers": [
        "ManifestResourceTransformer",
        "AppendingTransformer",
        "PropertiesMergingResourceTransformer"
      ]
    },
    {
      "artifactId": "web-app",
      "packaging": "war",
      "executable": true,
      "type": "WAR",
      "method": "spring-boot-maven-plugin",
      "mainClass": "com.example.web.WebApplication",
      "launcherClass": "org.springframework.boot.loader.WarLauncher",
      "embeddedServer": "Tomcat",
      "servletInitializer": true,
      "runCommand": "java -jar target/web-app-1.0.0.war"
    },
    {
      "artifactId": "enterprise-app",
      "packaging": "ear",
      "executable": false,
      "type": "EAR",
      "method": "maven-ear-plugin",
      "deploymentOnly": true,
      "requiresExternalServer": true,
      "modules": [
        "web-app.war",
        "ejb-module.jar"
      ],
      "javaEEVersion": "10"
    }
  ]
}
```

---

## 🎯 Checklist de Détection Complète

### Pour JAR Exécutable

- [ ] `packaging = jar`
- [ ] Présence d'un des plugins :
  - [ ] `spring-boot-maven-plugin`
  - [ ] `maven-shade-plugin` avec goal `shade`
  - [ ] `maven-assembly-plugin` avec goal `single`
  - [ ] `maven-dependency-plugin` (unpack/copy) + `maven-jar-plugin`
- [ ] Configuration de `mainClass`
- [ ] MANIFEST.MF contient `Main-Class`

### Pour WAR Exécutable

- [ ] `packaging = war`
- [ ] Présence de :
  - [ ] `spring-boot-maven-plugin` (exécutable avec `java -jar`)
  - [ ] `jetty-maven-plugin` (exécutable avec `mvn jetty:run`)
  - [ ] `tomcat7-maven-plugin` (exécutable avec `mvn tomcat7:run`)
- [ ] Pour Spring Boot : classe extends `SpringBootServletInitializer`

### Pour EAR

- [ ] `packaging = ear`
- [ ] `maven-ear-plugin` présent
- [ ] ⚠️ **Non exécutable standalone** (nécessite serveur d'application)

---

## 📚 Références

### Documentation Officielle

- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/)
- [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/)
- [Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/)
- [Maven JAR Plugin](https://maven.apache.org/plugins/maven-jar-plugin/)
- [Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/)
- [Maven WAR Plugin](https://maven.apache.org/plugins/maven-war-plugin/)
- [Maven EAR Plugin](https://maven.apache.org/plugins/maven-ear-plugin/)
- [Jetty Maven Plugin](https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide/index.html#jetty-maven-plugin)

### Articles & Guides

- [Spring Boot Executable JARs](https://docs.spring.io/spring-boot/specification/executable-jar/)
- [Creating Fat/Uber JARs](https://maven.apache.org/plugins/maven-shade-plugin/examples/executable-jar.html)
- [Maven Assembly Descriptors](https://maven.apache.org/plugins/maven-assembly-plugin/assembly.html)

---

## 🎓 Conclusion

Ce document couvre **TOUS** les patterns de génération d'artefacts exécutables Maven :

✅ **JAR Exécutables** : 5 méthodes (dont 4 viables)
✅ **WAR Exécutables** : 3 méthodes
✅ **EAR** : 1 méthode (non exécutable standalone)

Utilisez ce document comme référence pour implémenter la **détection automatique** dans votre `descriptor-plugin` !

---

**Version du document :** 1.0  
**Dernière mise à jour :** 2025-11-10  
**Auteur :** Guide technique Maven
