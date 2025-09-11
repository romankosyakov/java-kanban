package service;

import model.*;

public class StringConverter {

    public Task convertToTask(String value) {
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

    public String convertToString(Task task) {
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

}
