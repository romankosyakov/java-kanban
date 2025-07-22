package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.Epic;
import model.Task;
import model.Status;
import model.Subtask;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int id = 1;

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        deleteAllSubtasks(); //так как сабтаски не могут существовать без эпиков
        epics.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
        }
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void addNewTask(Task task) {
        task.setId(id);
        tasks.put(task.getId(), task);
        id++;
    }

    public void addNewEpic(Epic epic) {
        epic.setId(id);
        epics.put(epic.getId(), epic);
        id++;
    }

    public void addNewSubtask(Subtask subtask) {
        int subtaskEpicId = subtask.getEpicId();
        if (epics.get(subtaskEpicId) != null) {
            subtask.setId(id);
            subtasks.put(id, subtask);
            epics.get(subtaskEpicId).addSubtaskId(subtask.getId());
            updateEpicStatus(subtaskEpicId);
            id++;
        } else {
            System.out.println("Отсутствует эпик с таким id. Добавление подзадачи не было выполнено");
        }
    }

    private void updateEpicStatus(int id) {
        Epic epic = epics.get(id);
        if (epic != null && id >= 1) {
            if (epic.getSubtaskIds().isEmpty()) {
                epic.setStatus(Status.NEW);
            } else {
                int countNew = 0;
                int countDone = 0;
                for (Integer subtaskId : epic.getSubtaskIds()) {
                    Status subtaskStatus = subtasks.get(subtaskId).getStatus();
                    switch (subtaskStatus) {
                        case Status.NEW:
                            countNew++;
                            break;
                        case Status.DONE:
                            countDone++;
                            break;
                    }
                }
                if (countNew == epic.getSubtaskIds().size()) {
                    epic.setStatus(Status.NEW);
                } else if (countDone == epic.getSubtaskIds().size()) {
                    epic.setStatus(Status.DONE);
                } else {
                    epic.setStatus(Status.IN_PROGRESS);
                }
            }
        } else {
            System.out.println("Отсутствует эпик с таким id.");
        }
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> subtasksByEpic = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epic.getId()) {
                subtasksByEpic.add(subtask);
            }
        }
        return subtasksByEpic;
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null && id >= 1) {
            return task;
        } else {
            System.out.println("Отсутствует задача с таким id.");
            return null;
        }
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null && id >= 1) {
            return subtask;
        } else {
            System.out.println("Отсутствует подзадача с таким id.");
            return null;
        }
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null && id >= 1) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
            System.out.println("Эпик с id=" + id + " успешно удален. Так же удалены его подзадачи.");
        } else {
            System.out.println("Отсутствует эпик с таким id.");
        }
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null && id >= 1) {
            subtasks.remove(id);
            updateEpicStatus(subtask.getEpicId());
            System.out.println("Подзадача с id=" + id + " успешно удалена.");
        } else {
            System.out.println("Отсутствует подзадача с таким id.");
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            System.out.println("Обновление задачи с id=" + task.getId() + " выполнено успешно.");
        } else {
            System.out.println("Отсутствует задача с таким id для обновления.");
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            System.out.println("Обновление эпика с id=" + epic.getId() + " выполнено успешно.");
        } else {
            System.out.println("Отсутствует эпик с таким id для обновления.");
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
            System.out.println("Обновление подзадачи с id=" + subtask.getId() + " выполнено успешно.");
        } else {
            System.out.println("Отсутствует подзадача с таким id для обновления.");
        }
    }

}
