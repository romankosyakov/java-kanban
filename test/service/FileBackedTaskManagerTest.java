package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
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
    void testSaveAndLoadTasks() {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(2));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        // Перезагружаем менеджер
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);

        List<Task> tasks = loadedManager.getTasks();
        assertEquals(2, tasks.size());
        assertEquals("Task 1", tasks.get(0).getName());
        assertEquals("Description 2", tasks.get(1).getDescription());
        assertEquals(Status.IN_PROGRESS, tasks.get(1).getStatus());
    }

    @Test
    void testTaskFieldsPreservedAfterSaveLoad() {
        Task original = new Task("Test Task", "Test Description", Status.DONE, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        manager.addNewTask(original);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);
        Task loaded = loadedManager.getTaskById(original.getId());

        assertNotNull(loaded);
        assertEquals(original.getId(), loaded.getId());
        assertEquals(original.getName(), loaded.getName());
        assertEquals(original.getDescription(), loaded.getDescription());
        assertEquals(original.getStatus(), loaded.getStatus());
        assertEquals(original.getType(), loaded.getType());
    }

    @Test
    void testIdCounterRestoredCorrectly() {
        // Создаем несколько задач
        for (int i = 0; i < 3; i++) {
            Task task = new Task("Task " + i, "Desc " + i, Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(i));
            manager.addNewTask(task);
        }

        int lastId = manager.getTasks().get(2).getId();

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);

        // Добавляем новую задачу - должна получить следующий id
        Task newTask = new Task("New Task", "New Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(100));
        loadedManager.addNewTask(newTask);

        assertEquals(lastId + 1, newTask.getId());
    }

    @Test
    void testLoadFromFileStaticMethod() {
        Task task = new Task("Test", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        manager.addNewTask(task);

        // Используем статический метод из ТЗ
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);

        assertFalse(loadedManager.getTasks().isEmpty());
        assertEquals(task.getName(), loadedManager.getTaskById(task.getId()).getName());
    }

    @Test
    void testSaveAfterModification() {
        Task task = new Task("Original", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        manager.addNewTask(task);

        // Модифицируем и сохраняем
        task.setStatus(Status.DONE);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals(Status.DONE, loaded.getStatus());
    }

    @Test
    void testCSVFormatCorrect() {
        Task task = new Task("Test Task", "Description", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now()); // Запятая в описании
        manager.addNewTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals("Description", loaded.getDescription());
    }

    @Test
    void testMultipleManagersSameFile() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        manager.addNewTask(task1);

        // Второй менеджер с тем же файлом
        FileBackedTaskManager manager2 = new FileBackedTaskManager(testFile, true);

        assertFalse(manager2.getTasks().isEmpty());
        assertEquals(task1.getId(), manager2.getTasks().getFirst().getId());
    }
}