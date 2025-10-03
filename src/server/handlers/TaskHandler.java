package server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler {
    public TaskHandler(TaskManager taskManager, Gson gson) {
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
            sendSuccess(exchange, taskManager.getTasks());
        } else {
            try {
                int id = Integer.parseInt(idParam);
                Task task = taskManager.getTaskById(id);
                sendSuccess(exchange, task);
            } catch (NotFoundException e) {
                sendNotFound(exchange, "Задача c таким id не найдена");
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID задачи");
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            Task task = gson.fromJson(requestBody, Task.class);

            if (task == null) {
                sendBadRequest(exchange, "Неверный запрос: некорректный JSON формат");
                return;
            }

            String idParam = getPathParameter(exchange, 1);

            if (idParam == null) {
                taskManager.addNewTask(task);
                int newId = task.getId();
                task.setId(newId);
                sendCreated(exchange, task);
            } else {
                int id = Integer.parseInt(idParam);
                task.setId(id);
                taskManager.updateTask(task);
                sendSuccess(exchange, task);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный запрос: некорректный JSON формат");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("пересекается")) {
                sendHasInteractions(exchange, e.getMessage());
            } else {
                sendBadRequest(exchange, e.getMessage());
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при сохранении задачи " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange, 1);

        if (idParam == null) {
            try {
                taskManager.deleteAllTasks();
                sendNoContent(exchange);
            } catch (NotFoundException e) {
                // Если задач нет, это не ошибка - просто возвращаем 204
                sendNoContent(exchange);
            } catch (Exception e) {
                sendInternalError(exchange, "Ошибка при удалении задач: " + e.getMessage());
            }
        } else {
            try {
                int id = Integer.parseInt(idParam);
                taskManager.deleteTaskById(id);
                sendNoContent(exchange);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Неверный формат ID задачи");
            } catch (IllegalArgumentException | NotFoundException e) {
                sendNotFound(exchange, e.getMessage());
            } catch (Exception e) {
                sendInternalError(exchange, e.getMessage());
            }
        }
    }
}
