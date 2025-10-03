package service;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1, task2, task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task1", "Desc1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        task2 = new Task("Task2", "Desc2", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(2));
        task3 = new Task("Task3", "Desc3", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(4));
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    // Граничные условия для HistoryManager из ТЗ
    @Test
    void shouldReturnEmptyHistoryWhenNoTasksAdded() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой при инициализации");
    }

    @Test
    void shouldHandleDuplicatesInHistory() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task1);

        assertEquals(1, historyManager.getHistory().size(), "История не должна содержать дубликатов");
    }

    @Test
    void shouldRemoveFromBeginningOfHistory() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);

        historyManager.removeNode(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должна удаляться задача из начала истории");
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveFromMiddleOfHistory() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);

        historyManager.removeNode(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должна удаляться задача из середины истории");
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveFromEndOfHistory() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);

        historyManager.removeNode(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должна удаляться задача из конца истории");
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldMaintainOrderAfterMultipleOperations() {
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        historyManager.addInHistory(task3);

        historyManager.removeNode(task2.getId());
        historyManager.addInHistory(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Должен сохраняться порядок после операций");
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task2, history.get(2));
    }

    @Test
    void shouldHandleRemovalOfNonExistentTask() {
        historyManager.addInHistory(task1);

        assertDoesNotThrow(() -> historyManager.removeNode(999),
                "Не должно быть исключений при удалении несуществующей задачи");
        assertEquals(1, historyManager.getHistory().size());
    }
}