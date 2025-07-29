package service;

import model.*;

import java.util.ArrayList;

public interface TaskManager {
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    void addNewTask(Task task);

    void addNewEpic(Epic epic);

    void addNewSubtask(Subtask subtask);

    void updateEpicStatus(int id);

    ArrayList<Subtask> getEpicSubtasks(Epic epic);

    Epic getEpicById(int id);

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    void deleteEpicById(int id);

    void deleteTaskById(int id);

    void deleteSubtaskById(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);
}
