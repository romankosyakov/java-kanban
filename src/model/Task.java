package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    private int id;
    private final String name;
    private final String description;
    private Status status;
    protected Duration duration;
    protected LocalDateTime startTime;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public Task(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "model.Task{" +
                "name=" + name +
                ", description=" + description +
                ", status=" + status.toString() +
                ", duration=" + getDurationConverted() +
                ", startTime=" + getStartTimeConverted() +
                ", endTime=" + getEndTimeConverted() +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id &&
                Objects.equals(name, task.name) &&
                Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash += id;
        hash *= 29;
        if (name != null) {
            hash += name.hashCode();
        }
        hash *= 31;
        if (description != null) {
            hash += description.hashCode();
        }
        return hash;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public String getDurationConverted() {
        return duration != null ? String.format("%d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart()) : "null";
    }

    public String getStartTimeConverted() {
        return startTime != null ? startTime.format(dateTimeFormatter) : "null";
    }

    public String getEndTimeConverted() {
        return startTime != null && duration != null ?
                getEndTime().format(dateTimeFormatter) : "null";
    }
}
