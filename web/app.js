"use strict";

const EXAMPLES = [
  ["example-insert.json", "AVL tree: insert 65"],
  ["example-delete.json", "AVL tree: delete 50"],
  ["example-bst-insert.json", "BST: insert 45"],
  ["example-bst-delete.json", "BST: delete 50"],
  ["example-rb-insert.json", "Red-black tree: insert 60"],
  ["example-rb-delete.json", "Red-black tree: delete 50"],
  ["example-heap-insert.json", "Min-heap: insert 3"],
  ["example-heap-extract.json", "Min-heap: extract min"],
  ["example-quick-sort.json", "Quick sort"],
  ["example-merge-sort.json", "Merge sort"],
];

// Every action gets one color group. Unknown actions are drawn as "inspect".
const KINDS = {
  move: ["swap", "rotate_left", "rotate_right", "recolor", "replace"],
  write: ["insert", "overwrite", "extract"],
  alert: ["delete", "unbalanced", "double_black", "duplicate", "not_found", "empty"],
  done: ["done"],
};

const $ = id => document.getElementById(id);
const PLAYER_IDS = ["progress", "first", "prev", "play", "next", "last"];

let data = null;
let views = [];
let current = 0;
let timer = null;
let loadId = 0;

function formatValue(value) {
  return typeof value === "string" ? value : JSON.stringify(value);
}

function reducedMotion() {
  return matchMedia("(prefers-reduced-motion: reduce)").matches;
}

// ---------- loading ----------

async function openExample(file) {
  const id = ++loadId;
  let example;
  try {
    const response = await fetch(`../examples/${file}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    example = await response.json();
    validateContract(example);
  } catch (error) {
    if (id !== loadId) return;
    showMessage(`Cannot load ${file}: ${error.message}`);
    // Back to the scenario on screen, so the failed one can be picked again.
    $("scenario").value = data ? $("source").textContent : "";
    return;
  }
  if (id === loadId) showScenario(example, file);
}

function showScenario(example, file) {
  pause();
  showMessage("");
  data = example;
  $("stage").replaceChildren();
  views = createViews(data, $("stage"));
  $("structure").textContent = data.structure;
  $("operation").textContent = data.operation;
  $("value").textContent = formatValue(data.value);
  $("source").textContent = file;
  $("progress").max = data.steps.length - 1;
  show(0);
}

function createViews({ structure, steps }, stage) {
  const states = steps.map(step => step.state);
  if (structure === "array") return [createArrayView(stage, states, true)];
  if (structure === "heap") {
    return [
      createTreeView(stage, states.map(items => heapToTree(items))),
      createArrayView(stage, states, false),
    ];
  }
  return [createTreeView(stage, states)];
}

function showMessage(text) {
  $("message").textContent = text;
  $("message").hidden = text === "";
}

// ---------- current step ----------

function show(index) {
  const step = data.steps[index];
  const kind = kindOf(step.action);
  const active = activeItems(step);
  // The slide plays only for a single step forward; jumps and steps back just show the state.
  const swapped = step.action === "swap" && index === current + 1 && Array.isArray(step.at) ? step.at : null;
  current = index;
  for (const view of views) view.render(index, { active, kind, swapped });

  $("counter").textContent = `Step ${index + 1} / ${data.steps.length}`;
  $("action").textContent = step.action;
  $("action").dataset.kind = kind;
  $("comment").textContent = step.comment;
  $("at").textContent = describeAt(index);
  $("progress").value = index;
  $("progress").disabled = false;
  $("play").disabled = false;
  $("first").disabled = $("prev").disabled = index === 0;
  $("next").disabled = $("last").disabled = index === data.steps.length - 1;
}

function kindOf(action) {
  return Object.keys(KINDS).find(kind => KINDS[kind].includes(action)) ?? "inspect";
}

/** Ids or indexes to highlight. The pair of a `merge` step is a range; `done` marks a whole sorted array. */
function activeItems({ action, at, state }) {
  if (action === "done" && data.structure === "array") return new Set(state.keys());
  if (at === null) return new Set();
  if (!Array.isArray(at)) return new Set([at]);
  if (action !== "merge") return new Set(at);
  return new Set([...state.keys()].filter(i => i >= at[0] && i <= at[1]));
}

function describeAt(index) {
  const { action, at, state } = data.steps[index];
  if (at === null) return "-";
  if (Array.isArray(at)) {
    return action === "merge" ? `indexes ${at[0]}..${at[1]}` : `indexes ${at[0]}, ${at[1]}`;
  }
  if (!TREE_STRUCTURES.includes(data.structure)) return `index ${at}`;

  const node = findNode(state, at);
  if (node) return `node #${at} (${formatValue(node.value)})`;
  // `delete` points at a node that is already gone, so its value comes from the step before.
  const removed = index > 0 ? findNode(data.steps[index - 1].state, at) : null;
  return removed ? `node #${at} (${formatValue(removed.value)}, removed)` : `node #${at} (removed)`;
}

function findNode(node, id) {
  if (node === null || node.id === id) return node;
  return findNode(node.left, id) ?? findNode(node.right, id);
}

// ---------- player ----------

function goTo(index) {
  if (data) show(Math.max(0, Math.min(index, data.steps.length - 1)));
}

function play() {
  if (!data) return;
  if (current === data.steps.length - 1) show(0);
  $("play").textContent = "Pause";
  schedule();
}

function schedule() {
  timer = setTimeout(() => {
    goTo(current + 1);
    if (current === data.steps.length - 1) pause();
    else schedule();
  }, 1000 / $("speed").value);
}

function pause() {
  clearTimeout(timer);
  timer = null;
  $("play").textContent = "Play";
}

function togglePlay() {
  if (timer === null) play();
  else pause();
}

function jump(index) {
  pause();
  goTo(index);
}

$("first").addEventListener("click", () => jump(0));
$("prev").addEventListener("click", () => jump(current - 1));
$("next").addEventListener("click", () => jump(current + 1));
$("last").addEventListener("click", () => jump(Infinity));
$("play").addEventListener("click", togglePlay);
$("progress").addEventListener("input", event => jump(Number(event.target.value)));
$("speed").addEventListener("input", event => { $("speed-value").textContent = `${event.target.value}x`; });

document.addEventListener("keydown", event => {
  if (event.ctrlKey || event.metaKey || event.altKey || event.target.closest("input, select")) return;
  if (event.key === " " && event.target.closest("button")) return;
  const action = {
    ArrowLeft: () => jump(current - 1),
    ArrowRight: () => jump(current + 1),
    Home: () => jump(0),
    End: () => jump(Infinity),
    " ": togglePlay,
  }[event.key];
  if (!action) return;
  event.preventDefault();
  action();
});

// ---------- scenarios ----------

$("scenario").append(...EXAMPLES.map(([file, label]) => new Option(label, file)));
$("scenario").addEventListener("change", event => openExample(event.target.value));

if (location.protocol === "file:") {
  $("scenario").disabled = true;
  showMessage("Browsers do not let a page opened from disk load the scenarios. "
    + "Run python3 -m http.server in the repository root and open http://localhost:8000/web/.");
} else {
  $("scenario").value = EXAMPLES[0][0]; // some browsers restore the old choice on reload
  openExample(EXAMPLES[0][0]);
}
