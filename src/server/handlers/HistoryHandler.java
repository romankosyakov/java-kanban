package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.NotFoundException;
import service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetHistory(exchange);
            } else {
                sendBadRequest(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка сервера " + e.getMessage());
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        try {
            sendSuccess(exchange, taskManager.getHistory());
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(exchange, "Ошибка при получении истории: " + e.getMessage());
        }
    }
}