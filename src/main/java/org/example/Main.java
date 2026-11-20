package org.example;

import Trees.AVLTree;

import java.io.FileWriter;
import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        AVLTree<Integer> tree = new AVLTree<>();

            // Строим исходное дерево
            for (int i = 1; i <= 10; i++) {
                tree.insert(i);
            }

            // Удаляем узел (например, 1, 4 или 7)
            tree.remove(4); // Удаление узла с 2 детьми

            String json = tree.getOperationJson();
            System.out.println(json);

            // Сохраняем в example-delete.json (Шаг 7 плана)
            try (FileWriter fileWriter = new FileWriter("example-delete.json")) {
                fileWriter.write(json);
                System.out.println("Готово! example-delete.json успешно сохранен.");
            } catch (IOException e) {
                System.err.println("Ошибка при сохранении: " + e.getMessage());
        }
    }
}