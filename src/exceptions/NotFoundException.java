package exceptions;

import model.TaskType;

public class NotFoundException extends RuntimeException {
    public NotFoundException(TaskType taskType) {
        super("Отсутствуют %s. Невозможно выполнить операцию.".formatted(getTaskTypeToString(taskType)));
    }

    private static String getTaskTypeToString(TaskType taskType) {
        return switch (taskType) {
            case EPIC -> "эпики";
            case TASK -> "задачи";
            case SUBTASK -> "подзадачи";
        };
    }

    public NotFoundException(String message) {
        super(message);
    }

}