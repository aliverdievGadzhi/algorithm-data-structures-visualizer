"use strict";

/**
 * Draws an array as cells with indexes, and as bars when `withBars` is set and all values are numbers.
 * The bar scale covers all `states`, so bars do not rescale while playing.
 */
function createArrayView(host, states, withBars) {
  const values = states.flat();
  const showBars = withBars && values.length > 0 && values.every(Number.isFinite);
  const low = showBars ? values.reduce((a, b) => Math.min(a, b), 0) : 0;
  const high = showBars ? values.reduce((a, b) => Math.max(a, b), 0) : 0;
  const range = high - low || 1;

  const row = addDiv(host, "array");
  const columns = [];

  function render(stepIndex, { active, kind, swapped }) {
    const state = states[stepIndex];
    row.dataset.kind = kind;

    while (columns.length > state.length) columns.pop().remove();
    while (columns.length < state.length) columns.push(createColumn(row, columns.length, showBars));

    state.forEach((value, i) => {
      const column = columns[i];
      const label = formatValue(value);
      column.classList.toggle("active", active.has(i));
      column.title = `index ${i}, value ${label}`;
      column.querySelector(".cell").textContent = label;
      if (!showBars) return;
      const bar = column.querySelector(".bar");
      bar.style.bottom = `${(Math.min(value, 0) - low) / range * 100}%`;
      bar.style.height = `${Math.abs(value) / range * 100}%`;
    });

    const focus = columns[[...active][0]];
    if (withBars && focus) host.scrollTo({ left: focus.offsetLeft - row.offsetLeft - host.clientWidth / 2 });

    if (swapped && !reducedMotion()) {
      const [a, b] = swapped.map(i => columns[i]);
      if (!a || !b) return;
      const distance = b.offsetLeft - a.offsetLeft;
      for (const [column, dx] of [[a, distance], [b, -distance]]) {
        column.animate([{ transform: `translateX(${dx}px)` }, { transform: "none" }], { duration: MOVE_MS, easing: "ease" });
      }
    }
  }

  return { render };
}

function createColumn(row, index, showBars) {
  const column = addDiv(row, "column");
  if (showBars) addDiv(addDiv(column, "plot"), "bar");
  addDiv(column, "cell");
  addDiv(column, "slot").textContent = index;
  return column;
}

function addDiv(parent, className) {
  const element = document.createElement("div");
  element.className = className;
  return parent.appendChild(element);
}
