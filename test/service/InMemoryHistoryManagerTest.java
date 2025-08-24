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
    private Task task4;

    @BeforeEach
    void setup() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Name1", "Desc1", Status.NEW);
        task2 = new Task("Name2", "Desc2", Status.NEW);
        task3 = new Task("Name3", "Desc3", Status.NEW);
        task4 = new Task("Name4", "Desc4", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
        task4.setId(4);
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
    void shouldDeletePreviousSameTaskInHistory() { // так же проверяет работу linkLast и removeNode
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

}
