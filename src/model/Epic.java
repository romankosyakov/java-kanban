package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    protected ArrayList<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;

    public Epic(String name, String description, Duration duration, LocalDateTime startTime) {
        super(name, description, Status.NEW, duration, startTime);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtasksIds() {
        subtaskIds.clear();
    }

    public void removeSubtaskById(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "model.Epic{" +
                "id=" + getId() +
                ", name=" + getName() +
                ", description=" + getDescription() +
                ", status=" + getStatus().toString() +
                ", duration=" + getDurationConverted() +
                ", startTime=" + getStartTimeConverted() +
                ", endTime=" + getEndTimeConverted();
    }

}
