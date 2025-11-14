package io.github.tourem.maven.descriptor.model.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzedDependency {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;

    @Builder.Default
    private Metadata metadata = null;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Metadata {
        private Long sizeBytes;
        private Double sizeKB;
        private Double sizeMB;
        private String fileLocation;
        private String sha256;
        private String packaging;
    }
}

