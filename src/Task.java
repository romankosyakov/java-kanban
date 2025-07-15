import java.util.ArrayList;

public class Task {
    ArrayList<Integer> tasksIds = new ArrayList<>();
    int taskId;
    String name;
    String description;
    Status status;

    Task (String name, String description) {
        taskId = TaskManager.getId();
        tasksIds.add(taskId);
        TaskManager.setId();
        this.name = name;
        this.description = description;
        status = Status.NEW;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", name=" + name +
                ", description=" + description +
                ", status=" + status.toString();
    }
}
