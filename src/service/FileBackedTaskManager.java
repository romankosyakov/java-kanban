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

    StringConverter converter = new StringConverter();

    public FileBackedTaskManager(File dataFile, boolean loadData) {
        this.dataFile = checkAndCreateFile(dataFile);
        if (loadData) {
            loadDataFromFile();
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
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }


    private void save() {
        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8, false)) {
            String header = "taskId,type,name,status,description,epicId" + System.lineSeparator();
            writer.write(header);
            List<Task> tasks = getTasks();
            for (Task task : tasks) {
                writer.write(converter.convertToString(task) + System.lineSeparator());
            }
            List<Epic> epics = getEpics();
            for (Epic epic : epics) {
                writer.write(converter.convertToString(epic) + System.lineSeparator());
            }
            List<Subtask> subtasks = getSubtasks();
            for (Subtask subtask : subtasks) {
                writer.write(converter.convertToString(subtask) + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: " + dataFile.getName(), e);
        }
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
                    Task task = converter.convertToTask(line);
                    if (task != null) {
                        restoreTask(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки данных из файла: " + dataFile.getName(), e);
        }
    }

    private void restoreTask(Task task) {
        int taskId = task.getId();
        if (taskId > getMaxId()) {
            setIdInManager(taskId);
        }
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            super.addNewSubtask(subtask);
        } else if (task.getType() == TaskType.EPIC) {
            Epic epic = (Epic) task;
            super.addNewEpic(epic);
        } else {
            super.addNewTask(task);
        }
    }
}