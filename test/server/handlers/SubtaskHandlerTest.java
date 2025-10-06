package server.handlers;

import model.Status;
import model.Epic;
import model.Subtask;
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

class SubtaskHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private TaskManager taskManager;
    private int epicId;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
        taskManager = server.getTaskManager();

        // Создаем эпик для подзадач
        Epic epic = new Epic("Test Epic", "Description", null, null);
        taskManager.addNewEpic(epic);
        epicId = epic.getId();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetSubtasksShouldReturn200() throws IOException, InterruptedException {
        // Создаем подзадачу для проверки
        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epicId);
        taskManager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /subtasks должен возвращать 200");
        assertFalse(taskManager.getSubtasks().isEmpty(), "Подзадачи должны сохраняться в менеджере");
    }

    @Test
    void testGetSubtaskByIdShouldReturn200ForExistingSubtask() throws IOException, InterruptedException {
        // Создаем подзадачу
        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epicId);
        taskManager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /subtasks/{id} для существующей подзадачи должен возвращать 200");
        assertNotNull(taskManager.getSubtaskById(subtaskId), "Подзадача должна быть найдена в менеджере");
    }

    @Test
    void testGetSubtaskByIdShouldReturn404ForNonExistingSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "GET /subtasks/{id} для несуществующей подзадачи должен возвращать 404");
    }

    @Test
    void testCreateSubtaskShouldReturn201AndSaveInManager() throws IOException, InterruptedException {
        String subtaskJson = """
                {
                    "name": "New Subtask",
                    "description": "New Subtask Description",
                    "status": "NEW",
                    "epicId": %d
                }
                """.formatted(epicId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "POST /subtasks должен возвращать 201");
        assertEquals(1, taskManager.getSubtasks().size(), "Подзадача должна быть сохранена в менеджере");
        assertEquals("New Subtask", taskManager.getSubtasks().getFirst().getName(), "Имя подзадачи должно совпадать");
    }

    @Test
    void testDeleteSubtaskShouldReturn200AndRemoveFromManager() throws IOException, InterruptedException {
        // Создаем подзадачу для удаления
        Subtask subtask = new Subtask("Subtask to delete", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epicId);
        taskManager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "DELETE /subtasks/{id} должен возвращать 204");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадача должна быть удалена из менеджера");
    }
}