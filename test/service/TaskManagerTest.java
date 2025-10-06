package service;

import exceptions.IntersectWithOtherTaskException;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();
    }

    // Тесты для задач
    @Test
    void shouldAddAndFindTask() {
        Task task = new Task("Test Task", "Description", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task);

        task.setStatus(Status.DONE);
        taskManager.updateTask(task);

        assertEquals(Status.DONE, taskManager.getTaskById(task.getId()).getStatus());
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task);

        taskManager.deleteTaskById(task.getId());

        assertTrue(taskManager.getTasks().isEmpty(), "Задачи должны быть удалены");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.addNewTask(new Task("Task1", "Desc1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now()));
        taskManager.addNewTask(new Task("Task2", "Desc2", Status.NEW,
                Duration.ofHours(2), LocalDateTime.now().plusHours(3)));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    // Тесты для эпиков
    @Test
    void shouldAddAndFindEpic() {
        Epic epic = new Epic("Epic", "Description", null, null);
        taskManager.addNewEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epic.getId());
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic.getName(), savedEpic.getName(), "Названия эпиков не совпадают");
        assertEquals(epic.getDescription(), savedEpic.getDescription(), "Описания эпиков не совпадают");
    }

    @Test
    void shouldUpdateEpic() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        epic.setStatus(Status.IN_PROGRESS);
        taskManager.updateEpic(epic);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(subtask);

        taskManager.deleteEpicById(epic.getId());

        assertTrue(taskManager.getEpics().isEmpty(), "Эпик должен быть удален");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадачи эпика должны быть удалены");
    }

    // Тесты для подзадач
    @Test
    void shouldAddAndFindSubtaskWithEpic() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask.getName(), savedSubtask.getName(), "Названия подзадач не совпадают");
        assertEquals(epic.getId(), savedSubtask.getEpicId(), "Подзадача должна быть связана с эпиком");
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(subtask);

        subtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);

        assertEquals(Status.DONE, taskManager.getSubtaskById(subtask.getId()).getStatus());
    }

    @Test
    void shouldNotAddSubtaskWithoutEpic() {
        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 999);

        taskManager.addNewSubtask(subtask);

        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадача без эпика не должна добавляться");
    }

    // Тесты для истории просмотров
    @Test
    void shouldAddToHistory() {
        Task task = new Task("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task);

        taskManager.getTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size(), "Задача должна быть добавлена в историю");
        assertEquals(task, taskManager.getHistory().getFirst());
    }

    @Test
    void shouldNotDuplicateInHistory() {
        Task task = new Task("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task);

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size(), "История не должна содержать дубликатов");
    }

    @Test
    void shouldRemoveFromHistoryWhenDeleted() {
        Task task = new Task("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Task2", "Desc2", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(4));
        taskManager.addNewTask(task);
        taskManager.addNewTask(task2);
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.deleteTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size(), "История должна очищаться при удалении задачи");
    }

    // Тесты для расчета статуса Epic
    @Test
    void epicStatusShouldBeNewWhenNoSubtasks() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика со всеми подзадачами NEW должен быть NEW");
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.DONE,
                Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.DONE,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика со всеми подзадачами DONE должен быть DONE");
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksNewAndDone() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.DONE,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика с подзадачами NEW и DONE должен быть IN_PROGRESS");
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.IN_PROGRESS,
                Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика с любой подзадачей IN_PROGRESS должен быть IN_PROGRESS");
    }

    // Тесты на пересечение интервалов
    @Test
    void shouldPreventTaskTimeOverlap() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(2), baseTime);
        taskManager.addNewTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime.plusMinutes(30));

        assertThrows(IntersectWithOtherTaskException.class, () -> taskManager.addNewTask(task2),
                "Должно бросаться исключение при пересечении временных интервалов");
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime);
        taskManager.addNewTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime.plusHours(2));

        assertDoesNotThrow(() -> taskManager.addNewTask(task2),
                "Не должно быть исключений при непересекающихся задачах");
    }

    @Test
    void shouldGetEpicSubtasks() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic.getId());
        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epic.getId());

        assertEquals(2, epicSubtasks.size(), "Должны возвращаться все подзадачи эпика");
        assertTrue(epicSubtasks.contains(sub1));
        assertTrue(epicSubtasks.contains(sub2));
    }
}