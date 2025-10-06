package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

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
            sendInternalError(exchange, "Внутренняя ошибка сервера " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange, 1);

        if (idParam == null) {
            try {
                sendSuccess(exchange, taskManager.getSubtasks());
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при получении подзадач");
            }
        } else {
            try {
                int id = Integer.parseInt(idParam);
                Subtask subtask = taskManager.getSubtaskById(id);
                sendSuccess(exchange, subtask);
            } catch (NotFoundException e) {
                sendNotFound(exchange, "Подзадача c таким id не найдена");
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID подзадачи");
            } catch (Exception e) {
                sendInternalError(exchange, "Внутренняя ошибка при получении подзадачи");
            }
        }
    }


    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);

        try {
            Subtask subtask = gson.fromJson(requestBody, Subtask.class);

            if (subtask == null) {
                sendBadRequest(exchange, "Неверный формат JSON");
                return;
            }

            String idParam = getPathParameter(exchange, 1);

            if (idParam == null) {
                taskManager.addNewSubtask(subtask);
                sendCreated(exchange, subtask);
            } else {
                taskManager.updateSubtask(subtask);
                sendSuccess(exchange, subtask);
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID подзадачи");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("пересекается")) {
                sendHasInteractions(exchange, e.getMessage());
            } else {
                sendBadRequest(exchange, e.getMessage());
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при создании подзадачи " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange, 1);

        if (idParam == null) {
            taskManager.deleteAllSubtasks();
            sendNoContent(exchange);
        } else {
            try {
                int id = Integer.parseInt(idParam);
                taskManager.deleteSubtaskById(id);
                sendNoContent(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID подзадачи");
            } catch (IllegalArgumentException e) {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }
}