package service;

import exceptions.NotFoundException;
import model.*;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    void deleteAllTasks() throws NotFoundException;

    void deleteAllEpics() throws NotFoundException;

    void deleteAllSubtasks() throws NotFoundException;

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    void addNewTask(Task task) throws NotFoundException;

    void addNewEpic(Epic epic) throws NotFoundException;

    void addNewSubtask(Subtask subtask) throws NotFoundException;

    ArrayList<Subtask> getEpicSubtasks(Epic epic) throws NotFoundException;

    Epic getEpicById(int id);

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    void deleteEpicById(int id) throws NotFoundException;

    void deleteTaskById(int id) throws NotFoundException;

    void deleteSubtaskById(int id) throws NotFoundException;

    void updateTask(Task task) throws NotFoundException;

    void updateEpic(Epic epic) throws NotFoundException;

    void updateSubtask(Subtask subtask) throws NotFoundException;

    List<Task> getHistory() throws NotFoundException;

    ArrayList<Task> getPrioritizedTasks();
}
