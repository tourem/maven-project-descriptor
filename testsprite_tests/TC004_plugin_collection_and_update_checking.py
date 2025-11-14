import requests
import json

BASE_URL = "http://localhost:5173"
TIMEOUT = 30
HEADERS = {
    "Accept": "application/json"
}

def test_plugin_collection_and_update_checking():
    url = f"{BASE_URL}/api/plugins"
    try:
        # Step 1: Collect effective build plugins with executions, phases, and sanitized config
        params = {
            "checkUpdates": "true"  # enable optional checking for newer plugin versions
        }
        response = requests.get(url, headers=HEADERS, params=params, timeout=TIMEOUT)
        response.raise_for_status()
        data = response.json()

        # Validate top-level structure
        assert isinstance(data, dict), "Response should be a JSON object"
        assert "plugins" in data, "Response must contain 'plugins' key"
        plugins = data["plugins"]
        assert isinstance(plugins, list), "'plugins' must be a list"
        assert len(plugins) > 0, "There should be at least one plugin collected"

        # For each plugin, verify expected keys and data types
        for plugin in plugins:
            assert "groupId" in plugin and isinstance(plugin["groupId"], str) and plugin["groupId"], "Plugin must have a non-empty groupId"
            assert "artifactId" in plugin and isinstance(plugin["artifactId"], str) and plugin["artifactId"], "Plugin must have a non-empty artifactId"
            assert "version" in plugin and isinstance(plugin["version"], str) and plugin["version"], "Plugin must have a non-empty version"
            # Executions is expected to be a list with phases and goals
            assert "executions" in plugin and isinstance(plugin["executions"], list), "'executions' must be a list"
            # Configurations should be sanitized, so key should exist
            assert "configuration" in plugin and isinstance(plugin["configuration"], dict), "'configuration' must be a dict"
            # If update checking enabled, 'updateStatus' key reports plugin update info
            assert "updateStatus" in plugin, "'updateStatus' must be present for update checking"
            update_status = plugin["updateStatus"]
            assert update_status in ["up-to-date", "update-available", "unknown"], f"Unexpected updateStatus: {update_status}"

    except requests.RequestException as e:
        assert False, f"HTTP request failed: {e}"
    except json.JSONDecodeError:
        assert False, "Response is not valid JSON"

test_plugin_collection_and_update_checking()