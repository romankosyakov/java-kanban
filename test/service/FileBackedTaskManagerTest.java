package service;

import exceptions.ManagerSaveException;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    private File testFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            testFile = Files.createTempFile(tempDir, "test_data", ".csv").toFile();
            return new FileBackedTaskManager(testFile, false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }

    @Test
    void shouldHandleDirectoryInsteadOfFile() throws IOException {
        Path tempDirectory = tempDir.resolve("directory");
        Files.createDirectory(tempDirectory);

        File directoryFile = tempDirectory.toFile();

        assertThrows(ManagerSaveException.class, () -> new FileBackedTaskManager(directoryFile, false),
                "Должно бросаться ManagerSaveException при указании директории вместо файла");
    }

    @Test
    void shouldNotThrowWithValidFile() throws IOException {
        Path validFile = tempDir.resolve("valid.csv");
        Files.createFile(validFile);

        assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(validFile.toFile(), false);
            Task task = new Task("Test", "Desc", Status.NEW,
                    Duration.ofHours(1), LocalDateTime.now());
            manager.addNewTask(task);
        }, "Не должно быть исключений с валидным файлом");
    }

    @Test
    void shouldThrowManagerSaveExceptionOnReadOnlyFile() throws IOException {
        Path readOnlyPath = tempDir.resolve("readonly.csv");
        Files.createFile(readOnlyPath);
        File readOnlyFile = readOnlyPath.toFile();

        // Устанавливаем read-only атрибут
        readOnlyFile.setReadOnly();

        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(readOnlyFile, false);
            Task task = new Task("Test", "Desc", Status.NEW,
                    Duration.ofHours(1), LocalDateTime.now());
            manager.addNewTask(task);
        }, "Должно бросаться ManagerSaveException при записи в read-only файл");

        // Восстанавливаем права для очистки
        readOnlyFile.setWritable(true);
    }

    @Test
    void shouldHandleFileSaveException() throws IOException {
        Path validFile = tempDir.resolve("valid.csv");
        Files.createFile(validFile);

        FileBackedTaskManager manager = new FileBackedTaskManager(validFile.toFile(), false);

        // Делаем файл read-only после создания менеджера
        validFile.toFile().setReadOnly();

        Task task = new Task("Test", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());

        assertThrows(ManagerSaveException.class, () -> manager.addNewTask(task),
                "Должно бросаться ManagerSaveException при ошибке сохранения");

        // Восстанавливаем права
        validFile.toFile().setWritable(true);
    }

    @Test
    void testSaveAndLoadTasks() {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(2));

        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

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
        taskManager.addNewTask(original);

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
            taskManager.addNewTask(task);
        }

        int lastId = taskManager.getTasks().get(2).getId();

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);

        // Добавляем новую задачу - должна получить следующий id
        Task newTask = new Task("New Task", "New Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now().plusDays(100));
        loadedManager.addNewTask(newTask);

        assertEquals(lastId + 1, newTask.getId());
    }

    @Test
    void testLoadFromFileStaticMethod() {
        Task task = new Task("Test", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        taskManager.addNewTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);

        assertFalse(loadedManager.getTasks().isEmpty());
        assertEquals(task.getName(), loadedManager.getTaskById(task.getId()).getName());
    }

    @Test
    void testSaveAfterModification() {
        Task task = new Task("Original", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        taskManager.addNewTask(task);

        // Модифицируем и сохраняем
        task.setStatus(Status.DONE);
        taskManager.updateTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals(Status.DONE, loaded.getStatus());
    }

    @Test
    void testCSVFormatCorrect() {
        Task task = new Task("Test Task", "Description", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now()); // Запятая в описании
        taskManager.addNewTask(task);

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(testFile, true);
        Task loaded = loadedManager.getTaskById(task.getId());

        assertEquals("Description", loaded.getDescription());
    }

    @Test
    void testMultipleManagersSameFile() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        taskManager.addNewTask(task1);

        // Второй менеджер с тем же файлом
        FileBackedTaskManager manager2 = new FileBackedTaskManager(testFile, true);

        assertFalse(manager2.getTasks().isEmpty());
        assertEquals(task1.getId(), manager2.getTasks().getFirst().getId());
    }
}