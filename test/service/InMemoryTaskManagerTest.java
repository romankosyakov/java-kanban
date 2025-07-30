package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        // Создаем задачи разных типов
        Task task = new Task("Задача", "Описание задачи", Status.NEW);
        Epic epic = new Epic("Эпик", "Описание эпика");
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, epic.getId()); // Предполагаем, что есть Epic с id = 1

        List<Task> history = m.getHistoryManager().getHistory();

        // Добавляем задачи
        history.add(task);
        history.add(epic);
        history.add(subtask);

        // Проверяем, что задачи добавлены в историю
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));

        // Убеждаемся, что количество элементов в истории правильное
        assertEquals(3, history.size());
    }

    @Test
    void noConflictBetweenRequestedIdAndGeneratedId() {
        Task t = new Task("T", "D", Status.NEW);
        t.setId(99); // manually set before add
        m.addNewTask(t);
        assertNotEquals(99, t.getId()); // id overwritten
        assertEquals(1, t.getId());
        Task t2 = new Task("X", "Y", Status.NEW);
        m.addNewTask(t2);
        assertEquals(2, t2.getId());
    }

    @Test
    void epicAndSubtaskWithSameIdShouldNotBeEqual() {
        Epic epic1 = new Epic("Epic", "Epic");
        epic1.setId(8);
        Subtask sub1 = new Subtask("Subtask", "SubtaskDescription", Status.NEW, 1);
        sub1.setId(8);
        assertNotEquals(epic1, sub1);
    }

    //deleting
    @Test
    void deleteAllClearsEverything() {
        Task t = new Task("T", "D", Status.NEW);
        m.addNewTask(t);
        Epic e = new Epic("E", "D");
        m.addNewEpic(e);
        Subtask s = new Subtask("S", "D", Status.NEW, e.getId());
        m.addNewSubtask(s);

        m.deleteAllSubtasks();
        assertTrue(m.getSubtasks().isEmpty());
        assertTrue(m.getEpicSubtasks(e).isEmpty());
        assertEquals(Status.NEW, m.getEpicById(e.getId()).getStatus());

        m.deleteAllEpics();
        assertTrue(m.getEpics().isEmpty());

        m.deleteAllTasks();
        assertTrue(m.getTasks().isEmpty());
    }

    //getting
    @Test
    void canGetEverything() {
        Task t = new Task("T", "D", Status.NEW);
        m.addNewTask(t);
        Epic e = new Epic("E", "D");
        m.addNewEpic(e);
        Subtask s = new Subtask("S", "D", Status.NEW, e.getId());
        m.addNewSubtask(s);

        assertEquals(t, m.getTaskById(t.getId()));
        assertEquals(e, m.getEpicById(e.getId()));
        assertEquals(s, m.getSubtaskById(s.getId()));
    }

    //updating
    @Test
    void updateTaskTest() {
        Task t1 = new Task("T", "D", Status.NEW);
        m.addNewTask(t1);
        t1.setStatus(Status.DONE);
        m.updateTask(t1);
        Task t2 = new Task("T", "D", Status.DONE);
        t2.setId(t1.getId());
        assertEquals(t1, t2);
    }

    @Test
    void updateEpicTest() {
        Epic e1 = new Epic("E", "D");
        m.addNewEpic(e1);
        e1.setStatus(Status.DONE);
        m.updateEpic(e1);
        Epic e2 = new Epic("E", "D");
        e2.setId(e1.getId());
        assertEquals(e1, e2);
    }

    @Test
    void updateSubtaskTest() {
        Epic e1 = new Epic("E", "D");
        m.addNewEpic(e1);
        int epicId = e1.getId();
        Subtask s1 = new Subtask("T", "D", Status.NEW, epicId);
        m.addNewSubtask(s1);
        s1.setStatus(Status.DONE);
        m.updateSubtask(s1);
        Subtask s2 = new Subtask("T", "D", Status.DONE, epicId);
        s2.setId(s1.getId());
        assertEquals(s1, s2);
    }

    @Test
    void updateEpicStatusTest() {
        Epic e1 = new Epic("E", "D");
        m.addNewEpic(e1);
        assertEquals(Status.NEW, e1.getStatus());
        Subtask s1 = new Subtask("S1", "Desc", Status.NEW, e1.getId());
        m.addNewSubtask(s1);
        Subtask s2 = new Subtask("S2", "Desc", Status.DONE, e1.getId());
        m.addNewSubtask(s2);
        assertEquals(Status.IN_PROGRESS, e1.getStatus());
    }
}
