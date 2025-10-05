package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(String name, String description, Status status, Duration duration, LocalDateTime startTime, int epicId) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "model.Subtask{" +
                "id=" + getId() +
                ", epicId=" + epicId +
                ", name=" + getName() +
                ", description=" + getDescription() +
                ", status=" + getStatus().toString() +
                ", duration=" + getDurationConverted() +
                ", startTime=" + getStartTimeConverted() +
                ", endTime=" + getEndTimeConverted() +
                "}";
    }

    public int getEpicId() {
        return epicId;
    }
}
