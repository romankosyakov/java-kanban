package service;

import exceptions.ManagerSaveException;
import model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File dataFile;

    public static class StringConverter {

        public static Task convertToTask(String value) {
            try {
                String[] fields = value.split(",", -1); // -1 чтобы сохранить пустые поля
                if (fields.length < 6) {
                    return null;
                }

                int id = Integer.parseInt(fields[0]);
                TaskType taskType = TaskType.valueOf(fields[1]);
                String name = fields[2];
                Status status = Status.valueOf(fields[3]);
                String description = fields[4];
                Duration duration = parseCustomFormat(fields[5]);
                LocalDateTime startTime = LocalDateTime.parse(fields[6], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                switch (taskType) {
                    case TASK:
                        Task task = new Task(name, description, status, duration, startTime);
                        task.setId(id);
                        return task;

                    case EPIC:
                        Epic epic = new Epic(name, description, duration, startTime);
                        epic.setId(id);
                        epic.setStatus(status);
                        return epic;

                    case SUBTASK:
                        if (fields.length < 8) {
                            return null;
                        }
                        int epicId = Integer.parseInt(fields[8]);
                        Subtask subtask = new Subtask(name, description, status, duration, startTime, epicId);
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

        public static String convertToString(Task task) {
            StringBuilder sb = new StringBuilder();
            sb.append(task.getId()).append(",");
            sb.append(task.getType().toString()).append(",");
            sb.append(task.getName()).append(",");
            sb.append(task.getStatus()).append(",");
            sb.append(task.getDescription()).append(",");
            sb.append(task.getDurationConverted()).append(",");
            sb.append(task.getStartTimeConverted()).append(",");
            sb.append(task.getEndTimeConverted());
            if (task.getType() == TaskType.SUBTASK) {
                sb.append(",");
                sb.append(((Subtask) task).getEpicId());
            }
            return sb.toString();
        }

        private static Duration parseCustomFormat(String timeString) {
            try {
                String[] parts = timeString.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Неверный формат: " + timeString);
                }

                long hours = Long.parseLong(parts[0]);
                long minutes = Long.parseLong(parts[1]);
                long seconds = Long.parseLong(parts[2]);

                return Duration.ofHours(hours)
                        .plusMinutes(minutes)
                        .plusSeconds(seconds);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Неверный числовой формат: " + timeString, e);
            }
        }

    }

    public FileBackedTaskManager(File dataFile, boolean loadData) {
        try {
            this.dataFile = checkAndCreateFile(dataFile);
            if (loadData) {
                loadDataFromFile();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка создания файла: " + dataFile.getName(), e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Некорректный файл: " + dataFile.getName(), e);
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
            String header = "taskId,type,name,status,description,duration,startTime,endTime,epicId" + System.lineSeparator();
            writer.write(header);
            Stream.concat(
                            Stream.concat(
                                    getTasks().stream(),
                                    getEpics().stream()
                            ),
                            getSubtasks().stream()
                    )
                    .forEach(task -> {
                        try {
                            writer.write(StringConverter.convertToString(task) + System.lineSeparator());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: " + dataFile.getName(), e);
        }
    }

    private File checkAndCreateFile(File file) throws IOException {
        if (file == null) {
            file = new File("data.csv");
        }

        if (!file.exists()) {
            // Создаем родительские директории, если их нет
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Не удалось создать директорию: " + parentDir.getAbsolutePath());
                }
            }
            if (!file.createNewFile()) {
                throw new IOException("Не удалось создать файл: " + file.getAbsolutePath());
            }
            System.out.println("Создан новый файл: " + file.getAbsolutePath());
        } else {
            System.out.println("Используется существующий файл: " + file.getAbsolutePath());
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException("Указанный путь ведет к директории, а не к файлу: " + file.getAbsolutePath());
        }

        if (!file.canWrite()) {
            throw new IOException("Нет прав на запись в файл: " + file.getAbsolutePath());
        }

        return file;
    }

    private void loadDataFromFile() {
        try {
            String content = Files.readString(dataFile.toPath(), StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                return;
            }

            Arrays.stream(content.split(System.lineSeparator()))
                    .skip(1)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(StringConverter::convertToTask)
                    .filter(Objects::nonNull)
                    .forEach(this::restoreTask);

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