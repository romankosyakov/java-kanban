package service;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
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
        task1 = new Task("Name1", "Desc1", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        task2 = new Task("Name2", "Desc2", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(2));
        task3 = new Task("Name3", "Desc3", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(5));
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    @Test
    void shouldAddTasksToHistory() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        assertEquals(List.of(task1, task2, task3), historyManager.getHistory());
    }

    @Test
    void shouldDeletePreviousSameTaskInHistoryFromTheMiddle() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.addInHistory(task2);
        assertEquals(List.of(task1, task3, task2), historyManager.getHistory());
    }

    @Test
    void shouldDeletePreviousSameTaskInHistoryFromHead() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.addInHistory(task1);
        assertEquals(List.of(task2, task3, task1), historyManager.getHistory());
    }

    @Test
    void shouldDeletePreviousSameTaskInHistoryFromTail() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.addInHistory(task3);
        assertEquals(List.of(task1, task2, task3), historyManager.getHistory());
    }

    @Test
    void shouldDeleteHistoryNodeFromMiddle() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.removeNode(task2.getId());
        assertEquals(List.of(task1, task3), historyManager.getHistory());
    }

    @Test
    void shouldDeleteHistoryNodeFromHead() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.removeNode(task1.getId());
        assertEquals(List.of(task2, task3), historyManager.getHistory());
    }

    @Test
    void shouldDeleteHistoryNodeFromTail() { // так же проверяет работу linkLast и removeNode
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);
        historyManager.removeNode(task3.getId());
        assertEquals(List.of(task1, task2), historyManager.getHistory());
    }

}