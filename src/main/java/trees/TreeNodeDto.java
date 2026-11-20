package trees;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Snapshot of one tree node for the visualizer.
 * Optional fields are left out of the JSON when a tree does not use them:
 * {@code height} and {@code balance} for AVL, {@code color} for red-black.
 */
public record TreeNodeDto(
        int id,
        Object value,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer height,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer balance,
        @JsonInclude(JsonInclude.Include.NON_NULL) String color,
        TreeNodeDto left,
        TreeNodeDto right
) {
}
