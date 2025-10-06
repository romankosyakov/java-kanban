package service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import exceptions.IntersectWithOtherTaskException;
import exceptions.NotFoundException;
import model.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int id = 1;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    void setIdInManager(int id) {
        this.id = id;
    }

    int getMaxId() {
        return id;
    }

    @Override
    public List<Task> getHistory() throws NotFoundException {
        if (historyManager.getHistory().isEmpty()) {
            throw new NotFoundException("Отсутствует история просмотров");
        } else {
            return new ArrayList<>(historyManager.getHistory());
        }
    }

    public ArrayList<Task> getPrioritizedTasks() {
        TreeSet<Task> sortedTasks = Stream.concat(
                        getTasks().stream().filter(Objects::nonNull),
                        getSubtasks().stream().filter(Objects::nonNull)
                )
                .filter(task -> task.getStartTime() != null)
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(Task::getStartTime))
                ));

        return new ArrayList<>(sortedTasks);
    }

    @Override
    public void deleteAllTasks() throws NotFoundException {
        if (tasks.isEmpty()) {
            throw new NotFoundException(TaskType.TASK);
        } else {
            tasks.keySet().forEach(historyManager::removeNode);
            tasks.clear();
        }
    }

    @Override
    public void deleteAllEpics() throws NotFoundException {
        if (epics.isEmpty()) {
            throw new NotFoundException(TaskType.EPIC);
        } else {
            Stream.concat(
                            subtasks.keySet().stream(),
                            epics.keySet().stream()
                    )
                    .forEach(historyManager::removeNode);
            subtasks.clear();
            epics.clear();
        }
    }

    @Override
    public void deleteAllSubtasks() throws NotFoundException {
        if (subtasks.isEmpty()) {
            throw new NotFoundException(TaskType.SUBTASK);
        } else {
            subtasks.keySet().forEach(historyManager::removeNode);
            subtasks.clear();
            epics.values()
                    .forEach(epic -> {
                        epic.getSubtaskIds().clear();
                        epic.setStatus(Status.NEW);
                        epic.setDuration(null);
                        epic.setStartTime(null);
                        epic.setEndTime(null);
                    });
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
    public void addNewTask(Task task) throws NotFoundException {
        if (task == null) {
            throw new NotFoundException("Задача не проинициализирована, добавление невозможно");
        } else {
            if (intersectWithOtherTasks(task)) {
                throw new IntersectWithOtherTaskException("Невозможно добавить задачу! Пересечение времени с другой задачей.");
            }
            task.setId(id);
            tasks.put(task.getId(), task);
            id++;
        }
    }

    @Override
    public void addNewEpic(Epic epic) throws NotFoundException {
        if (epic == null) {
            throw new NotFoundException("Эпик не проинициализирован, добавление невозможно");
        } else {
            epic.setId(id);
            epics.put(epic.getId(), epic);
            id++;
        }
    }

    @Override
    public void addNewSubtask(Subtask subtask) throws NotFoundException {
        if (subtask == null) {
            throw new NotFoundException("Подзадача не проинициализирована, добавление невозможно");
        } else {
            if (intersectWithOtherTasks(subtask)) {
                throw new IntersectWithOtherTaskException("Невозможно добавить подзадачу! Пересечение времени с другой задачей.");
            }
            int subtaskEpicId = subtask.getEpicId();
            if (epics.get(subtaskEpicId) != null) {
                subtask.setId(id);
                subtasks.put(id, subtask);
                epics.get(subtaskEpicId).addSubtaskId(subtask.getId());
                updateEpicStatus(subtaskEpicId);
                updateEpicStartTime(subtaskEpicId);
                updateEpicDuration(subtaskEpicId);
                updateEpicEndTime(subtaskEpicId);
                id++;
            } else {
                System.out.println("Отсутствует эпик с таким id. Добавление подзадачи не было выполнено");
            }
        }
    }

    void updateEpicStatus(int id) {
        Epic epic = epics.get(id);
        List<Subtask> epicSubtasks = getEpicSubtasks(id);
        if (epic != null && id >= 1) {
            if (epicSubtasks.isEmpty()) {
                epic.setStatus(Status.NEW);
            } else {
                if (epicSubtasks.stream()
                        .allMatch(subtask -> subtask.getStatus().equals(Status.NEW))) {
                    epic.setStatus(Status.NEW);
                } else if (epicSubtasks.stream()
                        .allMatch(subtask -> subtask.getStatus().equals(Status.DONE))) {
                    epic.setStatus(Status.DONE);
                } else {
                    epic.setStatus(Status.IN_PROGRESS);
                }
            }
        } else {
            System.out.println("Отсутствует эпик с таким id.");
        }
    }

    private void updateEpicStartTime(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            Optional<LocalDateTime> minStartTime = getEpicSubtasks(id).stream()
                    .map(Task::getStartTime)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo);

            if (minStartTime.isPresent()) {
                epic.setStartTime(minStartTime.get());
            } else {
                epic.setStartTime(null);
            }
        } else {
            System.out.println("Отсутствует эпик с таким id.");
        }
    }

    private void updateEpicEndTime(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            Optional<LocalDateTime> maxEndTime = getEpicSubtasks(id).stream()
                    .map(Task::getEndTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo);

            if (maxEndTime.isPresent()) {
                epic.setEndTime(maxEndTime.get());
            } else {
                epic.setEndTime(null);
            }
        } else {
            System.out.println("Отсутствует эпик с таким id.");
        }
    }

    private void updateEpicDuration(int id) {
        Epic epic = epics.get(id);
        if (epic != null && epic.getStartTime() != null && epic.getEndTime() != null) {
            epic.setDuration(Duration.between(epic.getStartTime(), epic.getEndTime()));
        } else {
            epic.setDuration(null);
        }
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с id=" + epicId + " не найден");
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик не найден");
        }
        historyManager.addInHistory(epic);
        return epic;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача не найдена");
        }
        historyManager.addInHistory(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадача не найдена");
        }
        historyManager.addInHistory(subtask);
        return subtask;
    }

    @Override
    public void deleteEpicById(int id) throws NotFoundException {
        if (epics.isEmpty()) {
            throw new NotFoundException(TaskType.EPIC);
        } else {
            Epic epic = epics.get(id);
            if (epic != null) {
                epic.getSubtaskIds().forEach(subtaskId -> {
                    historyManager.removeNode(subtaskId);
                    subtasks.remove(subtaskId);
                });
                historyManager.removeNode(id);
                epics.remove(id);
                System.out.println("Эпик с id=" + id + " успешно удален. Так же удалены его подзадачи.");
            } else {
                System.out.println("Отсутствует эпик с таким id.");
            }
        }
    }

    @Override
    public void deleteTaskById(int id) {
        if (tasks.isEmpty()) {
            throw new NotFoundException(TaskType.EPIC);
        } else {
            historyManager.removeNode(id);
            tasks.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (subtasks.isEmpty()) {
            throw new NotFoundException(TaskType.EPIC);
        } else {
            Subtask subtask = subtasks.get(id);
            int subtaskEpicId = subtask.getEpicId();
            if (subtask != null) {
                historyManager.removeNode(id);
                subtasks.remove(id);
                updateEpicStatus(subtaskEpicId);
                updateEpicStartTime(subtaskEpicId);
                updateEpicDuration(subtaskEpicId);
                updateEpicEndTime(subtaskEpicId);
                System.out.println("Подзадача с id=" + id + " успешно удалена.");
            } else {
                System.out.println("Отсутствует подзадача с таким id.");
            }
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new NotFoundException("Задача не проинициализирована, добавление невозможно");
        } else {
            if (tasks.containsKey(task.getId())) {
                tasks.put(task.getId(), task);
                System.out.println("Обновление задачи с id=" + task.getId() + " выполнено успешно.");
            } else {
                System.out.println("Отсутствует задача с таким id для обновления.");
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new NotFoundException("Эпик не проинициализирован, добавление невозможно");
        } else {
            if (epics.containsKey(epic.getId())) {
                epics.put(epic.getId(), epic);
                System.out.println("Обновление эпика с id=" + epic.getId() + " выполнено успешно.");
            } else {
                System.out.println("Отсутствует эпик с таким id для обновления.");
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new NotFoundException("Подзадача не проинициализирована, добавление невозможно");
        } else {
            int subtaskEpicId = subtask.getEpicId();
            if (subtasks.containsKey(subtask.getId())) {
                subtasks.put(subtask.getId(), subtask);
                updateEpicStatus(subtaskEpicId);
                updateEpicStartTime(subtaskEpicId);
                updateEpicDuration(subtaskEpicId);
                updateEpicEndTime(subtaskEpicId);
                System.out.println("Обновление подзадачи с id=" + subtask.getId() + " выполнено успешно.");
            } else {
                System.out.println("Отсутствует подзадача с таким id для обновления.");
            }
        }
    }

    private boolean intersectWithOtherTasks(Task externalTask) {
        LocalDateTime externalTaskStartTime = externalTask.getStartTime();
        LocalDateTime externalTaskEndTime = externalTask.getEndTime();

        // Если у внешней задачи нет времени - не проверяем пересечения
        if (externalTaskStartTime == null || externalTaskEndTime == null) {
            return false;
        }

        return getPrioritizedTasks().stream()
                .anyMatch(task -> {
                    LocalDateTime taskStartTime = task.getStartTime();
                    LocalDateTime taskEndTime = task.getEndTime();

                    // Пропускаем задачи без времени
                    if (taskStartTime == null || taskEndTime == null) {
                        return false;
                    }

                    // Проверяем пересечение интервалов
                    return externalTaskStartTime.isBefore(taskEndTime) &&
                            externalTaskEndTime.isAfter(taskStartTime);
                });
    }
}
