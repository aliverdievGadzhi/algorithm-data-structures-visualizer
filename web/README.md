# Web visualizer

Plays recorded operations step by step. It reads the JSON contract in
[`contract.schema.json`](../contract.schema.json) and needs no build step or dependencies.

## Run

From the repository root:

```bash
python3 -m http.server
```

Open <http://localhost:8000/web/> and pick a scenario. The page does not work when opened from disk (`file://`):
browsers do not let it read the scenarios.

## Controls

| Key | Action |
|-----|--------|
| Left / Right | previous / next step |
| Space | play / pause |
| Home / End | first / last step |

## Tests

Needs Node.js 18 or newer, no packages. From the repository root:

```bash
node --test web/tests/visualizer.test.js
```

## Files

| File | Job |
|------|-----|
| `app.js` | loading, player, step details |
| `contract.js` | checks a file against the contract |
| `tree-view.js` | trees and the tree form of a heap (SVG) |
| `array-view.js` | array cells and bars |
| `tests/visualizer.test.js` | contract, layout and edge checks |

To add a built-in scenario, put the file in `examples/` and add a line to `EXAMPLES` in `app.js`.

The source of truth of the contract is `contract.schema.json`. `contract.js` repeats its rules by hand,
so the page needs no JSON Schema library; change both together. The tests compare their
field lists, structures, colors and name pattern.
