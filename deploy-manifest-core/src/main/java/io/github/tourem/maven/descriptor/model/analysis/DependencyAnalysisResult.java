package io.github.tourem.maven.descriptor.model.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DependencyAnalysisResult {

    @Builder.Default
    private String analyzer = "deploy-manifest-plugin";

    @Builder.Default
    private String baseAnalyzer = "maven-dependency-analyzer";

    @Builder.Default
    private Instant timestamp = Instant.now();

    private RawResults rawResults;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RawResults {
        private List<AnalyzedDependency> unused;       // declared but not used
        private List<AnalyzedDependency> undeclared;   // used but not declared
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Summary {
        private Integer totalDependencies;
        private Integer directDependencies;
        private Integer transitiveDependencies;
        private Issues issues;
        private PotentialSavings potentialSavings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Issues {
        private Integer unused;
        private Integer undeclared;
        private Integer totalIssues;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PotentialSavings {
        private Long bytes;
        private Double kb;
        private Double mb;
        private Double percentage; // optional if unknown
    }
}

