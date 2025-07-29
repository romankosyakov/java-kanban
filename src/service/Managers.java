package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;

public abstract class Managers {
    TaskManager taskManager;

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = getDefault();
    }

    public static TaskManager getDefault() {
        return new TaskManager() {
            @Override
            public void deleteAllTasks() {

            }

            @Override
            public void deleteAllEpics() {

            }

            @Override
            public void deleteAllSubtasks() {

            }

            @Override
            public ArrayList<Task> getTasks() {
                return null;
            }

            @Override
            public ArrayList<Epic> getEpics() {
                return null;
            }

            @Override
            public ArrayList<Subtask> getSubtasks() {
                return null;
            }

            @Override
            public void addNewTask(Task task) {

            }

            @Override
            public void addNewEpic(Epic epic) {

            }

            @Override
            public void addNewSubtask(Subtask subtask) {

            }

            @Override
            public void updateEpicStatus(int id) {

            }

            @Override
            public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
                return null;
            }

            @Override
            public Epic getEpicById(int id) {
                return null;
            }

            @Override
            public Task getTaskById(int id) {
                return null;
            }

            @Override
            public Subtask getSubtaskById(int id) {
                return null;
            }

            @Override
            public void deleteEpicById(int id) {

            }

            @Override
            public void deleteTaskById(int id) {

            }

            @Override
            public void deleteSubtaskById(int id) {

            }

            @Override
            public void updateTask(Task task) {

            }

            @Override
            public void updateEpic(Epic epic) {

            }

            @Override
            public void updateSubtask(Subtask subtask) {

            }
        };
    }

}
