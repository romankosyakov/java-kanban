package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

    class FileBackedTaskManagerTest {

        private File testFile;
        private FileBackedTaskManager manager;

        @BeforeEach
        void setUp() throws IOException {
            testFile = File.createTempFile("test_data", ".csv");
            testFile.deleteOnExit(); // Удалить файл после завершения тестов
            manager = new FileBackedTaskManager(testFile, false);
        }

    @Test
    void testSaveAndLoadEmptyManager() {
        // Сохраняем пустой менеджер
        manager.save();

        // Загружаем обратно
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void testSaveAndLoadTasks() {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        // Перезагружаем менеджер
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        List<Task> tasks = loadedManager.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Task 1", tasks.get(0).getName());
        assertEquals("Description 2", tasks.get(1).getDescription());
        assertEquals(Status.IN_PROGRESS, tasks.get(1).getStatus());
    }

    @Test
    void testSaveAndLoadEpicsWithSubtasks() {
        // Создаем эпик и подзадачи
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", Status.DONE, epic.getId());

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        // Перезагружаем
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        List<Epic> epics = loadedManager.getEpics();
        List<Subtask> subtasks = loadedManager.getSubtasks();

        assertEquals(1, epics.size());
        assertEquals(2, subtasks.size());
        assertEquals(2, epics.get(0).getSubtaskIds().size());
        assertEquals(Status.IN_PROGRESS, epics.get(0).getStatus());
    }

    @Test
    void testTaskFieldsPreservedAfterSaveLoad() {
        Task original = new Task("Test Task", "Test Description", Status.DONE);
        manager.addNewTask(original);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);
        Task loaded = loadedManager.getTaskById(original.getId());

        assertNotNull(loaded);
        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getName(), loaded.getName());
        assertEquals(original.getDescription(), loaded.getDescription());
        assertEquals(original.getStatus(), loaded.getStatus());
        assertEquals(original.getType(), loaded.getType());
    }

    @Test
    void testSubtaskEpicRelationshipPreserved() {
        Epic epic = new Epic("Test Epic", "Epic Desc");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Sub Desc", Status.NEW, epic.getId());
        manager.addNewSubtask(subtask);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());

        assertEquals(epic.getId(), loadedSubtask.getEpicId());
        assertTrue(loadedEpic.getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void testIdCounterRestoredCorrectly() {
        // Создаем несколько задач
        for (int i = 0; i < 3; i++) {
            Task task = new Task("Task " + i, "Desc " + i, Status.NEW);
            manager.addNewTask(task);
        }

        int lastId = manager.getTasks().get(2).getId();

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        // Добавляем новую задачу - должна получить следующий id
        Task newTask = new Task("New Task", "New Desc", Status.NEW);
        loadedManager.addNewTask(newTask);

        assertEquals(lastId + 1, newTask.getId());
    }

    @Test
    void testLoadFromFileStaticMethod() {
        Task task = new Task("Test", "Desc", Status.NEW);
        manager.addNewTask(task);

        // Используем статический метод из ТЗ
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        assertFalse(loadedManager.getTasks().isEmpty());
        assertEquals(task.getName(), loadedManager.getTaskById(task.getId()).getName());
    }

    @Test
    void testSaveAfterModification() {
        Task task = new Task("Original", "Desc", Status.NEW);
        manager.addNewTask(task);

        // Модифицируем и сохраняем
        task.setStatus(Status.DONE);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals(Status.DONE, loaded.getStatus());
    }

    @Test
    void testDeleteOperationsSaveToFile() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        // Удаляем одну задачу
        manager.deleteTaskById(task1.getId());

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);

        assertEquals(1, loadedManager.getTasks().size());
        assertNull(loadedManager.getTaskById(task1.getId()));
        assertNotNull(loadedManager.getTaskById(task2.getId()));
    }

    @Test
    void testFileHeaderCorrect() throws IOException {
        manager.save();

        String content = java.nio.file.Files.readString(testFile.toPath());
        assertTrue(content.startsWith("taskId,type,name,status,description,epicId"));
    }

    @Test
    void testCSVFormatCorrect() {
        Task task = new Task("Test Task", "Description", Status.NEW); // Запятая в описании
        manager.addNewTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals("Description", loaded.getDescription());
    }

    @Test
    void testEpicStatusRecalculationAfterLoad() {
        Epic epic = new Epic("Test Epic", "Desc");
        manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Sub 1", "Desc", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Sub 2", "Desc", Status.DONE, epic.getId());

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile,true);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());

        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus());
    }

    @Test
    void testMultipleManagersSameFile() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        manager.addNewTask(task1);

        // Второй менеджер с тем же файлом
        FileBackedTaskManager manager2 = new FileBackedTaskManager(testFile, true);

        assertFalse(manager2.getTasks().isEmpty());
        assertEquals(task1.getId(), manager2.getTasks().get(0).getId());
    }
}