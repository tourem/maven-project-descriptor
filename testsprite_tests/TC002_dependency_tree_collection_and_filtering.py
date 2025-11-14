import requests
import json

BASE_URL = "http://localhost:5173"
TIMEOUT = 30
HEADERS = {
    "Accept": "application/json"
}

def test_dependency_tree_collection_and_filtering():
    """
    Verify collection of dependency information (flat, hierarchical trees),
    filtering by scope, detection of duplicate dependencies, and correct export of dependency trees.
    """

    # 1. Get export of dependency tree (summary + flat + hierarchical)
    resp_export = requests.get(f"{BASE_URL}/dependencies/tree", headers=HEADERS, timeout=TIMEOUT)
    assert resp_export.status_code == 200, f"Failed to export dependency tree: {resp_export.text}"
    exported_data = resp_export.json()
    # Basic validation for exported data keys expected for dependency export
    assert "summary" in exported_data, "Exported data missing summary"
    assert "flatTree" in exported_data, "Exported data missing flatTree"
    assert "hierarchicalTree" in exported_data, "Exported data missing hierarchicalTree"
    # Validate summary contains expected keys
    summary = exported_data["summary"]
    assert "totalDependencies" in summary, "Missing totalDependencies in summary"
    assert "scopes" in summary, "Missing scopes in summary"
    assert "duplicates" in summary, "Missing duplicates in summary"
    assert isinstance(summary["duplicates"], list), "duplicates should be a list"

    # 2. Validate flat dependency tree structure
    flat_tree = exported_data["flatTree"]
    assert isinstance(flat_tree, list), "Flat tree should be a list"
    for dep in flat_tree:
        assert all(k in dep for k in ("groupId", "artifactId", "version")), "Missing keys in flat dependency node"

    # 3. Validate hierarchical dependency tree structure
    hierarchical_tree = exported_data["hierarchicalTree"]
    assert isinstance(hierarchical_tree, dict), "Hierarchical tree should be a dict"
    assert "dependency" in hierarchical_tree, "Missing dependency key in hierarchical tree node"
    # Recursive check for children structure
    def check_node(node):
        assert "dependency" in node, "Node missing dependency key"
        if "children" in node:
            assert isinstance(node["children"], list), "Children should be a list"
            for child in node["children"]:
                check_node(child)
    check_node(hierarchical_tree)

    # 4. Test filtering by scope, e.g. filter 'compile' scope only
    params = {"scope": "compile"}
    resp_filtered = requests.get(f"{BASE_URL}/dependencies/tree/flat", headers=HEADERS, params=params, timeout=TIMEOUT)
    assert resp_filtered.status_code == 200, f"Failed to get filtered flat dependency tree: {resp_filtered.text}"
    filtered_deps = resp_filtered.json()
    for dep in filtered_deps:
        dep_scope = dep.get("scope", None)
        assert dep_scope == "compile", f"Dependency scope expected 'compile', got {dep_scope}"

test_dependency_tree_collection_and_filtering()
