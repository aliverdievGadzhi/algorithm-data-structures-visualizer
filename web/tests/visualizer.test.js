"use strict";

// Run from the repository root: node --test web/tests/visualizer.test.js

const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

const ROOT = path.join(__dirname, "..", "..");
const read = file => fs.readFileSync(path.join(ROOT, file), "utf8");
const readJson = file => JSON.parse(read(file));

// The page uses classic scripts, so they are loaded into the global scope here as well.
for (const file of ["contract.js", "tree-view.js"]) vm.runInThisContext(read(`web/${file}`), file);
const constant = name => vm.runInThisContext(name);

const schema = readJson("contract.schema.json");
const exampleFiles = fs.readdirSync(path.join(ROOT, "examples")).filter(file => file.endsWith(".json")).sort();

// ---------- contract ----------

test("contract.js constants match contract.schema.json", () => {
  const { step, treeNode } = schema.$defs;
  const sorted = list => [...list].sort();
  const structures = [...constant("TREE_STRUCTURES"), ...constant("ARRAY_STRUCTURES")];
  const nodeFields = [...constant("NODE_FIELDS"), ...constant("OPTIONAL_NODE_FIELDS")];

  assert.deepEqual(sorted(structures), sorted(schema.properties.structure.enum));
  assert.deepEqual(sorted(constant("ARRAY_STRUCTURES")), sorted(schema.if.properties.structure.enum));

  assert.deepEqual(sorted(constant("ROOT_FIELDS")), sorted(schema.required));
  assert.deepEqual(sorted(constant("ROOT_FIELDS")), sorted(Object.keys(schema.properties)));
  assert.deepEqual(sorted(constant("STEP_FIELDS")), sorted(step.required));
  assert.deepEqual(sorted(constant("STEP_FIELDS")), sorted(Object.keys(step.properties)));
  assert.deepEqual(sorted(constant("NODE_FIELDS")), sorted(treeNode.required));
  assert.deepEqual(sorted(nodeFields), sorted(Object.keys(treeNode.properties)));

  assert.deepEqual(sorted(constant("NODE_COLORS")), sorted(treeNode.properties.color.enum));
  assert.equal(constant("NAME_PATTERN").source, schema.properties.operation.pattern);
  assert.equal(constant("NAME_PATTERN").source, step.properties.action.pattern);
});

test("every example follows the contract", () => {
  assert.ok(exampleFiles.length > 0);
  for (const file of exampleFiles) assert.doesNotThrow(() => validateContract(readJson(`examples/${file}`)), file);
});

test("every structure has an example", () => {
  const structures = exampleFiles.map(file => readJson(`examples/${file}`).structure);
  assert.deepEqual([...new Set(structures)].sort(), [...schema.properties.structure.enum].sort());
});

