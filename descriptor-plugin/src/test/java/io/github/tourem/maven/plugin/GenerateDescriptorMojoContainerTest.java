package io.github.tourem.maven.plugin;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GenerateDescriptorMojoContainerTest {

    @TempDir
    Path tempDir;

    @Test
    void generates_container_block_for_jib_project_in_json_and_html() throws Exception {
        Path projectRoot = Files.createTempDirectory("jib-project-");
        writeString(projectRoot.resolve("pom.xml"), jibPom());

        GenerateDescriptorMojo mojo = new GenerateDescriptorMojo();
        MavenProject project = new MavenProject();
        project.setArtifactId("demo");
        project.setVersion("1.0.0");
        File pomFile = projectRoot.resolve("pom.xml").toFile();
        project.setFile(pomFile);
        setBasedir(project, pomFile.getParentFile());

        setField(mojo, "project", project);
        setField(mojo, "outputDirectory", tempDir.toString());
        setField(mojo, "outputFile", "descriptor.json");
        setField(mojo, "exportFormat", "json");
        setField(mojo, "generateHtml", true);
        setField(mojo, "attach", false);
        setField(mojo, "skip", false);

        mojo.execute();

        Path json = Paths.get(tempDir.toString(), "descriptor.json");
        Path html = Paths.get(tempDir.toString(), "descriptor.html");
        assertThat(json).exists();
        assertThat(html).exists();

        String jsonContent = Files.readString(json);
        assertThat(jsonContent).contains("\"container\"");
        assertThat(jsonContent).contains("\"tool\":\"jib\"");
        assertThat(jsonContent).contains("\"image\":\"ghcr.io/acme/demo\"");
        assertThat(jsonContent).contains("\"tag\":\"1.0.0\"");
        assertThat(jsonContent).contains("\"additionalTags\"");

        String htmlContent = Files.readString(html);
        assertThat(htmlContent).contains("Container Image");
        assertThat(htmlContent).contains("ghcr.io/acme/demo");
    }

    @Test
    void generates_container_block_for_spring_boot_build_image() throws Exception {
        Path projectRoot = Files.createTempDirectory("boot-project-");
        writeString(projectRoot.resolve("pom.xml"), springBootPom());

        GenerateDescriptorMojo mojo = new GenerateDescriptorMojo();
        MavenProject project = new MavenProject();
        project.setArtifactId("demo");
        project.setVersion("1.0.0");
        File pomFile = projectRoot.resolve("pom.xml").toFile();
        project.setFile(pomFile);
        setBasedir(project, pomFile.getParentFile());

        setField(mojo, "project", project);
        setField(mojo, "outputDirectory", tempDir.toString());
        setField(mojo, "outputFile", "descriptor.json");
        setField(mojo, "exportFormat", "json");
        setField(mojo, "generateHtml", true);
        setField(mojo, "attach", false);
        setField(mojo, "skip", false);

        mojo.execute();

        Path json = Paths.get(tempDir.toString(), "descriptor.json");
        Path html = Paths.get(tempDir.toString(), "descriptor.html");
        assertThat(json).exists();
        assertThat(html).exists();

        String jsonContent = Files.readString(json);
        assertThat(jsonContent).contains("\"container\"");
        assertThat(jsonContent).contains("\"tool\":\"spring-boot\"");
        assertThat(jsonContent).contains("\"image\":\"docker.io/acme/demo\"");
        assertThat(jsonContent).contains("\"tag\":\"1.0.0\"");
        assertThat(jsonContent).contains("\"builderImage\":\"paketobuildpacks/builder-jammy-base\"");
        assertThat(jsonContent).contains("\"runImage\":\"paketobuildpacks/run-jammy-base\"");
        assertThat(jsonContent).contains("\"publish\":true");

        String htmlContent = Files.readString(html);
        assertThat(htmlContent).contains("Container Image");
        assertThat(htmlContent).contains("docker.io/acme/demo");
    }

    // --- helpers ---

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void setBasedir(MavenProject project, File basedir) throws Exception {
        try {
            Field basedirField = MavenProject.class.getDeclaredField("basedir");
            basedirField.setAccessible(true);
            basedirField.set(project, basedir);
        } catch (NoSuchFieldException ignored) { }
    }

    private static void writeString(Path path, String content) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static String jibPom() {
        return "" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>com.example</groupId>\n" +
            "  <artifactId>demo</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "  <packaging>jar</packaging>\n" +
            "  <build>\n" +
            "    <plugins>\n" +
            "      <plugin>\n" +
            "        <groupId>com.google.cloud.tools</groupId>\n" +
            "        <artifactId>jib-maven-plugin</artifactId>\n" +
            "        <configuration>\n" +
            "          <to>\n" +
            "            <image>ghcr.io/acme/demo</image>\n" +
            "            <tags>\n" +
            "              <tag>1.0.0</tag>\n" +
            "              <tag>latest</tag>\n" +
            "            </tags>\n" +
            "          </to>\n" +
            "          <from>\n" +
            "            <image>eclipse-temurin:17-jre</image>\n" +
            "          </from>\n" +
            "        </configuration>\n" +
            "      </plugin>\n" +
            "    </plugins>\n" +
            "  </build>\n" +
            "</project>\n";
    }

    private static String springBootPom() {
        return "" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>com.example</groupId>\n" +
            "  <artifactId>demo</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "  <packaging>jar</packaging>\n" +
            "  <build>\n" +
            "    <plugins>\n" +
            "      <plugin>\n" +
            "        <groupId>org.springframework.boot</groupId>\n" +
            "        <artifactId>spring-boot-maven-plugin</artifactId>\n" +
            "        <executions>\n" +
            "          <execution>\n" +
            "            <goals><goal>build-image</goal></goals>\n" +
            "          </execution>\n" +
            "        </executions>\n" +
            "        <configuration>\n" +
            "          <image>\n" +
            "            <name>docker.io/acme/demo</name>\n" +
            "            <tags><tag>1.0.0</tag><tag>latest</tag></tags>\n" +
            "            <builder>paketobuildpacks/builder-jammy-base</builder>\n" +
            "            <runImage>paketobuildpacks/run-jammy-base</runImage>\n" +
            "            <publish>true</publish>\n" +
            "          </image>\n" +
            "        </configuration>\n" +
            "      </plugin>\n" +
            "    </plugins>\n" +
            "  </build>\n" +
            "</project>\n";
    }
}

