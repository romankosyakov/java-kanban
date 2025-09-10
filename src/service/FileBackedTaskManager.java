package service;

import exceptions.ManagerSaveException;
import model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File dataFile;

    public FileBackedTaskManager(File dataFile, boolean loadData) {
        this.dataFile = checkAndCreateFile(dataFile);
        if (loadData) {
            loadDataFromFile();
        }
    }

    private static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file, false);
        manager.loadDataFromFile();
        return manager;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8, false)) {
            String header = "taskId,type,name,status,description,epicId" + System.lineSeparator();
            writer.write(header);
            List<Task> tasks = getTasks();
            for (Task task : tasks) {
                writer.write(toStringTaskInfoForSave(task) + System.lineSeparator());
            }
            List<Epic> epics = getEpics();
            for (Epic epic : epics) {
                writer.write(toStringTaskInfoForSave(epic) + System.lineSeparator());
            }
            List<Subtask> subtasks = getSubtasks();
            for (Subtask subtask : subtasks) {
                writer.write(toStringTaskInfoForSave(subtask) + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: " + dataFile.getName(), e);
        }
    }

    @Override
    public void addNewSubtask(Subtask subtask) {
        super.addNewSubtask(subtask);
        save();
    }

    @Override
    public void addNewTask(Task task) {
        super.addNewTask(task);
        save();
    }

    @Override
    public void addNewEpic(Epic epic) {
        super.addNewEpic(epic);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    void updateEpicStatus(int id) {
        super.updateEpicStatus(id);
        save();
    }

    @Override
    public void deleteEpicById(int id){
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteTaskById(int id){
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id){
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void updateTask(Task task){
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic){
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask){
        super.updateSubtask(subtask);
        save();
    }

    public String toStringTaskInfoForSave(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType().toString()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription());
        if (task.getType() == TaskType.SUBTASK) {
            sb.append(",");
            sb.append(((Subtask) task).getEpicId());
        }
        return sb.toString();
    }

    private File checkAndCreateFile(File file) {
        if (file == null) {
            file = new File("data.csv");
        }
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("Создан новый файл: " + file.getAbsolutePath());
                } else {
                    System.out.println("Не удалось создать новый файл: " + file.getAbsolutePath());
                }
            } else {
                System.out.println("Используется существующий файл: " + file.getAbsolutePath());
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("Указанный путь ведет к директории, а не к файлу: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при работе с файлом: " + file.getAbsolutePath(), e);
        }
        return file;
    }

    private void loadDataFromFile() {
        try {
            String content = Files.readString(dataFile.toPath(), StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                return;
            }

            String[] lines = content.split(System.lineSeparator());
            if (lines.length <= 1) {
                return;
            }

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    Task task = fromString(line);
                    if (task != null) {
                        restoreTask(task);
                    }
                }
            }
            findAndSetId();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки данных из файла: " + dataFile.getName(), e);
        }
    }

    private void restoreTask(Task task) {
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            super.addNewSubtask(subtask);
            Epic epic = getEpicById(subtask.getEpicId());
            epic.addSubtaskId(subtask.getId());
        } else if (task.getType() == TaskType.EPIC) {
            Epic epic = (Epic) task;
            super.addNewEpic(epic);
        } else {
            super.addNewTask(task);
        }
    }

    private void findAndSetId() {
        int maxId = 1;
        List<Task> tasks = getTasks();
        for (Task currentTask : tasks) {
            int id = currentTask.getId();
            if (id > maxId) {
                maxId = id;
            }
        }
        List<Epic> epics = getEpics();
        for (Epic currentEpic : epics) {
            int id = currentEpic.getId();
            if (id > maxId) {
                maxId = id;
            }
        }
        List<Subtask> subtasks = getSubtasks();
        for (Task currentSubtask : subtasks) {
            int id = currentSubtask.getId();
            if (id > maxId) {
                maxId = id;
            }
        }
        setIdInManager(maxId + 1);
    }

    private Task fromString(String value) {
        try {
            String[] fields = value.split(",", -1); // -1 чтобы сохранить пустые поля
            if (fields.length < 5) {
                return null;
            }

            int id = Integer.parseInt(fields[0]);
            TaskType taskType = TaskType.valueOf(fields[1]);
            String name = fields[2];
            Status status = Status.valueOf(fields[3]);
            String description = fields[4];

            switch (taskType) {
                case TASK:
                    Task task = new Task(name, description, status);
                    task.setId(id);
                    return task;

                case EPIC:
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setStatus(status);
                    return epic;

                case SUBTASK:
                    if (fields.length < 6) {
                        return null;
                    }
                    int epicId = Integer.parseInt(fields[5]);
                    Subtask subtask = new Subtask(name, description, status, epicId);
                    subtask.setId(id);
                    return subtask;

                default:
                    return null;
            }
        } catch (Exception e) {
            System.out.println("Ошибка парсинга строки: " + value + " - " + e.getMessage());
            return null;
        }
    }

}