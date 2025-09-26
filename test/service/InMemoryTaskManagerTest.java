package service;

import model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    void shouldGenerateSequentialIds() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(2));

        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        assertEquals(1, task1.getId(), "Первая задача должна иметь id=1");
        assertEquals(2, task2.getId(), "Вторая задача должна иметь id=2");
    }

    @Test
    void shouldGetPrioritizedTasks() {
        LocalDateTime earlyTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime lateTime = LocalDateTime.of(2024, 1, 1, 14, 0);

        Task lateTask = new Task("Late", "Desc", Status.NEW,
                Duration.ofHours(1), lateTime);
        Task earlyTask = new Task("Early", "Desc", Status.NEW,
                Duration.ofHours(1), earlyTime);

        taskManager.addNewTask(lateTask);
        taskManager.addNewTask(earlyTask);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(earlyTask, prioritized.get(0), "Задачи должны быть отсортированы по времени начала");
        assertEquals(lateTask, prioritized.get(1), "Задачи должны быть отсортированы по времени начала");
    }

    @Test
    void shouldHandleTasksWithoutTimeInPrioritizedList() {
        Task taskWithTime = new Task("WithTime", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task taskWithoutTime = new Task("WithoutTime", "Desc", Status.NEW, null, null);

        taskManager.addNewTask(taskWithTime);
        taskManager.addNewTask(taskWithoutTime);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size(), "В приоритизированный список должны попадать только задачи с временем");
        assertEquals(taskWithTime, prioritized.getFirst());
    }

    @Test
    void shouldHandleEpicWithoutSubtasksTimeCalculation() {
        Epic epic = new Epic("Epic", "Description", null, null);
        taskManager.addNewEpic(epic);

        assertDoesNotThrow(() -> {
            taskManager.updateEpicStartTime(epic.getId());
            taskManager.updateEpicEndTime(epic.getId());
            taskManager.updateEpicDuration(epic.getId());
        }, "Не должно быть исключений при расчете времени эпика без подзадач");

        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
        assertNull(epic.getDuration());
    }
}