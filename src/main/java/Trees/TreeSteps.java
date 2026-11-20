package Trees;

public class TreeSteps {
    private int index;
    private String action;
    private Object at;
    private String comment;
    private TreeNodeDto tree;

    public TreeSteps(int index, String action, Object at, String comment, TreeNodeDto tree) {
        this.index = index;
        this.action = action;
        this.at = at;
        this.comment = comment;
        this.tree = tree;
    }

    public int getIndex() { return index; }
    public String getAction() { return action; }
    public Object getAt() { return at; }
    public String getComment() { return comment; }
    public TreeNodeDto getTree() { return tree; }
}
