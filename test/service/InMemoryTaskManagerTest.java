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
}
