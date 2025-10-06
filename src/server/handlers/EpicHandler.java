package server.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Epic;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.matches("/epics/\\d+/subtasks") && "GET".equals(method)) {
                handleGetEpicSubtasks(exchange);
                return;
            }

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendBadRequest(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange, 1);

        if (idParam == null) {
            try {
                sendSuccess(exchange, taskManager.getEpics());
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при получении эпиков");
            }
        } else {
            try {
                int id = Integer.parseInt(idParam);
                Epic epic = taskManager.getEpicById(id);
                sendSuccess(exchange, epic);
            } catch (NotFoundException e) {
                sendNotFound(exchange, "Эпик c таким id не найден");
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID эпика");
            } catch (Exception e) {
                sendInternalError(exchange, "Внутренняя ошибка при получении эпика");
            }
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (pathParts.length < 4) {
                sendBadRequest(exchange, "Неверный формат URL. Ожидается: /epics/{id}/subtasks");
                return;
            }
            int epicId = Integer.parseInt(pathParts[2]);
            List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
            sendSuccess(exchange, subtasks);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID эпика");
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при получении подзадач эпика: " + e.getMessage());
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        if (requestBody == null || requestBody.trim().isEmpty()) {
            sendBadRequest(exchange, "Тело запроса не может быть пустым");
            return;
        }
        try {
            Epic epic = gson.fromJson(requestBody, Epic.class);

            if (epic.getName() == null || epic.getName().trim().isEmpty()) {
                sendBadRequest(exchange, "Поле 'name' является обязательным");
                return;
            }

            String idParam = getPathParameter(exchange, 1);

            if (idParam == null) {
                taskManager.addNewEpic(epic);
                int newId = epic.getId();
                epic.setId(newId);
                sendCreated(exchange, epic);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Невалидный JSON: " + e.getMessage());
        } catch (RuntimeException e) {
            sendBadRequest(exchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при обработке запроса: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange, 1);

        if (idParam == null) {
            try {
                taskManager.deleteAllEpics();
                sendNoContent(exchange);
            } catch (NotFoundException e) {
                sendNoContent(exchange); // подзадач нет - не ошибка
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при удалении подзадач");
            }
        } else {
            try {
                int id = Integer.parseInt(idParam);
                taskManager.deleteEpicById(id);
                sendNoContent(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID подзадачи");
            } catch (IllegalArgumentException | NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при удалении подзадачи");
            }
        }
    }
}
