"""Builds operation responses in the shared JSON contract (see contract.schema.json)."""


def start(operation, values):
    """Returns an empty response for sorting `values` with the given algorithm name."""
    return {"structure": "array", "operation": operation, "value": list(values), "steps": []}


def record(response, action, at, comment, state):
    """Appends a step with a copy of the whole array as it is after the action."""
    response["steps"].append({
        "index": len(response["steps"]),
        "action": action,
        "at": at,
        "comment": comment,
        "state": list(state),
    })
