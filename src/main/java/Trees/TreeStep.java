package trees;

/**
 * One step of an operation.
 *
 * @param index   position of the step, starting at 0.
 * @param action  what happened, e.g. {@code compare}, {@code insert}, {@code rotate_left}, {@code done}.
 * @param at      id of the node the action is about, or {@code null}.
 * @param comment human-readable explanation.
 * @param state   full snapshot of the structure <b>after</b> the action ({@code null} for an empty tree).
 */
public record TreeStep(int index, String action, Integer at, String comment, TreeNodeDto state) {
}
