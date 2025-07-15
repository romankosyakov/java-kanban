import java.util.ArrayList;

public class Subtask extends Task{
    ArrayList<Integer> subtasksIds = new ArrayList<>();
    int subtaskId;
    int epicId;

    Subtask(Epic epic, String name, String description) {
        super(name, description);
        subtaskId = taskId;
        subtasksIds.add(subtaskId);
        epicId = epic.getEpicId();
    }

    public int getEpicId() {
        return epicId;
    }

    public int getSubtaskId() {
        return subtaskId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "subtaskId=" + subtaskId +
                ", epicId=" + epicId +
                ", name=" + name +
                ", description=" + description +
                ", status=" + status.toString();
    }
}
