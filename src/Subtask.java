import java.util.ArrayList;

public class Subtask extends Task{
    protected int epicId;

    Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", epicId=" + epicId +
                ", name=" + getName() +
                ", description=" + getDescription() +
                ", status=" + getStatus().toString();
    }

    public int getEpicId() {
        return epicId;
    }
}
