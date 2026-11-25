"use strict";

const NODE_RADIUS = 20;
const NODE_GAP = 56;
const LEVEL_GAP = 76;
const TREE_MARGIN = 34;
const MOVE_MS = 300;

/** Turns a heap array into the same node shape as the trees; the id of a node is its index. */
function heapToTree(items, i = 0) {
  if (i >= items.length) return null;
  return { id: i, value: items[i], slot: i, left: heapToTree(items, 2 * i + 1), right: heapToTree(items, 2 * i + 2) };
}

/** Lists all nodes in order with their column and depth. */
function layoutTree(root) {
  const nodes = [];
  const visit = (node, depth, parentId) => {
    if (node === null) return;
    visit(node.left, depth + 1, node.id);
    nodes.push({ node, parentId, depth, column: nodes.length });
    visit(node.right, depth + 1, node.id);
  };
  visit(root, 0, null);
  return nodes;
}

/**
 * Pairs the edges of the next state with the drawn ones, so an edge moves instead of jumping.
 * Both lists hold {parent, child} ids. Returns a Map from next edge to drawn edge; the rest are new.
 * An edge keeps its line if it joins the same two nodes (a rotation only flips it), else if it leads
 * to the same child, else if it starts at the same parent.
 */
function matchEdges(drawn, next) {
  const rules = [
    (a, b) => (a.parent === b.parent && a.child === b.child) || (a.parent === b.child && a.child === b.parent),
    (a, b) => a.child === b.child,
    (a, b) => a.parent === b.parent,
  ];
  const free = new Set(drawn);
  const matches = new Map();
  for (const rule of rules) {
    for (const edge of next) {
      if (matches.has(edge)) continue;
      const match = [...free].find(candidate => rule(candidate, edge));
      if (!match) continue;
      matches.set(edge, match);
      free.delete(match);
    }
  }
  return matches;
}

/**
 * Draws binary trees. SVG elements are kept between steps and moved together, frame by frame, with plain
 * attributes: CSS transitions of SVG geometry do not work in every browser.
 * The canvas fits the biggest of all `roots`, so it does not jump while playing.
 */
