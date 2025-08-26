package service;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {

        private final Task task;
        private Node next;
        private Node prev;

        private Node(Node prev, Task task, Node next) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }

    private final Map<Integer, Node> memoryMap = new HashMap<>();
    private Node head;
    private Node tail;


    @Override
    public void addInHistory(Task task) {
        int taskId = task.getId();
        if (taskId >= 1) {
            removeNode(taskId);
            linkLast(taskId, new Node(null, task, null));
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    @Override
    public void removeNode(int id) {
        Node nodeToRemove = memoryMap.get(id);
        if (nodeToRemove != null && memoryMap.containsKey(id)) {
            Node prevNode = nodeToRemove.prev;
            Node nextNode = nodeToRemove.next;

            if (prevNode != null) {
                prevNode.next = nextNode;
            } else {
                head = nextNode;
            }

            if (nextNode != null) {
                nextNode.prev = prevNode;
            } else {
                tail = prevNode;
            }
            memoryMap.remove(id);
        }
    }

    private void linkLast(int taskId, Node node) {
        node.prev = tail;
        memoryMap.put(taskId, node);
        tail = node;
        if (head == null) {
            head = node;
        } else {
            tail.prev.next = node;
        }
    }
}
