"use strict";

// Source of truth: ../contract.schema.json. This is a hand-written copy of its rules, so the page needs
// no JSON Schema library. web/tests/visualizer.test.js fails when the constants below no longer match
// the schema.
//
// Three rules go beyond the schema, which cannot express them: step indexes are 0, 1, 2, ...;
// the last action is "done"; node ids are unique within one state.

const TREE_STRUCTURES = ["bst", "avl", "rb"];
const ARRAY_STRUCTURES = ["heap", "array"];
const ROOT_FIELDS = ["structure", "operation", "value", "steps"];
const STEP_FIELDS = ["index", "action", "at", "comment", "state"];
const NODE_FIELDS = ["id", "value", "left", "right"];
const OPTIONAL_NODE_FIELDS = ["height", "balance", "color"];
const NODE_COLORS = ["RED", "BLACK"];
const NAME_PATTERN = /^[a-z_]+$/;

/** Throws an Error with a readable message if `data` does not follow the contract. */
function validateContract(data) {
  checkObject(data, "file", ROOT_FIELDS);
  const structures = [...TREE_STRUCTURES, ...ARRAY_STRUCTURES];
  check(structures.includes(data.structure),
    `"structure" must be one of ${structures.join(", ")}, got ${JSON.stringify(data.structure)}`);
  checkName(data.operation, "operation");
  check(Array.isArray(data.steps) && data.steps.length > 0, `"steps" must be a non-empty array`);

  data.steps.forEach((step, i) => checkStep(step, i, TREE_STRUCTURES.includes(data.structure)));
  check(data.steps.at(-1).action === "done", `the last step must have the action "done"`);
}

function checkStep(step, i, isTree) {
  const where = `steps[${i}]`;
  checkObject(step, where, STEP_FIELDS);
  check(step.index === i, `${where}.index must be ${i}, got ${JSON.stringify(step.index)}`);
  checkName(step.action, `${where}.action`);
  check(typeof step.comment === "string" && step.comment !== "", `${where}.comment must be a non-empty string`);

  const { at } = step;
  check(at === null || isIndex(at) || (Array.isArray(at) && at.length === 2 && at.every(isIndex)),
    `${where}.at must be null, a non-negative integer, or a pair of them`);

  if (isTree) {
    if (step.state !== null) checkNode(step.state, `${where}.state`, new Set());
  } else {
    check(Array.isArray(step.state), `${where}.state must be an array for this structure`);
  }
}

function checkNode(node, where, ids) {
  checkObject(node, where, NODE_FIELDS, OPTIONAL_NODE_FIELDS);
  check(isIndex(node.id), `${where}.id must be a non-negative integer`);
  check(!ids.has(node.id), `${where}.id ${node.id} is used twice in one state`);
  ids.add(node.id);
  if ("height" in node) check(isIndex(node.height), `${where}.height must be a non-negative integer`);
  if ("balance" in node) check(Number.isInteger(node.balance), `${where}.balance must be an integer`);
  if ("color" in node) check(NODE_COLORS.includes(node.color), `${where}.color must be one of ${NODE_COLORS.join(", ")}`);
  for (const side of ["left", "right"]) {
    if (node[side] !== null) checkNode(node[side], `${where}.${side}`, ids);
  }
}

function checkObject(value, where, required, optional = []) {
  check(value !== null && typeof value === "object" && !Array.isArray(value), `${where} must be an object`);
  for (const key of required) check(Object.hasOwn(value, key), `${where} is missing "${key}"`);
  for (const key of Object.keys(value)) {
    check(required.includes(key) || optional.includes(key), `${where} has an unknown field "${key}"`);
  }
}

function checkName(value, where) {
  check(typeof value === "string" && NAME_PATTERN.test(value), `${where} must contain only a-z and "_"`);
}

function isIndex(value) {
  return Number.isInteger(value) && value >= 0;
}

function check(condition, message) {
  if (!condition) throw new Error(message);
}
