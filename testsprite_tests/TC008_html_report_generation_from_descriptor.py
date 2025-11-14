import os
import subprocess
import re
import tempfile

def test_html_report_generation_from_descriptor():
    # Run mvn verify to generate descriptor files including descriptor.html
    # since the instruction states to use mvn -q -Dstyle.color=always verify
    mvn_command = ["mvn", "-q", "-Dstyle.color=always", "verify"]
    try:
        result = subprocess.run(mvn_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=300, text=True)
        # Check mvn execution success
        assert result.returncode == 0, f"Maven verify failed: {result.stderr.strip()}"

        # Descriptor files expected in target directory
        target_dir = os.path.join(os.getcwd(), "target")
        descriptor_html_path = os.path.join(target_dir, "descriptor.html")

        # Check if descriptor.html exists
        assert os.path.isfile(descriptor_html_path), "descriptor.html not found in target directory"

        # Read descriptor.html content
        with open(descriptor_html_path, "r", encoding="utf-8") as f:
            html_content = f.read()

        # Validate presence of navigable tabs text in HTML content
        # Tabs: overview, modules, dependencies, licenses, build info
        tabs = ["overview", "modules", "dependencies", "licenses", "build info"]
        for tab in tabs:
            # Since tabs can be in format <a> or <li> or headings,
            # do a case-insensitive search in html content
            pattern = re.compile(re.escape(tab), re.IGNORECASE)
            assert pattern.search(html_content), f"Tab '{tab}' not found in descriptor.html"

    except subprocess.TimeoutExpired:
        assert False, "Maven verify command timed out"
    except Exception as ex:
        assert False, f"Unexpected error: {ex}"

test_html_report_generation_from_descriptor()