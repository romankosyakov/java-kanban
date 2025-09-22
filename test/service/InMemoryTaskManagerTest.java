package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private InMemoryTaskManager m;

    @BeforeEach
    void setup() {
        m = (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    public void testAddDifferentTasksToHistory() {
        Task task = new Task("Задача", "Описание задачи", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        Epic epic = new Epic("Эпик", "Описание эпика", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());

        m.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 5, 0), epic.getId());

        m.addNewTask(task);
        m.addNewSubtask(subtask);

        // Доступ к задачам (автоматически добавит в историю)
        m.getTaskById(task.getId());
        m.getEpicById(epic.getId());
        m.getSubtaskById(subtask.getId());

        List<Task> history = m.getHistory();

        assertEquals(3, history.size());
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));
    }

    @Test
    void testHistoryDoesNotDuplicateTasks() {
        Task task = new Task("T", "D", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        m.addNewTask(task);

        m.getTaskById(task.getId());
        m.getTaskById(task.getId());
        m.getTaskById(task.getId());

        List<Task> history = m.getHistory();

        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test
    void testHistoryRemovesDeletedTask() {
        Task task = new Task("T", "D", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        m.addNewTask(task);

        m.getTaskById(task.getId());
        assertEquals(1, m.getHistory().size());

        m.deleteTaskById(task.getId());

        assertTrue(m.getHistory().isEmpty());
    }

    @Test
    void noConflictBetweenRequestedIdAndGeneratedId() {
        Task t = new Task("T", "D", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        t.setId(99);
        m.addNewTask(t);
        assertNotEquals(99, t.getId());
        assertEquals(1, t.getId());

        Task t2 = new Task("X", "Y", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        m.addNewTask(t2);
        assertEquals(2, t2.getId());
    }

    @Test
    void epicAndSubtaskWithSameIdShouldNotBeEqual() {
        Epic epic = new Epic("Epic", "Desc", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        m.addNewEpic(epic);

        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now(), epic.getId());
        m.addNewSubtask(sub);
        sub.setId(epic.getId());

        assertNotEquals(epic, sub);
    }

    @Test
    void deleteAllClearsEverythingAndHistory() {
        Task t = new Task("T", "D", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        m.addNewTask(t);
        Epic e = new Epic("E", "D", Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 10, 0, 0));
        m.addNewEpic(e);
        Subtask s = new Subtask("S", "D", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 10, 0, 0), e.getId());
        m.addNewSubtask(s);

        m.getTaskById(t.getId());
        m.getEpicById(e.getId());
        m.getSubtaskById(s.getId());

        assertEquals(3, m.getHistory().size());

        m.deleteAllSubtasks();
        m.deleteAllEpics();
        m.deleteAllTasks();

        assertTrue(m.getTasks().isEmpty());
        assertTrue(m.getEpics().isEmpty());
        assertTrue(m.getSubtasks().isEmpty());
        assertTrue(m.getHistory().isEmpty());
    }

    @Test
    void canGetEverything() {
        Task t = new Task("T", "D", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        m.addNewTask(t);
        Epic e = new Epic("E", "D", Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 5, 0));
        m.addNewEpic(e);
        Subtask s = new Subtask("S", "D", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 10, 0), e.getId());
        m.addNewSubtask(s);

        assertEquals(t, m.getTaskById(t.getId()));
        assertEquals(e, m.getEpicById(e.getId()));
        assertEquals(s, m.getSubtaskById(s.getId()));
    }

    @Test
    void updateTaskTest() {
        Task t1 = new Task("T", "D", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        m.addNewTask(t1);
        t1.setStatus(Status.DONE);
        m.updateTask(t1);

        Task t2 = new Task("T", "D", Status.DONE, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        t2.setId(t1.getId());

        assertEquals(t1, t2);
    }

    @Test
    void updateEpicTest() {
        Epic e1 = new Epic("E", "D", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        m.addNewEpic(e1);
        e1.setStatus(Status.DONE);
        m.updateEpic(e1);

        Epic e2 = new Epic("E", "D", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        e2.setId(e1.getId());

        assertEquals(e1, e2);
    }

    @Test
    void updateSubtaskTest() {
        Epic e1 = new Epic("E", "D", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        m.addNewEpic(e1);
        Subtask s1 = new Subtask("T", "D", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now(), e1.getId());
        m.addNewSubtask(s1);
        s1.setStatus(Status.DONE);
        m.updateSubtask(s1);

        Subtask s2 = new Subtask("T", "D", Status.DONE, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now(), e1.getId());
        s2.setId(s1.getId());

        assertEquals(s1, s2);
    }

    @Test
    void updateEpicStatusTest() {
        Epic e1 = new Epic("E", "D", Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        m.addNewEpic(e1);
        assertEquals(Status.NEW, e1.getStatus());

        Subtask s1 = new Subtask("S1", "Desc", Status.NEW, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0), e1.getId());
        m.addNewSubtask(s1);
        Subtask s2 = new Subtask("S2", "Desc", Status.DONE, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 0, 0), e1.getId());
        m.addNewSubtask(s2); // Проверка интервалов
        assertEquals(Status.NEW, e1.getStatus());
        Subtask s3 = new Subtask("S2", "Desc", Status.DONE, Duration.ofMinutes(1), LocalDateTime.of(2000, 1, 1, 0, 5, 0), e1.getId());
        m.addNewSubtask(s3);
        assertEquals(Status.IN_PROGRESS, e1.getStatus());
    }

}
