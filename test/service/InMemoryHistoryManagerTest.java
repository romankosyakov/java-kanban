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

    @BeforeEach
    void setup() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Name1", "Desc1", Status.NEW);
        task2 = new Task("Name2", "Desc2", Status.NEW);
    }

    @Test
    void shouldAddTasksToHistory() {
        task1.setId(100);
        task2.setId(32);
        historyManager.addInHistory(task1);
        historyManager.addInHistory(task2);
        List<Task> hist = historyManager.getHistory();
        assertEquals(2, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name2", hist.get(1).getName());
    }

    @Test
    void shouldNotExceedMaxHistorySize() {
        for (int i = 1; i <= 12; i++) {
            Task t = new Task("Task" + i, "D", Status.NEW);
            t.setId(i);
            historyManager.addInHistory(t);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(11, history.size());
        assertEquals(2, history.getFirst().getId());
    }
}
