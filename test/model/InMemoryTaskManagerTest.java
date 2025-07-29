package model;

import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.Managers;
import service.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    @Test
    void tasksShouldBeEqualIfIdIsSame() {
        Task task1 = new Task("Task", "TaskDescription", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task", "TaskDescription", Status.DONE);
        task2.setId(1);

        assertEquals(task1, task2);
    }

    @Test
    void subtasksShouldBeEqualIfIdIsSame() {
        Subtask sub1 = new Subtask("Subtask", "SubtaskDescription", Status.NEW, 1);
        sub1.setId(2);
        Subtask sub2 = new Subtask("Subtask", "SubtaskDescription", Status.DONE, 1);
        sub2.setId(2);

        assertEquals(sub1, sub2);
    }

    @Test
    void epicsShouldBeEqualIfIdIsSame() {
        Epic epic1 = new Epic("Epic", "EpicDescription");
        epic1.setId(8);
        Epic epic2 = new Epic("Epic", "EpicDescription");
        epic2.setId(8);

        assertEquals(epic1, epic2);
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
    void shouldAddAndFindDifferentTaskTypesById() {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        Task task = new Task("Task", "TaskDescription", Status.NEW);
        inMemoryTaskManager.addNewTask(task);
        Epic epic = new Epic("Epic", "EpicDescription");
        inMemoryTaskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask", "SubtaskDescription", Status.DONE, epic.getId());
        inMemoryTaskManager.addNewSubtask(subtask);

        assertEquals(task, inMemoryTaskManager.getTaskById(task.getId()));
        assertEquals(epic, inMemoryTaskManager.getEpicById(epic.getId()));
        assertEquals(subtask, inMemoryTaskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void getDefaultHistoryShouldReturnTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "TaskManager instance should not be null");
    }


    @Test
    void subtaskShouldNotBeEpic() {
        Epic epic1 = new Epic("name", "description");
        epic1.setId(1);
        Subtask sub1 = new Subtask("name", "description", Status.NEW, epic1.getId());
        sub1.setId(1);
        assertNotEquals(epic1, sub1);
    }

}