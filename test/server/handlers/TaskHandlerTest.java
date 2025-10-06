package server.handlers;

import model.Status;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private TaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
        taskManager = server.getTaskManager();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetTasksShouldReturn200() throws IOException, InterruptedException {
        // Создаем задачу для проверки
        Task task = new Task("Test Task", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addNewTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /tasks должен возвращать 200");
        assertFalse(taskManager.getTasks().isEmpty(), "Задачи должны сохраняться в менеджере");
    }

    @Test
    void testGetTaskByIdShouldReturn200ForExistingTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Test Task", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addNewTask(task);
        int taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /tasks/{id} для существующей задачи должен возвращать 200");
        assertNotNull(taskManager.getTaskById(taskId), "Задача должна быть найдена в менеджере");
    }

    @Test
    void testGetTaskByIdShouldReturn404ForNonExistingTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "GET /tasks/{id} для несуществующей задачи должен возвращать 404");
    }

    @Test
    void testCreateTaskShouldReturn201AndSaveInManager() throws IOException, InterruptedException {
        String taskJson = """
                {
                    "name": "New Task",
                    "description": "New Task Description",
                    "status": "NEW"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "POST /tasks должен возвращать 201");
        assertEquals(1, taskManager.getTasks().size(), "Задача должна быть сохранена в менеджере");
        assertEquals("New Task", taskManager.getTasks().getFirst().getName(), "Имя задачи должно совпадать");
    }

    @Test
    void testDeleteTaskShouldReturn200AndRemoveFromManager() throws IOException, InterruptedException {
        // Создаем задачу для удаления
        Task task = new Task("Task to delete", "Description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addNewTask(task);
        int taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "DELETE /tasks/{id} должен возвращать 204");
        assertTrue(taskManager.getTasks().isEmpty(), "Задача должна быть удалена из менеджера");
    }
}