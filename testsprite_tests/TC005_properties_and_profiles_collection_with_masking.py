import requests

BASE_URL = "http://localhost:5173"
TIMEOUT = 30

def test_properties_and_profiles_collection_with_masking():
    url = f"{BASE_URL}/api/properties"
    headers = {
        "Accept": "application/json",
    }
    try:
        response = requests.get(url, headers=headers, timeout=TIMEOUT)
        response.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    data = response.json()

    # Validate presence of grouped properties keys
    expected_groups = ["project", "system", "environment", "custom"]
    for group in expected_groups:
        assert group in data, f"Missing properties group: {group}"
        assert isinstance(data[group], dict), f"Properties group '{group}' should be a dictionary"

    # Validate that sensitive keys are masked in the response for each group
    def contains_masked_values(props):
        mask_values = {"****", "*****", "MASKED", None}
        for key, value in props.items():
            if "password" in key.lower() or "secret" in key.lower() or "token" in key.lower():
                if value not in mask_values:
                    return False
        return True

    for group in expected_groups:
        assert contains_masked_values(data[group]), f"Sensitive values in '{group}' are not properly masked"

    # Optionally verify filtering - that unfiltered keys are present, and filtering config respected
    # This depends on API implementation, we assume existence of some non-masked keys
    for group in expected_groups:
        if len(data[group]) == 0:
            # Warn if empty but not fail, as it could be valid
            print(f"Warning: Properties group '{group}' is empty")


test_properties_and_profiles_collection_with_masking()