import json
import random
import unittest
from pathlib import Path

from jsonschema import Draft202012Validator

from sorting import bubble_sort, insertion_sort, merge_sort, quick_sort
from sorting.__main__ import EXAMPLES, EXAMPLES_DIR, SAMPLE, to_json

ROOT = Path(__file__).resolve().parents[2]
VALIDATOR = Draft202012Validator(json.loads((ROOT / "contract.schema.json").read_text()))
SORTS = [bubble_sort, insertion_sort, quick_sort, merge_sort]

EDGE_CASES = [
    [],
    [1],
    [2, 1],
    [1, 2, 3, 4, 5],
    [5, 4, 3, 2, 1],
    [3, 3, 3],
    [4, 1, 4, 2, 1],
    [-3, 0, -1, 7],
]


class SortingTest(unittest.TestCase):

    def check(self, sort, values):
        response = sort(values)
        steps = response["steps"]
        self.assertEqual(sorted(values), steps[-1]["state"])
        self.assertEqual("done", steps[-1]["action"])
        self.assertEqual(list(values), response["value"])
        self.assertEqual(sort.__name__, response["operation"])
        for i, step in enumerate(steps):
            self.assertEqual(i, step["index"])
            self.assertEqual(len(values), len(step["state"]))
            self.assertTrue(step["comment"].isascii(), step["comment"])
            at = step["at"]
            for index in at if isinstance(at, list) else [] if at is None else [at]:
                self.assertLess(index, len(values))
        VALIDATOR.validate(response)

    def test_edge_cases(self):
        for sort in SORTS:
            for values in EDGE_CASES:
                with self.subTest(sort=sort.__name__, values=values):
                    self.check(sort, values)

    def test_random_arrays(self):
        rng = random.Random(1)
        for sort in SORTS:
            for _ in range(200):
                values = [rng.randint(-20, 20) for _ in range(rng.randint(0, 15))]
                with self.subTest(sort=sort.__name__, values=values):
                    self.check(sort, values)

    def test_input_is_not_changed(self):
        for sort in SORTS:
            values = [3, 1, 2]
            sort(values)
            self.assertEqual([3, 1, 2], values)

    def test_empty_array_has_only_done(self):
        for sort in SORTS:
            self.assertEqual(["done"], [s["action"] for s in sort([])["steps"]])

    def test_sorted_input_has_no_moves_in_simple_sorts(self):
        for sort in (bubble_sort, insertion_sort):
            actions = {s["action"] for s in sort([1, 2, 3, 4])["steps"]}
            self.assertEqual({"compare", "done"}, actions)


class ExamplesTest(unittest.TestCase):

    def test_all_examples_match_the_schema(self):
        files = sorted(EXAMPLES_DIR.glob("*.json"))
        self.assertTrue(files)
        for path in files:
            with self.subTest(file=path.name):
                VALIDATOR.validate(json.loads(path.read_text()))
                self.assertEqual("done", json.loads(path.read_text())["steps"][-1]["action"])

    def test_sorting_examples_are_up_to_date(self):
        for name, sort in EXAMPLES.items():
            with self.subTest(file=name):
                self.assertEqual(to_json(sort(SAMPLE)), (EXAMPLES_DIR / name).read_text(),
                                 f"{name} is outdated, run `python -m sorting`")


if __name__ == "__main__":
    unittest.main()
