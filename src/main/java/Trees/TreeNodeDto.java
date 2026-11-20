package Trees;

public class TreeNodeDto {
    private final Object value;
    private final int height;
    private final int balance;
    private final TreeNodeDto left;
    private final TreeNodeDto right;

    public TreeNodeDto(Object value, int height, int balance, TreeNodeDto left, TreeNodeDto right) {
        this.value = value;
        this.height = height;
        this.balance = balance;
        this.left = left;
        this.right = right;
    }

    public Object getValue() { return value; }
    public int getHeight() { return height; }
    public int getBalance() { return balance; }
    public TreeNodeDto getLeft() { return left; }
    public TreeNodeDto getRight() { return right; }
    }

