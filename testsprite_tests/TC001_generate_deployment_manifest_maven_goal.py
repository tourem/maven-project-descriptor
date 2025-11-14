import subprocess
import os
import glob

def test_generate_deployment_manifest_maven_goal():
    # Run Maven verify goal with the deploy-manifest-plugin's 'generate' goal
    mvn_command = ["mvn", "-q", "-Dstyle.color=always", "verify"]
    try:
        result = subprocess.run(
            mvn_command,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            cwd=os.getcwd(),
            timeout=300,
            text=True,
            check=True
        )
    except subprocess.CalledProcessError as e:
        raise AssertionError(f"Maven build failed: {e.stderr.strip()}") from e
    except subprocess.TimeoutExpired as e:
        raise AssertionError("Maven build timed out") from e

    # Verify JSON descriptor file exists in target directory
    json_files = glob.glob("target/deploy-manifest/descriptor.json")
    assert json_files, "JSON descriptor file 'descriptor.json' was not generated."

    # Verify YAML descriptor file exists in target directory
    yaml_files = glob.glob("target/deploy-manifest/descriptor.yaml")
    assert yaml_files, "YAML descriptor file 'descriptor.yaml' was not generated."

    # Verify optional HTML report exists in target directory
    html_files = glob.glob("target/deploy-manifest/descriptor.html")
    # HTML report is optional but if configured should exist, we check if any HTML found
    # It is acceptable for it to be absent according to instruction, so no assert failure

    # Read JSON and YAML files content to do basic validation
    import json
    import yaml

    with open(json_files[0], "r", encoding="utf-8") as jf:
        json_content = json.load(jf)
    assert isinstance(json_content, dict), "JSON descriptor content is not a dictionary."

    with open(yaml_files[0], "r", encoding="utf-8") as yf:
        yaml_content = yaml.safe_load(yf)
    assert isinstance(yaml_content, dict), "YAML descriptor content is not a dictionary."

    # Check that key core metadata fields exist in the JSON descriptor
    expected_keys = ["buildInfo", "modules", "dependencies", "licenses", "properties", "plugins"]
    missing_keys = [k for k in expected_keys if k not in json_content]
    assert not missing_keys, f"Missing keys in descriptor.json: {missing_keys}"

    # Check for dry-run summary, signing, compression, archiving features indicators in JSON or YAML
    # These indicators might be flags or section names; check heuristic keys
    features_keys = ["dryRunSummary", "signing", "compression", "archiving"]
    feature_found = any(key in json_content or key in yaml_content for key in features_keys)
    # Because these are optional, we do not assert failure if missing, but log if none found
    # For the test, it's sufficient that buildInfo and modules present and files generated

    # If descriptor.html exists, verify it contains basic HTML structure and key summary tabs
    if html_files:
        with open(html_files[0], "r", encoding="utf-8") as hf:
            html_content = hf.read()
        assert "<html" in html_content.lower(), "descriptor.html missing <html> tag."
        # Check for presence of basic tabs in HTML report
        for tab in ["Modules", "Dependencies", "Licenses", "Properties", "Plugins"]:
            assert tab in html_content, f"descriptor.html missing tab '{tab}'."

test_generate_deployment_manifest_maven_goal()