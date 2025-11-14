import subprocess
import os
import xml.etree.ElementTree as ET

def test_environment_configuration_detection():
    # Run mvn verify quietly with color output forced
    try:
        result = subprocess.run(
            ["mvn", "-q", "-Dstyle.color=always", "verify"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=30,
            check=False,
            text=True
        )
    except subprocess.TimeoutExpired:
        assert False, "Maven verify command timed out"

    # Maven build should succeed
    assert result.returncode == 0, f"Maven build failed with code {result.returncode}. Stderr: {result.stderr}"
    
    # The plugin should generate descriptor files at target directory per PRD
    descriptor_dir = os.path.join(os.getcwd(), "target")
    json_descriptor_path = os.path.join(descriptor_dir, "descriptor.json")
    yaml_descriptor_path = os.path.join(descriptor_dir, "descriptor.yaml")
    html_report_path = os.path.join(descriptor_dir, "descriptor.html")

    # Check at least one descriptor file exists
    descriptors_exist = any(os.path.isfile(p) for p in [json_descriptor_path, yaml_descriptor_path])
    assert descriptors_exist, "No descriptor.json or descriptor.yaml file found in target directory after build"

    # Check descriptor.json or descriptor.yaml content for environment config detection keys
    # We'll pick JSON if available
    import json
    import yaml

    env_config_found = False

    try:
        if os.path.isfile(json_descriptor_path):
            with open(json_descriptor_path, "r", encoding="utf-8") as fjson:
                desc = json.load(fjson)
        elif os.path.isfile(yaml_descriptor_path):
            with open(yaml_descriptor_path, "r", encoding="utf-8") as fyaml:
                desc = yaml.safe_load(fyaml)
        else:
            assert False, "Neither descriptor JSON nor YAML file is present"
    except Exception as ex:
        assert False, f"Failed to load descriptor file: {ex}"

    # The environment config detection from Spring Boot files should appear under environmentConfigurations
    # or similar key, try to find keys related to ports, context paths, actuator endpoints.
    # As exact schema isn't given, test heuristic keys in descriptor recursively

    def search_keys_for_envconfig(obj):
        if isinstance(obj, dict):
            for k, v in obj.items():
                if k.lower() in ["environmentconfig", "environmentconfigurations", "environmentConfig", "environmentConfigurations"]:
                    return True
                if k.lower() in ["springboot", "spring", "env", "environment"]:
                    # heuristic
                    if isinstance(v, dict):
                        # Check for port or actuator keys
                        lower_keys = [key.lower() for key in v.keys()]
                        keywords = ["port", "contextpath", "context-path", "actuator", "endpoints"]
                        if any(any(kw in lk for kw in keywords) for lk in lower_keys):
                            return True
                found = search_keys_for_envconfig(v)
                if found:
                    return True
        elif isinstance(obj, list):
            for item in obj:
                found = search_keys_for_envconfig(item)
                if found:
                    return True
        return False

    env_config_found = search_keys_for_envconfig(desc)

    assert env_config_found, (
        "Environment configuration detection from Spring Boot files not found in descriptor files. "
        "Expected keys for service ports, context paths, or actuator endpoints."
    )

test_environment_configuration_detection()
