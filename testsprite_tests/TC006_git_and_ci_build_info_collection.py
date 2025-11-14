import subprocess

def test_git_and_ci_build_info_collection():
    """
    Verify the collection of Git metadata such as commit, branch, tag, and remote information.
    Test the collection of CI environment details for traceability by running the Maven plugin goal 'generate'.
    This test relies on Maven Surefire/Failsafe plugin execution.
    """
    try:
        # Run Maven verify command to execute tests including those for the deploy-manifest-plugin
        result = subprocess.run(
            ["mvn", "-q", "-Dstyle.color=always", "verify"],
            capture_output=True,
            text=True,
            timeout=300  # 5 minutes to cover full build and plugin execution
        )
        
        # Assert Maven build success
        assert result.returncode == 0, f"Maven verify failed with exit code {result.returncode}.\nStdout:\n{result.stdout}\nStderr:\n{result.stderr}"
        
        # Check output for indication that Git and CI build info was collected
        # This may vary depending on plugin log output; use keywords from the PRD description
        output = result.stdout + result.stderr
        assert ("Git metadata" in output or "collect Git" in output or "GitInfoCollector" in output or "build info" in output), "Output does not indicate collection of Git metadata and CI build info."
        
        # Further validation that descriptor files produced - specifically check JSON/YAML descriptor files presence
        # Usually descriptor.json or descriptor.yaml should be generated in target directory
        import os
        target_dir = "target"
        json_path = os.path.join(target_dir, "descriptor.json")
        yaml_path = os.path.join(target_dir, "descriptor.yaml")
        html_path = os.path.join(target_dir, "descriptor.html")
        
        # At least one descriptor file should exist
        assert os.path.exists(json_path) or os.path.exists(yaml_path), "Descriptor files (JSON or YAML) not found in target directory after build."
        
        # Optionally check that HTML report is generated as the plugin supports it optionally
        has_html = os.path.exists(html_path)
        
        # Minimal validation on descriptor JSON content if exists
        if os.path.exists(json_path):
            import json
            with open(json_path, "r", encoding="utf-8") as f:
                descriptor = json.load(f)
            # Check keys related to git info and ci environment in descriptor if possible
            assert any(k in descriptor for k in ["git", "build", "ci", "buildInfo"]), "Descriptor JSON does not contain expected keys for Git/CI build info."
        
    except subprocess.TimeoutExpired:
        assert False, "Maven verify command timed out."
    except Exception as ex:
        assert False, f"Exception during test: {ex}"

test_git_and_ci_build_info_collection()