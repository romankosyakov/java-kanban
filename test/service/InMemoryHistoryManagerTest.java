package service;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setup() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Name1", "Desc1", Status.NEW);
        task2 = new Task("Name2", "Desc2", Status.NEW);
        task3 = new Task("Name3", "Desc3", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    @Test
    void shouldAddTasksToHistory() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        List<Task> hist = historyManager.getHistory();
        assertEquals(2, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name2", hist.get(1).getName());
    }

    @Test
    void shouldDeletePreviousSameTaskInHistoryFromTheMiddle() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.addInHistory(task2);
        List<Task> hist = historyManager.getHistory();
        assertEquals(3, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name3", hist.get(1).getName());
        assertEquals("Name2", hist.get(2).getName());
    }

    @Test
    void shouldDeletePreviousSameTaskInHistoryFromHead() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.addInHistory(task1);
        List<Task> hist = historyManager.getHistory();
        assertEquals(3, hist.size());
        assertEquals("Name2", hist.get(0).getName());
        assertEquals("Name3", hist.get(1).getName());
        assertEquals("Name1", hist.get(2).getName());
    }

    @Test
    void shouldDeletePreviousSameTaskInHistoryFromTail() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.addInHistory(task3);
        List<Task> hist = historyManager.getHistory();
        assertEquals(3, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name2", hist.get(1).getName());
        assertEquals("Name3", hist.get(2).getName());
    }

    @Test
    void shouldDeleteHistoryNodeFromMiddle() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.removeNode(task2.getId());
        List<Task> hist = historyManager.getHistory();
        assertEquals(2, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name3", hist.get(1).getName());
    }

    @Test
    void shouldDeleteHistoryNodeFromHead() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.removeNode(task1.getId());
        List<Task> hist = historyManager.getHistory();
        assertEquals(2, hist.size());
        assertEquals("Name2", hist.get(0).getName());
        assertEquals("Name3", hist.get(1).getName());
    }

    @Test
    void shouldDeleteHistoryNodeFromTail() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.removeNode(task3.getId());
        List<Task> hist = historyManager.getHistory();
        assertEquals(2, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name2", hist.get(1).getName());
    }


}
