package io.github.tourem.maven.descriptor.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to lookup available versions of Maven dependencies from configured repositories.
 * Uses maven-metadata.xml from JFrog/Nexus or other Maven repositories.
 *
 * @author tourem
 */
@Slf4j
public class DependencyVersionLookup {

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2";

    private final List<String> repositoryUrls;
    private final int timeoutMs;

    /**
     * Constructor with Maven Model to extract repository URLs.
     */
    public DependencyVersionLookup(Model model, int timeoutMs) {
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT_MS;
        this.repositoryUrls = extractRepositoryUrls(model);
    }

    /**
     * Constructor with explicit repository URLs.
     */
    public DependencyVersionLookup(List<String> repositoryUrls, int timeoutMs) {
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : DEFAULT_TIMEOUT_MS;
        this.repositoryUrls = repositoryUrls != null ? new ArrayList<>(repositoryUrls) : new ArrayList<>();
        if (this.repositoryUrls.isEmpty()) {
            this.repositoryUrls.add(MAVEN_CENTRAL);
        }
    }

    /**
     * Lookup available versions for a dependency, returning max 3 versions after the current version.
     *
     * @param groupId Maven groupId
     * @param artifactId Maven artifactId
     * @param currentVersion Current version in the project
     * @param maxVersions Maximum number of versions to return (default 3)
     * @return List of available versions after currentVersion, sorted descending (newest first)
     */
    public List<String> lookupAvailableVersions(String groupId, String artifactId,
                                                 String currentVersion, int maxVersions) {
        if (groupId == null || artifactId == null || currentVersion == null) {
            log.debug("Invalid parameters for version lookup: {}:{}:{}", groupId, artifactId, currentVersion);
            return Collections.emptyList();
        }

        int max = maxVersions > 0 ? maxVersions : 3;

        // Try each repository until we get results
        for (String repoUrl : repositoryUrls) {
            try {
                List<String> versions = fetchVersionsFromRepository(repoUrl, groupId, artifactId);
                if (versions != null && !versions.isEmpty()) {
                    return filterVersionsAfterCurrent(versions, currentVersion, max);
                }
            } catch (Exception e) {
                log.debug("Failed to fetch versions from {}: {}", repoUrl, e.getMessage());
            }
        }

        log.debug("No versions found for {}:{}:{}", groupId, artifactId, currentVersion);
        return Collections.emptyList();
    }

