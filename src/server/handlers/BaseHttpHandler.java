package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.TaskManager;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    public String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void sendText(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        if (exchange.getResponseHeaders() != null) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        }

        if (responseBytes.length == 0) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
        }

        OutputStream os = exchange.getResponseBody();
        if (os != null) {
            try (os) {
                if (responseBytes.length > 0) {
                    os.write(responseBytes);
                }
            }
        }
    }

    protected void sendSuccess(HttpExchange exchange, Object responseObject) throws IOException {
        String response = gson.toJson(responseObject);
        sendText(exchange, response, 200);
    }

    protected void sendCreated(HttpExchange exchange, Object responseObject) throws IOException {
        String response = gson.toJson(responseObject);
        sendText(exchange, response, 201);
    }


    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        String response = gson.toJson(message);
        sendText(exchange, response, 404);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String response = gson.toJson(message);
        sendText(exchange, response, 400);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        String response = gson.toJson(message);
        sendText(exchange, response, 406);
    }

    protected void sendInternalError(HttpExchange exchange, String message) throws IOException {
        String response = gson.toJson(message);
        sendText(exchange, response, 500);
    }

    protected void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }

    public String getPathParameter(HttpExchange exchange, int index) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length > index + 1) {
            return pathParts[index + 1];
        }
        return null;
    }
}