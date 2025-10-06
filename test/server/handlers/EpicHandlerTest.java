package server.handlers;

import model.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest {
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
    void testGetEpicsShouldReturn200() throws IOException, InterruptedException {
        // Создаем эпик для проверки
        Epic epic = new Epic("Test Epic", "Description", null, null);
        taskManager.addNewEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /epics должен возвращать 200");
        assertFalse(taskManager.getEpics().isEmpty(), "Эпики должны сохраняться в менеджере");
    }

    @Test
    void testGetEpicByIdShouldReturn200ForExistingEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Description", null, null);
        taskManager.addNewEpic(epic);
        int epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /epics/{id} для существующего эпика должен возвращать 200");
        assertNotNull(taskManager.getEpicById(epicId), "Эпик должен быть найден в менеджере");
    }

    @Test
    void testGetEpicByIdShouldReturn404ForNonExistingEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "GET /epics/{id} для несуществующего эпика должен возвращать 404");
    }

    @Test
    void testCreateEpicShouldReturn201AndSaveInManager() throws IOException, InterruptedException {
        String epicJson = """
                {
                    "name": "New Epic",
                    "description": "New Epic Description"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "POST /epics должен возвращать 201");
        assertEquals(1, taskManager.getEpics().size(), "Эпик должен быть сохранен в менеджере");
        assertEquals("New Epic", taskManager.getEpics().getFirst().getName(), "Имя эпика должно совпадать");
    }

    @Test
    void testGetEpicSubtasksShouldReturn200ForExistingEpic() throws IOException, InterruptedException {
        // Создаем эпик и подзадачу
        Epic epic = new Epic("Test Epic", "Description", null, null);
        taskManager.addNewEpic(epic);
        int epicId = epic.getId();
        assertTrue(epicId > 0, "Эпик должен иметь валидный ID");

        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW, Duration.ofMinutes(1), LocalDateTime.now(), epicId);
        taskManager.addNewSubtask(subtask);
        assertEquals(1, taskManager.getSubtasks().size(), "Подзадача должна быть создана");
        assertEquals(1, taskManager.getEpicSubtasks(epicId).size(), "Подзадачи должны быть у эпика в менеджере");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /epics/{id}/subtasks должен возвращать 200");

    }

    @Test
    void testGetEpicSubtasksShouldReturn404ForNonExistingEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "GET /epics/{id}/subtasks для несуществующего эпика должен возвращать 404");
    }

    @Test
    void testDeleteEpicShouldReturn204AndRemoveFromManager() throws IOException, InterruptedException {
        // Создаем эпик для удаления
        Epic epic = new Epic("Epic to delete", "Description", null, null);
        taskManager.addNewEpic(epic);
        int epicId = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "DELETE /epics/{id} должен возвращать 204");
        assertTrue(taskManager.getEpics().isEmpty(), "Эпик должен быть удален из менеджера");
    }
}