    /**
     * Fetch all versions from maven-metadata.xml in a repository.
     */
    private List<String> fetchVersionsFromRepository(String repoUrl, String groupId, String artifactId)
            throws Exception {
        String path = groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml";
        String url = repoUrl.endsWith("/") ? repoUrl + path : repoUrl + "/" + path;

        log.debug("Fetching versions from: {}", url);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("User-Agent", "deploy-manifest-plugin/2.4.0")
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String xml = new String(response.body(), StandardCharsets.UTF_8);
            return parseVersionsFromMetadata(xml);
        } else {
            log.debug("HTTP {} from {}", response.statusCode(), url);
            return Collections.emptyList();
        }
    }

    /**
     * Parse versions from maven-metadata.xml.
     */
    private List<String> parseVersionsFromMetadata(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        NodeList versionNodes = doc.getElementsByTagName("version");
        List<String> versions = new ArrayList<>();

        for (int i = 0; i < versionNodes.getLength(); i++) {
            Element versionElement = (Element) versionNodes.item(i);
            String version = versionElement.getTextContent().trim();
            if (!version.isEmpty()) {
                versions.add(version);
            }
        }

        log.debug("Parsed {} versions from metadata", versions.size());
        return versions;
    }



    /**
     * Filter versions to return only those after currentVersion, sorted descending.
     * Uses semantic version comparison when possible.
     */
    private List<String> filterVersionsAfterCurrent(List<String> allVersions, String currentVersion, int maxVersions) {
        // Filter out snapshots unless current is also snapshot
        boolean includeSnapshots = currentVersion.toUpperCase().contains("SNAPSHOT");

        List<String> filtered = allVersions.stream()
                .filter(v -> includeSnapshots || !v.toUpperCase().contains("SNAPSHOT"))
                .filter(v -> isVersionAfter(v, currentVersion))
                .collect(Collectors.toList());

        // Sort descending (newest first) using semantic version comparison
        filtered.sort((v1, v2) -> compareVersions(v2, v1));

        // Return max N versions
        return filtered.stream()
                .limit(maxVersions)
                .collect(Collectors.toList());
    }

    /**
     * Check if version1 is after version2.
     */
    private boolean isVersionAfter(String version1, String version2) {
        return compareVersions(version1, version2) > 0;
    }

    /**
     * Compare two versions semantically.
     * Returns: positive if v1 > v2, negative if v1 < v2, 0 if equal.
     *
     * Handles formats like: 1.2.3, 1.2.3-SNAPSHOT, 1.2.3.RELEASE, etc.
     */
    private int compareVersions(String v1, String v2) {
        if (v1.equals(v2)) return 0;

        // Normalize versions (remove qualifiers for comparison)
        String[] parts1 = normalizeVersion(v1).split("\\.");
        String[] parts2 = normalizeVersion(v2).split("\\.");

        int maxLen = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLen; i++) {
            int p1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int p2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }

        // If numeric parts are equal, compare qualifiers
        return compareQualifiers(v1, v2);
    }

    /**
     * Normalize version string for comparison.
     * Examples: 1.2.3-SNAPSHOT -> 1.2.3, 1.2.3.RELEASE -> 1.2.3
     */
    private String normalizeVersion(String version) {
        // Remove common qualifiers
        return version
                .replaceAll("(?i)-SNAPSHOT", "")
                .replaceAll("(?i)\\.RELEASE", "")
                .replaceAll("(?i)-RELEASE", "")
                .replaceAll("(?i)\\.Final", "")
                .replaceAll("(?i)-Final", "")
                .replaceAll("(?i)\\.GA", "")
                .replaceAll("(?i)-GA", "");
    }

    /**
     * Parse a version part as integer, handling non-numeric parts.
     */
    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            // For non-numeric parts, use hashCode for consistent ordering
            return 0;
        }
    }

    /**
     * Compare version qualifiers (SNAPSHOT < RELEASE < GA < Final).
     */
    private int compareQualifiers(String v1, String v2) {
        int q1 = getQualifierRank(v1);
        int q2 = getQualifierRank(v2);
        return Integer.compare(q1, q2);
    }

    /**
     * Get qualifier rank for ordering.
     */
    private int getQualifierRank(String version) {
        String upper = version.toUpperCase();
        if (upper.contains("SNAPSHOT")) return 0;
        if (upper.contains("ALPHA")) return 1;
        if (upper.contains("BETA")) return 2;
        if (upper.contains("RC")) return 3;
        if (upper.contains("M")) return 4; // Milestone
        if (upper.contains("RELEASE")) return 5;
        if (upper.contains("GA")) return 6;
        if (upper.contains("FINAL")) return 7;
        return 5; // Default to RELEASE level
    }

    /**
     * Extract repository URLs from Maven Model.
     */
    private List<String> extractRepositoryUrls(Model model) {
        List<String> urls = new ArrayList<>();

        if (model == null) {
            log.debug("No Maven model provided, using Maven Central only");
            urls.add(MAVEN_CENTRAL);
            return urls;
        }

        // Extract from <repositories>
        if (model.getRepositories() != null) {
            for (Repository repo : model.getRepositories()) {
                if (repo.getUrl() != null && !repo.getUrl().isEmpty()) {
                    urls.add(repo.getUrl());
                    log.debug("Added repository: {} ({})", repo.getId(), repo.getUrl());
                }
            }
        }

        // Extract from <pluginRepositories>
        if (model.getPluginRepositories() != null) {
            for (Repository repo : model.getPluginRepositories()) {
                if (repo.getUrl() != null && !repo.getUrl().isEmpty() && !urls.contains(repo.getUrl())) {
                    urls.add(repo.getUrl());
                    log.debug("Added plugin repository: {} ({})", repo.getId(), repo.getUrl());
                }
            }
        }

        // Always add Maven Central as fallback
        if (!urls.contains(MAVEN_CENTRAL)) {
            urls.add(MAVEN_CENTRAL);
        }

        log.info("Configured {} repositories for version lookup", urls.size());
        return urls;
    }
}
