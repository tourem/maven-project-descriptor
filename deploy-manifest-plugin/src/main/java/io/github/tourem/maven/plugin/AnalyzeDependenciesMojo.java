package io.github.tourem.maven.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.tourem.maven.descriptor.model.analysis.AnalyzedDependency;
import io.github.tourem.maven.descriptor.model.analysis.DependencyAnalysisResult;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.DefaultProjectDependencyAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalysis;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mojo(name = "analyze-dependencies", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class AnalyzeDependenciesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /** Output directory for analysis JSON. */
    @Parameter(property = "deployment.analysisOutputDir")
    private File analysisOutputDir;

    /** Output file name for analysis JSON. */
    @Parameter(property = "deployment.analysisOutputFile", defaultValue = "dependency-analysis.json")
    private String analysisOutputFile;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            ProjectDependencyAnalyzer analyzer = new DefaultProjectDependencyAnalyzer();
            ProjectDependencyAnalysis result = analyzer.analyze(project);

            List<AnalyzedDependency> unused = mapArtifacts(result.getUnusedDeclaredArtifacts());
            List<AnalyzedDependency> undeclared = mapArtifacts(result.getUsedUndeclaredArtifacts());

            DependencyAnalysisResult.RawResults raw = DependencyAnalysisResult.RawResults.builder()
                    .unused(unused)
                    .undeclared(undeclared)
                    .build();

            DependencyAnalysisResult.Summary summary = DependencyAnalysisResult.Summary.builder()
                    .totalDependencies(safeCount(project.getArtifacts()))
                    .directDependencies(safeCount(project.getDependencies()))
                    .transitiveDependencies(Math.max(0, safeCount(project.getArtifacts()) - safeCount(project.getDependencies())))
                    .issues(DependencyAnalysisResult.Issues.builder()
                            .unused(unused.size())
                            .undeclared(undeclared.size())
                            .totalIssues(unused.size() + undeclared.size())
                            .build())
                    .potentialSavings(estimateSavings(unused))
                    .build();

            DependencyAnalysisResult out = DependencyAnalysisResult.builder()
                    .timestamp(Instant.now())
                    .rawResults(raw)
                    .summary(summary)
                    .build();

            writeJson(out);
            getLog().info("Dependency analysis generated: " + getOutputPath());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to analyze dependencies", e);
        }
    }

    private int safeCount(Collection<?> c) {
        return c == null ? 0 : c.size();
    }

    private List<AnalyzedDependency> mapArtifacts(Set<Artifact> artifacts) {
        List<AnalyzedDependency> list = new ArrayList<>();
        if (artifacts == null) return list;
        for (Artifact a : artifacts) {
            AnalyzedDependency.AnalyzedDependencyBuilder b = AnalyzedDependency.builder()
                    .groupId(a.getGroupId())
                    .artifactId(a.getArtifactId())
                    .version(a.getVersion())
                    .scope(a.getScope());

            File file = resolveFile(a);
            if (file != null && file.isFile()) {
                b.metadata(AnalyzedDependency.Metadata.builder()
                        .sizeBytes(file.length())
                        .sizeKB(round(file.length() / 1024.0))
                        .sizeMB(round(file.length() / (1024.0 * 1024.0)))
                        .fileLocation(file.getAbsolutePath())
                        .sha256(sha256Hex(file))
                        .packaging(a.getType())
                        .build());
            }
            list.add(b.build());
        }
        return list;
    }

    private File resolveFile(Artifact a) {
        File f = a.getFile();
        if (f != null && f.exists()) return f;
        // fallback: try standard local repo layout
        try {
            String rel = a.getGroupId().replace('.', '/') + "/" + a.getArtifactId() + "/" + a.getVersion() +
                    "/" + a.getArtifactId() + "-" + a.getVersion() + (a.getClassifier() != null ? ("-" + a.getClassifier()) : "") + "." + a.getType();
            Path localRepo = Path.of(project.getProjectBuildingRequest().getLocalRepository().getBasedir());
            Path p = localRepo.resolve(rel);
            if (Files.exists(p)) return p.toFile();
        } catch (Exception ignored) {
        }
        return null;
    }

    private String sha256Hex(File file) {
        try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(file.toPath()), MessageDigest.getInstance("SHA-256"))) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) { /* consume */ }
            byte[] digest = dis.getMessageDigest().digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            getLog().warn("Failed to compute sha256 for " + file + ": " + e.getMessage());
            return null;
        }
    }

    private DependencyAnalysisResult.PotentialSavings estimateSavings(List<AnalyzedDependency> unused) {
        long bytes = 0L;
        for (AnalyzedDependency d : unused) {
            if (d.getMetadata() != null && d.getMetadata().getSizeBytes() != null) {
                bytes += d.getMetadata().getSizeBytes();
            }
        }
        return DependencyAnalysisResult.PotentialSavings.builder()
                .bytes(bytes)
                .kb(round(bytes / 1024.0))
                .mb(round(bytes / (1024.0 * 1024.0)))
                .build();
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private void writeJson(DependencyAnalysisResult out) throws IOException {
        File dir = analysisOutputDir != null ? analysisOutputDir : new File(project.getBuild().getDirectory());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create output dir: " + dir);
        }
        File file = new File(dir, analysisOutputFile);
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            mapper.writeValue(fos, out);
        }
    }

    private String getOutputPath() {
        File dir = analysisOutputDir != null ? analysisOutputDir : new File(project.getBuild().getDirectory());
        return new File(dir, analysisOutputFile).getAbsolutePath();
    }
}

