package visualizer.steps;

/**
 * One step of an operation, shared by all structures.
 *
 * @param index   position of the step, starting at 0.
 * @param action  what happened, e.g. {@code compare}, {@code swap}, {@code rotate_left}, {@code done}.
 * @param at      what the action is about, or {@code null}: a node id for trees,
 *                an index or a pair of indexes {@code [i, j]} for arrays.
 *                For {@code delete} it is the removed node, which is no longer in {@code state}.
 * @param comment human-readable explanation.
 * @param state   full snapshot of the structure <b>after</b> the action.
 * @param <S>     snapshot type: {@code TreeNodeDto} for trees, a list for heaps.
 */
public record Step<S>(int index, String action, Object at, String comment, S state) {
}
