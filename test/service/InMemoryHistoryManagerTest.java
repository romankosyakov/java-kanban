package service;

import model.Task;
import model.Status;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    @Test
    void shouldAddTasksToHistory() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task("Name1", "Desc1", Status.NEW);
        t1.setId(100);
        Task t2 = new Task("Name2", "Desc2", Status.NEW);
        t2.setId(32);
        history.addInHistory(t1);
        history.addInHistory(t2);
        List<Task> hist = history.getHistory();
        assertEquals(2, hist.size());
        assertEquals("Name1", hist.get(0).getName());
        assertEquals("Name2", hist.get(1).getName());
    }

    @Test
    void shouldNotExceedMaxHistorySize() {
        HistoryManager historyManager = new InMemoryHistoryManager();
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
