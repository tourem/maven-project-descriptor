package io.github.tourem.maven.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.tourem.maven.descriptor.model.DependencyReport;
import io.github.tourem.maven.descriptor.model.DependencyTreeInfo;
import io.github.tourem.maven.descriptor.model.analysis.DependencyAnalysisResult;
import io.github.tourem.maven.descriptor.service.DependencyTreeCollector;
import io.github.tourem.maven.descriptor.model.DependencyTreeOptions;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven goal to generate a comprehensive dependency and plugin report.
 *
 * @author tourem
 */
@Mojo(name = "dependency-report", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class GenerateDependencyReportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(property = "dependency.report.outputDir")
    private File outputDir;

    @Parameter(property = "dependency.report.outputFile", defaultValue = "dependency-report")
    private String outputFile;

    @Parameter(property = "dependency.report.formats", defaultValue = "json,html")
    private String formats;

    @Parameter(property = "dependency.report.includeAnalysis", defaultValue = "true")
    private boolean includeAnalysis;

    @Parameter(property = "dependency.report.includeDependencyTree", defaultValue = "true")
    private boolean includeDependencyTree;

    @Parameter(property = "dependency.report.includePlugins", defaultValue = "true")
    private boolean includePlugins;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            DependencyReport.DependencyReportBuilder builder = DependencyReport.builder()
                    .timestamp(Instant.now())
                    .project(buildProjectInfo());

            if (includeDependencyTree) {
                builder.dependencyTree(collectDependencyTree());
            }

            if (includeAnalysis) {
                builder.analysis(runDependencyAnalysis());
            }

            if (includePlugins) {
                builder.plugins(collectPluginInfo());
            }

            DependencyReport report = builder.build();

            String[] formatArray = formats.split(",");
            for (String format : formatArray) {
                format = format.trim().toLowerCase();
                switch (format) {
                    case "json":
                        writeJson(report);
                        break;
                    case "yaml":
                    case "yml":
                        writeYaml(report);
                        break;
                    case "html":
                        writeHtml(report);
                        break;
                    default:
                        getLog().warn("Unknown format: " + format);
                }
            }

            getLog().info("Dependency report generated: " + getOutputPath("json"));
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate dependency report", e);
        }
    }

    private DependencyReport.ProjectInfo buildProjectInfo() {
        return DependencyReport.ProjectInfo.builder()
                .groupId(project.getGroupId())
                .artifactId(project.getArtifactId())
                .version(project.getVersion())
                .packaging(project.getPackaging())
                .name(project.getName())
                .description(project.getDescription())
                .build();
    }

    private DependencyTreeInfo collectDependencyTree() {
        try {
            DependencyTreeCollector collector = new DependencyTreeCollector();
            DependencyTreeOptions options = DependencyTreeOptions.builder()
                    .include(true)
                    .depth(-1)
                    .format(io.github.tourem.maven.descriptor.model.DependencyTreeFormat.BOTH)
                    .build();
            return collector.collect(project.getModel(), project.getBasedir().toPath(), options);
        } catch (Exception e) {
            getLog().warn("Failed to collect dependency tree: " + e.getMessage());
            return null;
        }
    }

    private DependencyAnalysisResult runDependencyAnalysis() {
        File analysisFile = new File(getTargetDir(), "dependency-analysis.json");
        if (analysisFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                return mapper.readValue(analysisFile, DependencyAnalysisResult.class);
            } catch (Exception e) {
                getLog().warn("Failed to read existing analysis: " + e.getMessage());
            }
        }
        return null;
    }




    private DependencyReport.PluginReport collectPluginInfo() {
        List<DependencyReport.PluginInfo> buildPlugins = new ArrayList<>();
        List<DependencyReport.PluginInfo> managementPlugins = new ArrayList<>();

        if (project.getBuild() != null && project.getBuild().getPlugins() != null) {
            for (Plugin plugin : project.getBuild().getPlugins()) {
                buildPlugins.add(mapPlugin(plugin));
            }
        }

        if (project.getPluginManagement() != null &&
            project.getPluginManagement().getPlugins() != null) {
            for (Plugin plugin : project.getPluginManagement().getPlugins()) {
                managementPlugins.add(mapPlugin(plugin));
            }
        }

        DependencyReport.PluginSummary summary = DependencyReport.PluginSummary.builder()
                .totalPlugins(buildPlugins.size() + managementPlugins.size())
                .buildPlugins(buildPlugins.size())
                .managementPlugins(managementPlugins.size())
                .updatesAvailable(0)
                .build();

        return DependencyReport.PluginReport.builder()
                .build(buildPlugins)
                .management(managementPlugins)
                .summary(summary)
                .build();
    }

    private DependencyReport.PluginInfo mapPlugin(Plugin plugin) {
        return DependencyReport.PluginInfo.builder()
                .groupId(plugin.getGroupId())
                .artifactId(plugin.getArtifactId())
                .version(plugin.getVersion())
                .currentVersion(plugin.getVersion())
                .build();
    }

    private void writeJson(DependencyReport report) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        File outputFile = new File(getOutputPath("json"));
        outputFile.getParentFile().mkdirs();
        mapper.writeValue(outputFile, report);
        getLog().info("JSON report written to: " + outputFile.getAbsolutePath());
    }

    private void writeYaml(DependencyReport report) throws Exception {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        File outputFile = new File(getOutputPath("yaml"));
        outputFile.getParentFile().mkdirs();

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            yaml.dump(report, writer);
        }
        getLog().info("YAML report written to: " + outputFile.getAbsolutePath());
    }

    private void writeHtml(DependencyReport report) throws Exception {
        File outputFile = new File(getOutputPath("html"));
        outputFile.getParentFile().mkdirs();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Dependency Report - ").append(report.getProject().getArtifactId()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; }\n");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
        html.append(".summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }\n");
        html.append(".card { background: #ecf0f1; padding: 20px; border-radius: 5px; text-align: center; }\n");
        html.append(".card h3 { margin: 0; color: #7f8c8d; font-size: 14px; }\n");
        html.append(".card p { margin: 10px 0 0 0; font-size: 32px; font-weight: bold; color: #2c3e50; }\n");
        html.append("</style>\n</head>\n<body>\n<div class=\"container\">\n");
        html.append("<h1>📦 Dependency Report</h1>\n");
        html.append("<h2>Project: ").append(report.getProject().getArtifactId()).append("</h2>\n");
        html.append("<p><strong>Version:</strong> ").append(report.getProject().getVersion()).append("</p>\n");

        html.append("<div class=\"summary\">\n");
        if (report.getDependencyTree() != null && report.getDependencyTree().getSummary() != null) {
            html.append("<div class=\"card\"><h3>Total Dependencies</h3><p>")
                .append(report.getDependencyTree().getSummary().getTotal()).append("</p></div>\n");
        }
        if (report.getAnalysis() != null && report.getAnalysis().getHealthScore() != null) {
            html.append("<div class=\"card\"><h3>Health Score</h3><p>")
                .append(report.getAnalysis().getHealthScore().getOverall()).append("/100</p></div>\n");
        }
        html.append("</div>\n");

        html.append("</div>\n</body>\n</html>");

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(html.toString().getBytes(StandardCharsets.UTF_8));
        }
        getLog().info("HTML report written to: " + outputFile.getAbsolutePath());
    }

    private String getOutputPath(String extension) {
        File dir = outputDir != null ? outputDir : getTargetDir();
        return new File(dir, outputFile + "." + extension).getAbsolutePath();
    }

    private File getTargetDir() {
        return new File(project.getBuild().getDirectory());
    }
}
