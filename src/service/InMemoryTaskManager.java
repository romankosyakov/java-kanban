package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int id = 1;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void addNewTask(Task task) {
        task.setId(id);
        tasks.put(task.getId(), task);
        id++;
    }

    @Override
    public void addNewEpic(Epic epic) {
        epic.setId(id);
        epics.put(epic.getId(), epic);
        id++;
    }

    @Override
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

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> subtasksByEpic = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasksByEpic.add(subtasks.get(subtaskId));
        }
        return subtasksByEpic;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.addInHistory(epic);
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.addInHistory(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.addInHistory(subtask);
        return subtask;
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
            System.out.println("Эпик с id=" + id + " успешно удален. Так же удалены его подзадачи.");
        } else {
            System.out.println("Отсутствует эпик с таким id.");
        }
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            subtasks.remove(id);
            updateEpicStatus(subtask.getEpicId());
            System.out.println("Подзадача с id=" + id + " успешно удалена.");
        } else {
            System.out.println("Отсутствует подзадача с таким id.");
        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            System.out.println("Обновление задачи с id=" + task.getId() + " выполнено успешно.");
        } else {
            System.out.println("Отсутствует задача с таким id для обновления.");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            System.out.println("Обновление эпика с id=" + epic.getId() + " выполнено успешно.");
        } else {
            System.out.println("Отсутствует эпик с таким id для обновления.");
        }
    }

    @Override
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
