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

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlingTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldThrowIntersectExceptionForOverlappingTasks() {
        TaskManager manager = new InMemoryTaskManager();
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(2), baseTime);
        manager.addNewTask(task1);

        Task overlappingTask = new Task("Overlap", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime.plusMinutes(30));

        assertThrows(IntersectWithOtherTaskException.class, () -> manager.addNewTask(overlappingTask),
                "Должно бросаться IntersectWithOtherTaskException при пересечении времени задач");
    }

    @Test
    void shouldNotThrowWhenTasksDoNotOverlap() {
        TaskManager manager = new InMemoryTaskManager();
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime);
        manager.addNewTask(task1);

        Task nonOverlappingTask = new Task("NoOverlap", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime.plusHours(2));

        assertDoesNotThrow(() -> manager.addNewTask(nonOverlappingTask),
                "Не должно быть исключений при непересекающихся задачах");
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
}