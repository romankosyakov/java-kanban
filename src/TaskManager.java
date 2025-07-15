import java.util.ArrayList;

public class TaskManager {
    ArrayList<Task> tasks = new ArrayList<>();
    ArrayList<Epic> epics = new ArrayList<>();
    ArrayList<Subtask> subtasks = new ArrayList<>();
    static int id = 1;

    public static void setId() {
        id++;
    }

    public static int getId() {
        return id;
    }

    public void deleteAll() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
    }

    public void getAll() {
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
        for (Epic epic: epics) {
            System.out.println(epic.toString());
            for (Subtask subtask : subtasks) {
                if (subtask.getEpicId() == epic.getEpicId()) {
                    System.out.println(subtask.toString());
                }
            }
        }
    }

    public void addNewTask(Task task) {
        tasks.add(task);
    }

    public void addNewEpic(Epic epic) {
        epics.add(epic);
    }

    public void addNewSubtask(Subtask subtask) {
        for (Epic epic : epics) {
            if (subtask.getEpicId() == epic.getEpicId()) {
                updateEpicStatus(subtask.getEpicId());
                subtasks.add(subtask);
            } else {
                System.out.println("Отсутствует эпик с таким id. Добавление подзадачи не выполнено.");
            }
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = null;
        for (Epic e : epics) {
            if (epicId == e.getEpicId()) {
                epic = e;
                break;
            } else {
                System.out.println("Отсутствует эпик с таким id.");
                return;
            }
        }

        ArrayList<Subtask> subtasksInEpic = new ArrayList<>();
        for (Subtask subtask : subtasks) {
            if(subtask.getEpicId() == epicId) {
                subtasksInEpic.add(subtask);
            }
        }
        if (subtasksInEpic.isEmpty()){
            epic.setStatus(Status.NEW);
        }

        int countNew = 0;
        int countDone = 0;

        for (Subtask subtask : subtasksInEpic) {
            if (subtask.getStatus() == Status.NEW) {
                countNew++;
            } else if (subtask.getStatus() == Status.DONE) {
                countDone++;
            }
        }

        if (countNew == subtasksInEpic.size()) {
            epic.setStatus(Status.NEW);
        } else if (countDone == subtasksInEpic.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    public void getEpicSubtasks(Epic epic) {
        for (Subtask subtask : subtasks) {
            if (subtask.getEpicId() == epic.getEpicId()) {
                System.out.println(subtask.toString());
            }
        }
    }

    public String getById(int id) {
        for (Task task : subtasks) {
            if (id == task.getTaskId()) {
                return task.toString();
            }
        }
        for (Epic epic : epics) {
            if (id == epic.getEpicId()) {
                return epic.toString();
            }
        }
        for (Subtask subtask : subtasks) {
            if (id == subtask.getSubtaskId()) {
                return subtask.toString();
            }
        }
        return "Отсутствует задача какого-либо из типов с таким идентификатором";
    }

    public void deleteById(int id) {
        if (id >= 1) {
            for (Task task : tasks) {
                if (id == task.getTaskId()) {
                    tasks.remove(task);
                    System.out.println("Задача с id=" + id + " успешно удалена.");
                }
            }
            for (Epic epic : epics) {
                if (id == epic.getEpicId()) {
                    for (Subtask subtask : subtasks) {
                        if (subtask.getEpicId() == id) {
                            subtasks.remove(subtask);
                        }
                    }
                    epics.remove(epic);
                    System.out.println("Эпик с id=" + id + " успешно удален. Так же удалены его сабтаски.");
                }
            }
            for (Subtask subtask : subtasks) {
                if (id == subtask.getSubtaskId()) {
                    subtasks.remove(subtask);
                    updateEpicStatus(subtask.getEpicId());
                    System.out.println("Подзадача с id=" + id + " успешно удалена.");
                }
            }
        } else {
            System.out.println("Отсутствует задача какого-либо из типов с таким идентификатором");
        }
    }

    public void updateTask(int taskId, Task task){
        if (taskId >= 1) {
            for (Task t: tasks) {
                if (taskId == t.getTaskId()) {
                    tasks.remove(t);
                    tasks.add(task);
                    System.out.println("Обновление задачи с id=" + id + " выполнено успешно.");
                } else {
                    System.out.println("В списке отсутствует задача с таким id.");
                }
            }
        } else {
            System.out.println("Некорректное значение id");
        }
    }

    public void updateEpic(int epicId, Epic epic){
        if (epicId >= 1) {
            for (Epic e: epics) {
                if (epicId == e.getTaskId()) {
                    epics.remove(e);
                    epics.add(epic);
                    System.out.println("Обновление эпика с id=" + id + " выполнено успешно.");
                } else {
                    System.out.println("В списке отсутствует эпик с таким id.");
                }
            }
        } else {
            System.out.println("Некорректное значение id");
        }
    }

    public void updateSubtask(int subtaskId, Subtask subtask){
        if (subtaskId >= 1) {
            for (Subtask s: subtasks) {
                if (subtaskId == s.getTaskId()) {
                    subtasks.remove(s);
                    subtasks.add(subtask);
                    updateEpicStatus(subtask.getEpicId());
                    System.out.println("Обновление подзадачи с id=" + id + " выполнено успешно.");
                } else {
                    System.out.println("В списке отсутствует подзадача с таким id.");
                }
            }
        } else {
            System.out.println("Некорректный id");
        }
    }

}
