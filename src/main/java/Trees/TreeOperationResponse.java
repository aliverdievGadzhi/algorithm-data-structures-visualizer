package Trees;

import java.util.List;

public class TreeOperationResponse {
    private String structure;
    private String operation;
    private Object value;
    private List<TreeSteps> steps;

    public TreeOperationResponse(String structure, String operation, Object value, List<TreeSteps> steps) {
        this.structure = structure;
        this.operation = operation;
        this.value = value;
        this.steps = steps;
    }

    public String getStructure() { return structure; }
    public String getOperation() { return operation; }
    public Object getValue() { return value; }
    public List<TreeSteps> getSteps() { return steps; }
}