test("the scenario list of the page is exactly the examples directory", () => {
  const listed = [...read("web/app.js").matchAll(/\["(example-[^"]+\.json)"/g)].map(match => match[1]);
  assert.deepEqual(listed.sort(), exampleFiles);
});

test("files that break the contract are rejected with the place of the error", () => {
  const cases = [
    [/"structure" must be/, d => { d.structure = "tree"; }],
    [/missing "value"/, d => { delete d.value; }],
    [/unknown field "extra"/, d => { d.extra = 1; }],
    [/operation must contain/, d => { d.operation = "Insert!"; }],
    [/non-empty array/, d => { d.steps = []; }],
    [/last step must have/, d => { d.steps.pop(); }],
    [/steps\[0\]\.index/, d => { d.steps[0].index = 5; }],
    [/steps\[0\]\.action/, d => { d.steps[0].action = ""; }],
    [/steps\[0\]\.comment/, d => { d.steps[0].comment = ""; }],
    [/steps\[0\]\.at/, d => { d.steps[0].at = -1; }],
    [/steps\[0\]\.at/, d => { d.steps[0].at = [1, 2, 3]; }],
    [/steps\[0\]\.at/, d => { d.steps[0].at = "1"; }],
    [/steps\[0\]\.state must be an object/, d => { d.steps[0].state = [1]; }],
    [/state\.left\.id .* used twice/, d => { d.steps[0].state.left.id = d.steps[0].state.id; }],
    [/state\.color must be/, d => { d.steps[0].state.color = "blue"; }],
    [/state\.balance must be/, d => { d.steps[0].state.balance = 0.5; }],
    [/state\.height must be/, d => { d.steps[0].state.height = -1; }],
    [/state\.left must be an object/, d => { d.steps[0].state.left = 7; }],
    [/state must be an array/, d => { d.structure = "heap"; }],
  ];
  for (const [message, breakIt] of cases) {
    const data = readJson("examples/example-rb-delete.json");
    breakIt(data);
    assert.throws(() => validateContract(data), message, breakIt.toString());
  }
  for (const data of [null, [], 1, "text"]) assert.throws(() => validateContract(data), /must be an object/);
});

test("empty tree, empty heap and empty array are valid", () => {
  for (const [structure, state] of [["bst", null], ["heap", []], ["array", []]]) {
    const step = { index: 0, action: "done", at: null, comment: "Done", state };
    assert.doesNotThrow(() => validateContract({ structure, operation: "insert", value: null, steps: [step] }));
  }
});

// ---------- layout ----------

test("heapToTree links index i to 2i + 1 and 2i + 2", () => {
  assert.equal(heapToTree([]), null);
  const root = heapToTree([1, 2, 3, 4]);
  assert.deepEqual([root.value, root.left.value, root.right.value, root.left.left.value], [1, 2, 3, 4]);
  assert.deepEqual([root.left.id, root.right.id, root.left.left.id], [1, 2, 3]);
  assert.equal(root.left.right, null);
});

test("layoutTree puts nodes in order, one per column, children one level down", () => {
  assert.deepEqual(layoutTree(null), []);
  const nodes = layoutTree(heapToTree(["a", "b", "c", "d"]));
  assert.deepEqual(nodes.map(n => n.node.value), ["d", "b", "a", "c"]);
  assert.deepEqual(nodes.map(n => n.column), [0, 1, 2, 3]);
  assert.deepEqual(nodes.map(n => n.depth), [2, 1, 0, 1]);
  assert.deepEqual(nodes.map(n => n.parentId), [1, 0, null, 0]);
});

// ---------- edges ----------

const edge = (parent, child) => ({ parent, child });
const matched = (drawn, next) => {
  const matches = matchEdges(drawn, next);
  return next.map(e => matches.get(e));
};

test("an unchanged tree keeps every edge", () => {
  const drawn = [edge(1, 2), edge(1, 3)];
  assert.deepEqual(matched(drawn, [edge(1, 3), edge(1, 2)]), [drawn[1], drawn[0]]);
});

test("a rotation reuses all edges: one flips, one changes parent, one changes child", () => {
  // Rotate right around 2 (under 9): 1 moves up and its right child 5 moves to 2.
  const drawn = [edge(9, 2), edge(2, 1), edge(1, 5)];
  assert.deepEqual(matched(drawn, [edge(9, 1), edge(1, 2), edge(2, 5)]), drawn);
});

test("an inserted node gets a new edge and a removed node frees its edge", () => {
  const drawn = [edge(1, 2)];
  assert.deepEqual(matched(drawn, [edge(1, 2), edge(2, 3)]), [drawn[0], undefined]);
  assert.deepEqual(matched([edge(1, 2), edge(2, 3)], [edge(1, 2)]), drawn);
});

test("no drawn edge is used twice in any example", () => {
  const edgesOf = state => layoutTree(state).filter(n => n.parentId !== null).map(n => edge(n.parentId, n.node.id));
  for (const file of exampleFiles) {
    const { structure, steps } = readJson(`examples/${file}`);
    if (!constant("TREE_STRUCTURES").includes(structure)) continue;
    for (let i = 1; i < steps.length; i++) {
      const [drawn, next] = [edgesOf(steps[i - 1].state), edgesOf(steps[i].state)];
      const used = [...matchEdges(drawn, next).values()];
      assert.equal(new Set(used).size, used.length, `${file} step ${i}`);
      assert.equal(used.length, Math.min(drawn.length, next.length), `${file} step ${i}: an edge would jump`);
    }
  }
});
