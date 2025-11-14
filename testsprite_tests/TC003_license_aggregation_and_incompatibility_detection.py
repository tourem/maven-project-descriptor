import subprocess
import os
import sys

def test_license_aggregation_and_incompatibility_detection():
    """
    Test the license collection feature of the Maven Deploy Manifest Plugin by running the Maven goal 'generate'.
    This verifies aggregation of licenses, detection of unknown and incompatible licenses,
    and generation of warnings and compliance charts in the produced descriptor files.
    """

    mvn_cmd = ["mvn", "-q", "-Dstyle.color=always", "verify"]

    try:
        # Run Maven verify to execute plugin goals including 'generate'
        result = subprocess.run(mvn_cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=120, text=True)

        output = result.stdout

        # Assert Maven build was successful
        assert result.returncode == 0, f"Maven build failed with return code {result.returncode}\nOutput:\n{output}"

        # Check output contains license aggregation indicators (common patterns or plugin logs)
        assert "license" in output.lower(), "No license-related information found in Maven output."

        # Check for warnings related to unknown or incompatible licenses
        warnings_found = any(keyword in output.lower() for keyword in ["warning", "incompatible license", "unknown license"])
        assert warnings_found, "No warnings about unknown or incompatible licenses found in output."

        # Check for existence of descriptor files with license info
        # Descriptor files are output to target directory by default
        json_path = os.path.join("target", "descriptor.json")
        yaml_path = os.path.join("target", "descriptor.yaml")
        html_path = os.path.join("target", "descriptor.html")

        assert os.path.isfile(json_path), f"Descriptor JSON file not found at {json_path}"
        assert os.path.isfile(yaml_path), f"Descriptor YAML file not found at {yaml_path}"
        # HTML report is optional, but check if exists
        assert os.path.isfile(html_path), f"Descriptor HTML report file not found at {html_path}"

        # Check content of descriptor.json for license aggregation and warnings presence
        with open(json_path, "r", encoding="utf-8") as f_json:
            json_content = f_json.read().lower()
            # License aggregation keywords expected in descriptor
            assert "licenses" in json_content, "No 'licenses' section found in descriptor.json"
            # Check for incompatibility or warnings in descriptor content
            assert ("incompatible" in json_content) or ("warning" in json_content), "No incompatibility warnings found in descriptor.json"

        # Optionally, check HTML report has compliance charts (basic check for legend or chart class)
        with open(html_path, "r", encoding="utf-8") as f_html:
            html_content = f_html.read().lower()
            assert "license" in html_content, "No license information found in HTML report"
            assert ("chart" in html_content) or ("warning" in html_content), "No compliance charts or warning info found in HTML report"

    except subprocess.TimeoutExpired:
        assert False, "Maven verify command timed out"
    except Exception as e:
        assert False, f"Unexpected exception during test: {e}"

test_license_aggregation_and_incompatibility_detection()
