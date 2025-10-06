package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import server.handlers.*;
import server.adapters.*;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int DEFAULT_PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;
    private final int port;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this(taskManager, DEFAULT_PORT);
    }

    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.taskManager = taskManager;
        this.gson = createGson();
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        configureRoutes();
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .setPrettyPrinting()
                .create();
    }

    private void configureRoutes() {
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP менеджера задач запущен на порту " + port);
    }


    public void stop() {
        server.stop(0);
        System.out.println("HTTP менеджера задач остановлен");
    }

    public Gson getGson() {
        return gson;
    }

    public int getPort() {
        return port;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer taskServer = new HttpTaskServer();
            taskServer.start();
        } catch (IOException e) {
            System.err.println("Ошибка запуска HTTP сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
