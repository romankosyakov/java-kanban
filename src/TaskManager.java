import java.util.HashMap;

public class TaskManager {
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int id = 1;

    public void setIdValue() {
        id++;
    }

    public int getIdValue() {
        return id;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        deleteAllSubtasks(); //так как сабтаски не могут существовать без эпиков
        epics.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void addNewTask(Task task) {
        task.setId(getIdValue());
        tasks.put(task.getId(), task);
        setIdValue();
    }

    public void addNewEpic(Epic epic) {
        epic.setId(getIdValue());
        epics.put(epic.getId(), epic);
        setIdValue();
    }

    public void addNewSubtask(Subtask subtask) {
        if (epics.get(subtask.getEpicId()) != null) {
            subtask.setId(getIdValue());
            subtasks.put(getIdValue(), subtask);
            epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
            setIdValue();
        } else {
            System.out.println("Отсутствует эпик с таким id. Добавление подзадачи не было выполнено");
        }
    }

    private void updateEpicStatus(int id) {
        Epic epic = epics.get(id);
        if (epic != null && id >= 1) {
            if (epic.getSubtaskIds().isEmpty()) {
                epic.setStatus(Status.NEW);
                return;
            } else {
                int countNew = 0;
                int countDone = 0;
                for (Integer subtaskId : epic.getSubtaskIds()) {
                    Status subtaskStatus = subtasks.get(subtaskId).getStatus();
                    switch (subtaskStatus) {
                        case NEW:
                            countNew++;
                            break;
                        case DONE:
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
            return;
        }
    }

    public HashMap<Integer, Subtask> getEpicSubtasks(Epic epic) {
        HashMap<Integer, Subtask> subtasksByEpic = new HashMap<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasksByEpic.put(subtaskId, subtasks.get(subtaskId));
        }
        return subtasksByEpic;
    }

    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null && id >= 1) {
            return epic;
        } else {
            System.out.println("Отсутствует эпик с таким id.");
            return null;
        }
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
        Task task = tasks.get(id);
        if (task != null && id >= 1) {
            tasks.remove(id);
            System.out.println("Задача с id=" + id + " успешно удалена.");
        } else {
            System.out.println("Отсутствует задача с таким id.");
        }
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null && id >= 1) {
            subtasks.remove(id);
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
