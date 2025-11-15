package io.github.tourem.maven.descriptor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.tourem.maven.descriptor.model.analysis.DependencyAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Comprehensive dependency report combining dependency tree, analysis, and plugin information.
 * This is a dedicated report separate from the main deployment manifest.
 * 
 * @author tourem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DependencyReport {

    @Builder.Default
    private String reportType = "dependency-report";

    @Builder.Default
    private String version = "1.0";

    @Builder.Default
    private Instant timestamp = Instant.now();

    private ProjectInfo project;
    private DependencyTreeInfo dependencyTree;
    private DependencyAnalysisResult analysis;
    private PluginReport plugins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectInfo {
        private String groupId;
        private String artifactId;
        private String version;
        private String packaging;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PluginReport {
        private List<PluginInfo> build;
        private List<PluginInfo> management;
        private PluginSummary summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PluginInfo {
        private String groupId;
        private String artifactId;
        private String version;
        private String currentVersion;
        private String latestVersion;
        private Boolean updateAvailable;
        private List<String> goals;
        private String phase;
        private Object configuration; // Can be Map or filtered
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PluginSummary {
        private Integer totalPlugins;
        private Integer buildPlugins;
        private Integer managementPlugins;
        private Integer updatesAvailable;
    }
}

