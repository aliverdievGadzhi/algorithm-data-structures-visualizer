package Trees;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AVLTree<T extends Comparable<T>> extends AbstractBinarySearchTree<T, AVLNode<T>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final List<TreeSteps> steps = new ArrayList<>();
    private T currentOperationValue;
    private String currentOperation = "insert";

    // Вспомогательный метод добавления шага с авто-инкрементом индекса
    private void recordStep(String action, Object at, String comment) {
        TreeNodeDto currentSnapshot = buildDto(this.root);
        steps.add(new TreeSteps(steps.size(), action, at, comment, currentSnapshot));
    }

    @Override
    public void insert(T value) {
        if (value == null) return;
        this.currentOperation = "insert";
        this.currentOperationValue = value;
        boolean wasEmpty = (this.root == null);

        this.root = insertNode(this.root, value);

        // Если дерево было пустым, фиксируем шаг вставки для корня
        if (wasEmpty) {
            recordStep("insert", value, "Создан корень дерева со значением " + value);
        }

        // Окончание операции
        recordStep("done", value, "Вставка " + value + " успешно завершена");
    }

    private AVLNode<T> insertNode(AVLNode<T> node, T key) {
        if (node == null) {
            AVLNode<T> newNode = createNode(key);
            newNode.setHeight(0);
            return newNode;
        }

        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            recordStep("compare", node.getKey(), key + " меньше " + node.getKey() + " — идём влево");
            boolean isNew = (node.getLeft() == null);
            AVLNode<T> leftChild = insertNode(node.getLeft(), key);
            node.setLeft(leftChild);
            leftChild.setParent(node);

            if (isNew) {
                recordStep("insert", key, "Вставлен узел " + key + " слева от " + node.getKey());
            }
        } else if (cmp > 0) {
            recordStep("compare", node.getKey(), key + " больше " + node.getKey() + " — идём вправо");
            boolean isNew = (node.getRight() == null);
            AVLNode<T> rightChild = insertNode(node.getRight(), key);
            node.setRight(rightChild);
            rightChild.setParent(node);

            if (isNew) {
                recordStep("insert", key, "Вставлен узел " + key + " справа от " + node.getKey());
            }
        } else {
            return node;
        }

        return balance(node);
    }

    private AVLNode<T> balance(AVLNode<T> node) {
        updateHeight(node);
        int balanceFactor = getBalanceFactor(node);

        // Left Left Case -> Малый правый поворот
        if (balanceFactor > 1 && getBalanceFactor(node.getLeft()) >= 0) {
            return rotateRight(node);
        }

        // Left Right Case -> Большой лево-правый поворот
        if (balanceFactor > 1 && getBalanceFactor(node.getLeft()) < 0) {
            node.setLeft(rotateLeft(node.getLeft()));
            return rotateRight(node);
        }

        // Right Right Case -> Малый левый поворот
        if (balanceFactor < -1 && getBalanceFactor(node.getRight()) <= 0) {
            return rotateLeft(node);
        }

        // Right Left Case -> Большой право-левый поворот
        if (balanceFactor < -1 && getBalanceFactor(node.getRight()) > 0) {
            node.setRight(rotateRight(node.getRight()));
            return rotateLeft(node);
        }

        return node;
    }

    private AVLNode<T> rotateRight(AVLNode<T> y) {
        int oldBalance = getBalanceFactor(y);

        AVLNode<T> x = y.getLeft();
        AVLNode<T> t2 = x.getRight();
        AVLNode<T> parent = y.getParent();

        x.setRight(y);
        y.setLeft(t2);

        if (t2 != null) t2.setParent(y);
        x.setParent(parent);
        y.setParent(x);

        if (parent != null) {
            if (parent.getLeft() == y) parent.setLeft(x);
            else parent.setRight(x);
        } else {
            this.root = x;
        }

        updateHeight(y);
        updateHeight(x);

        // 2. Логируем зафиксированный старый баланс (+2)
        recordStep("rotate_right", y.getKey(), "Баланс " + oldBalance + ", правый поворот вокруг " + y.getKey());
        return x;
    }

    private AVLNode<T> rotateLeft(AVLNode<T> x) {
        int oldBalance = getBalanceFactor(x);

        AVLNode<T> y = x.getRight();
        AVLNode<T> t2 = y.getLeft();
        AVLNode<T> parent = x.getParent();

        y.setLeft(x);
        x.setRight(t2);

        if (t2 != null) t2.setParent(x);
        y.setParent(parent);
        x.setParent(y);

        if (parent != null) {
            if (parent.getLeft() == x) parent.setLeft(y);
            else parent.setRight(y);
        } else {
            this.root = y;
        }

        updateHeight(x);
        updateHeight(y);

        // 2. Логируем зафиксированный старый баланс (-2)
        recordStep("rotate_left", x.getKey(), "Баланс " + oldBalance + ", левый поворот вокруг " + x.getKey());
        return y;
    }

    private int height(AVLNode<T> node) {
        return node == null ? -1 : node.getHeight();
    }

    private void updateHeight(AVLNode<T> node) {
        if (node != null) {
            node.setHeight(1 + Math.max(height(node.getLeft()), height(node.getRight())));
        }
    }

    private int getBalanceFactor(AVLNode<T> node) {
        return node == null ? 0 : height(node.getLeft()) - height(node.getRight());
    }

    public void remove(T value) {
        if (value == null || this.root == null) return;
        this.currentOperation = "delete";
        this.currentOperationValue = value;

        this.steps.clear();

        this.root = deleteNode( this.root, value);

        recordStep("done", value, "Удаление " + value + " завершено");
    }
    public AVLNode<T> deleteNode(AVLNode<T> node, T key){
           if(node==null){
               return null;
           }
           int cmp=key.compareTo(node.getKey());

           if(cmp<0){
               recordStep("compare", node.getKey(), key + " меньше " + node.getKey() + " — идём влево");
               AVLNode<T> leftChild = deleteNode(node.getLeft(), key);
               node.setLeft(leftChild);
               if (leftChild != null) leftChild.setParent(node);
           } else if (cmp>0) {
               recordStep("compare", node.getKey(), key + " больше " + node.getKey() + " — идём вправо");
               AVLNode<T> rightChild = deleteNode(node.getRight(), key);
               node.setRight(rightChild);
               if (rightChild != null) rightChild.setParent(node);
           }
           else {
               if(!node.hasLeft()||!node.hasRight()){
                   AVLNode<T> tmp=(node.hasLeft())?node.getLeft():node.getRight();
                   if(tmp==null){
                       recordStep("delete", key, "Удалён листовой узел " + key);
                       node = null;
                   }
                   else {
                       recordStep("delete", key, "Узел " + key + " заменён своим единственным потомком " + tmp.getKey());
                       tmp.setParent(node.getParent());
                       node = tmp;
                   }
               }
               else {

                   AVLNode<T> minNode = getMinValueNode(node.getRight());
                   recordStep(
                           "find_replacement",
                           minNode.getKey(),
                           "Узел " + node.getKey() + " имеет 2 детей. Берём замену из правого поддерева — минимальный узел " + minNode.getKey()
                   );

                   T replacedKey = node.getKey();
                   node.setKey(minNode.getKey());

                   recordStep(
                           "replace",
                           node.getKey(),
                           "Значение узла " + replacedKey + " заменено на " + minNode.getKey() + ". Теперь удаляем дубликат " + minNode.getKey() + " из правого поддерева"
                   );

                   AVLNode<T> rightChild = deleteNode(node.getRight(), minNode.getKey());
                   node.setRight(rightChild);
                   if (rightChild != null) rightChild.setParent(node);
               }
           }
        if (node == null) {
            return null;
        }
        return balance(node);
    }
    private AVLNode<T> getMinValueNode(AVLNode<T> node) {
        AVLNode<T> current = node;
        while (current.getLeft() != null) {
            current =  current.getLeft();
        }
        return current;
    }



    // ==========================================
    // СЕРИАЛИЗАЦИЯ В JSON
    // ==========================================

    @Override
    public String toJsonString() {
        if (this.root == null) {
            return "null";
        }
        try {
            TreeNodeDto dto = buildDto(this.root);
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private TreeNodeDto buildDto(AVLNode<T> node) {
        if (node == null) {
            return null;
        }
        return new TreeNodeDto(
                node.getKey(),
                node.getHeight(),
                getBalanceFactor(node),
                buildDto(node.getLeft()),
                buildDto(node.getRight())
        );
    }

    // Возвращает JSON в структурированном виде
    public String getOperationJson() {
        TreeOperationResponse response = new TreeOperationResponse(
                "avl",
                this.currentOperation,
                this.currentOperationValue,
                this.steps
        );
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public List<TreeSteps> getSteps() {
        return steps;
    }

    // ==========================================
    // ИСХОДНЫЕ МЕТОДЫ ОБХОДА И ПОСТРОЕНИЯ
    // ==========================================

    @Override
    public void inOrder(Node<T> node, List<? super T> result) {
        if (node instanceof AVLNode<T> avlNode) {
            super.inOrder(avlNode, result);
            return;
        }
        if (node != null) throw new IllegalArgumentException("Node must be of type AVLNode");
    }

    @Override
    public void inOrder(Node<T> node, List<? super T> result, Predicate<? super T> predicate) {
        if (node instanceof AVLNode<T> avlNode) {
            super.inOrder(avlNode, result, predicate);
            return;
        }
        if (node != null) throw new IllegalArgumentException("Node must be of type AVLNode");
    }

    @Override
    public void findNext(Node<T> node, List<? super T> result, int max, Predicate<? super T> predicate) {
        if (node instanceof AVLNode<T> avlNode) {
            super.findNext(avlNode, result, max, predicate);
            return;
        }
        if (node != null) throw new IllegalArgumentException("Node must be of type BSTNode");
    }

    @Override
    public void preOrder(Node<T> node, List<? super T> result) {
        if (node instanceof AVLNode<T> avlNode) {
            super.preOrder(avlNode, result);
            return;
        }
        if (node != null) throw new IllegalArgumentException("Node must be of type BSTNode");
    }

    @Override
    protected AVLNode<T> createNode(T key) {
        return new AVLNode<>(key);
    }

    public void convertToAVLTree(RBTree<T> rbTree) {
        List<T> inOrderList = new ArrayList<>();
        rbTree.inOrder(rbTree.getRoot(), inOrderList);
        AVLNode<T> root = buildAVLTree(inOrderList, 0, inOrderList.size() - 1);
        setRoot(root);
    }

    public AVLNode<T> buildAVLTree(List<T> inOrderList, int start, int end) {
        if (start > end) return null;

        int mid = (start + end) / 2;
        AVLNode<T> node = createNode(inOrderList.get(mid));

        node.setLeft(buildAVLTree(inOrderList, start, mid - 1));
        node.setRight(buildAVLTree(inOrderList, mid + 1, end));

        int leftHeight = (node.getLeft() == null) ? -1 : node.getLeft().getHeight();
        int rightHeight = (node.getRight() == null) ? -1 : node.getRight().getHeight();
        node.setHeight(1 + Math.max(leftHeight, rightHeight));

        return node;
    }

    public void mergeAVLTrees(AVLTree<T> AVLTree1, AVLTree<T> AVLTree2) {
        List<T> inOrderList1 = new ArrayList<>();
        List<T> inOrderList2 = new ArrayList<>();
        AVLTree1.inOrder(AVLTree1.getRoot(), inOrderList1);
        AVLTree2.inOrder(AVLTree2.getRoot(), inOrderList2);
        List<T> combinedList = merge(inOrderList1, inOrderList2);
        AVLNode<T> root = buildAVLTree(combinedList, 0, combinedList.size() - 1);
        setRoot(root);
    }

    public List<T> merge(List<T> list1, List<T> list2) {
        List<T> combinedList = new ArrayList<>();
        int i = 0, j = 0;
        while (i < list1.size() && j < list2.size()) {
            if (list1.get(i).compareTo(list2.get(j)) < 0) {
                combinedList.add(list1.get(i));
                i++;
            } else if (list1.get(i).compareTo(list2.get(j)) > 0) {
                combinedList.add(list2.get(j));
                j++;
            } else {
                combinedList.add(list1.get(i));
                i++;
                j++;
            }
        }
        combinedList.addAll(list1.subList(i, list1.size()));
        combinedList.addAll(list2.subList(j, list2.size()));
        return combinedList;
    }
}