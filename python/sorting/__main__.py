"""Regenerates the sorting examples: run `python -m sorting` from the python/ directory."""

import json
from pathlib import Path

from . import merge_sort, quick_sort

EXAMPLES_DIR = Path(__file__).resolve().parents[2] / "examples"
SAMPLE = [5, 2, 8, 1, 9, 3]
EXAMPLES = {
    "example-quick-sort.json": quick_sort,
    "example-merge-sort.json": merge_sort,
}


def to_json(response):
    return json.dumps(response, indent=2) + "\n"


if __name__ == "__main__":
    for name, sort in EXAMPLES.items():
        (EXAMPLES_DIR / name).write_text(to_json(sort(SAMPLE)))
        print(f"Wrote examples/{name}")
