package service;

import model.*;

import java.util.ArrayList;
import java.util.List;

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

    ArrayList<Subtask> getEpicSubtasks(int epicId);

    Epic getEpicById(int id);

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    void deleteEpicById(int id);

    void deleteTaskById(int id);

    void deleteSubtaskById(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    List<Task> getHistory();

    ArrayList<Task> getPrioritizedTasks();
}