function createTreeView(host, roots) {
  const layouts = roots.map(layoutTree);
  const columns = Math.max(1, ...layouts.map(nodes => nodes.length));
  const levels = Math.max(1, ...layouts.flatMap(nodes => nodes.map(n => n.depth + 1)));
  const width = (columns - 1) * NODE_GAP + 2 * TREE_MARGIN;
  const height = (levels - 1) * LEVEL_GAP + 2 * TREE_MARGIN + 10;

  const svg = addSvg(host, "svg", { class: "tree", width, height, role: "img", "aria-label": "Binary tree" });
  const edgeLayer = addSvg(svg, "g");
  const nodeLayer = addSvg(svg, "g");
  const emptyText = addSvg(svg, "text", { class: "empty", x: width / 2, y: height / 2 });
  const shapes = new Map(); // node id -> {group, point}
  let edges = []; // {parent, child, line, ends}, where ends maps a node id to the point of that end
  let frame = null;

  function render(stepIndex, { active, kind, swapped }) {
    const nodes = layouts[stepIndex];
    svg.dataset.kind = kind;
    emptyText.textContent = nodes.length === 0 ? "empty" : "";

    const position = new Map(nodes.map(({ node, depth, column }) => [node.id, {
      x: width / 2 + (column - (nodes.length - 1) / 2) * NODE_GAP,
      y: TREE_MARGIN + depth * LEVEL_GAP,
    }]));
    const moves = [];
    const draws = [];

    for (const { node } of nodes) {
      if (!shapes.has(node.id)) {
        shapes.set(node.id, { group: createNode(nodeLayer), point: { ...position.get(node.id) } });
      }
      updateNode(shapes.get(node.id).group, node);
    }
    if (swapped && swapped.every(id => shapes.has(id))) {
      const [a, b] = swapped.map(id => shapes.get(id));
      [a.point, b.point] = [b.point, a.point];
    }
    for (const [id, { group, point }] of shapes) {
      group.classList.toggle("active", active.has(id));
      if (position.has(id)) {
        moves.push([point, position.get(id)]);
      } else {
        // A removed node keeps its highlight while it fades out.
        shapes.delete(id);
        fadeOut(group);
      }
      draws.push(() => group.setAttribute("transform", `translate(${point.x} ${point.y})`));
    }

    const next = nodes.filter(n => n.parentId !== null).map(n => ({ parent: n.parentId, child: n.node.id }));
    const matches = matchEdges(edges, next);
    const kept = new Set(matches.values());
    for (const { line } of edges.filter(edge => !kept.has(edge))) fadeOut(line);
    edges = next.map(edge => ({ ...edge, ...reuseEdge(edge, matches.get(edge), position) }));
    for (const { parent, child, line, ends } of edges) {
      const [from, to] = [ends.get(parent), ends.get(child)];
      moves.push([from, position.get(parent)], [to, position.get(child)]);
      draws.push(() => {
        for (const [name, value] of [["x1", from.x], ["y1", from.y], ["x2", to.x], ["y2", to.y]]) {
          line.setAttribute(name, value);
        }
      });
    }

    cancelAnimationFrame(frame);
    animate(moves, () => draws.forEach(draw => draw()));
    scrollToFocus(position, nodes, active);
  }

  function reuseEdge({ parent, child }, drawn, position) {
    if (!drawn) {
      const line = addSvg(edgeLayer, "line", { class: "edge" });
      return { line, ends: new Map([parent, child].map(id => [id, { ...(shapes.get(id).point) }])) };
    }
    // The end at a shared node stays there, the other end travels from its old node to the new one.
    const { line, ends } = drawn;
    const free = [...ends.keys()].filter(id => id !== parent && id !== child);
    return { line, ends: new Map([parent, child].map(id => [id, ends.get(id) ?? ends.get(free.shift())])) };
  }

  /** Moves every [point, target] pair together; `draw` runs once per frame. */
  function animate(moves, draw) {
    const starts = moves.map(([point]) => ({ ...point }));
    const begin = performance.now();
    const moveTo = progress => {
      moves.forEach(([point, target], i) => {
        point.x = starts[i].x + (target.x - starts[i].x) * progress;
        point.y = starts[i].y + (target.y - starts[i].y) * progress;
      });
      draw();
    };
    // Frames do not run in a hidden tab, so the final state is drawn at once.
    if (reducedMotion() || document.hidden) return moveTo(1);
    const tick = now => {
      const t = Math.min(1, Math.max(0, (now - begin) / MOVE_MS));
      moveTo(t * t * (3 - 2 * t));
      if (t < 1) frame = requestAnimationFrame(tick);
    };
    moveTo(0);
    frame = requestAnimationFrame(tick);
  }

  /** A wide tree scrolls inside the stage: keep the active node (or the root) in sight. */
  function scrollToFocus(position, nodes, active) {
    const focus = position.get([...active].find(id => position.has(id)))
      ?? position.get(nodes.find(n => n.parentId === null)?.node.id);
    if (focus) host.scrollTo({ left: focus.x - host.clientWidth / 2 });
  }

  return { render };
}

function fadeOut(element) {
  element.classList.add("leaving");
  setTimeout(() => element.remove(), MOVE_MS);
}

function createNode(layer) {
  const group = addSvg(layer, "g", { class: "node" });
  addSvg(group, "title");
  addSvg(group, "circle", { class: "ring", r: NODE_RADIUS + 5 });
  addSvg(group, "circle", { class: "body", r: NODE_RADIUS });
  addSvg(group, "text", { class: "value" });
  addSvg(group, "text", { class: "meta", y: NODE_RADIUS + 15 });
  return group;
}

function updateNode(group, node) {
  if (node.color) group.dataset.color = node.color;

  const label = formatValue(node.value);
  group.querySelector(".value").textContent = label.length > 4 ? label.slice(0, 3) + "\u2026" : label;

  const meta = group.querySelector(".meta");
  if (node.slot !== undefined) meta.textContent = `[${node.slot}]`;
  else if (node.balance !== undefined) meta.textContent = `h${node.height ?? "?"} b${node.balance}`;
  meta.classList.toggle("bad", Math.abs(node.balance ?? 0) > 1);

  group.querySelector("title").textContent = [
    `value ${label}`,
    node.slot === undefined ? `id ${node.id}` : `index ${node.slot}`,
    node.height === undefined ? "" : `height ${node.height}`,
    node.balance === undefined ? "" : `balance ${node.balance}`,
    node.color ?? "",
  ].filter(Boolean).join(", ");
}

function addSvg(parent, tag, attributes = {}) {
  const element = document.createElementNS("http://www.w3.org/2000/svg", tag);
  for (const [name, value] of Object.entries(attributes)) element.setAttribute(name, value);
  return parent.appendChild(element);
}
