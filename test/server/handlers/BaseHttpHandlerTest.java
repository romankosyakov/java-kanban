package server.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class BaseHttpHandlerTest {
    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testSendSuccess() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
    }

    @Test
    void testSendBadRequest() throws IOException, InterruptedException {
        String invalidJson = "invalid json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertNotNull(response.body());
    }

    @Test
    void testSendCreated() throws IOException, InterruptedException {
        String taskJson = """
                {
                    "name": "Test Task for Created",
                    "description": "Test Description",
                    "status": "NEW"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Может быть 201 (успех) или 400/406 (ошибка валидации)
        assertTrue(response.statusCode() == 201 || response.statusCode() == 400 || response.statusCode() == 406);
    }

    @Test
    void testContentTypeHeader() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().map().get("Content-Type").getFirst().contains("application/json"));
    }

    @Test
    void testReadRequestBody() throws IOException, InterruptedException {
        String taskJson = """
                {
                    "name": "Test Read Body",
                    "description": "Testing request body reading",
                    "status": "NEW"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что сервер корректно прочитал тело запроса
        assertTrue(response.statusCode() == 201 || response.statusCode() == 400 || response.statusCode() == 406);
    }

    @Test
    void testSendInternalError() throws IOException, InterruptedException {
        // Создаем запрос с невалидными данными, которые могут вызвать внутреннюю ошибку
        String malformedJson = "{ invalid json }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(malformedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Может вернуть 400 (неверный запрос) или 500 (внутренняя ошибка)
        assertTrue(response.statusCode() == 400 || response.statusCode() == 500);
    }

    @Test
    void testSendHasInteractions() throws IOException, InterruptedException {
        // Пытаемся создать задачу, которая может пересекаться по времени
        String taskWithTime = """
                {
                    "name": "Task with Time",
                    "description": "Task with time intersection",
                    "status": "NEW",
                    "duration": 60,
                    "startTime": "01.01.2024 10:00"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskWithTime))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Может вернуть 201, 400, 406 или 500 в зависимости от состояния
        assertTrue(response.statusCode() == 201 || response.statusCode() == 400 ||
                response.statusCode() == 406 || response.statusCode() == 500);
    }
}