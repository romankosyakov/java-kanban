import java.util.ArrayList;

public class Epic extends Task{
    ArrayList<Integer> epicsIds = new ArrayList<>();
    public int epicId;

    Epic(String name, String description) {
        super(name, description);
        epicId = taskId;
        epicsIds.add(epicId);
    }

    @Override
    public void setStatus(Status status) {
        return; //Не придумал как по-другому запретить наследнику менять статус через этот метод
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "epicId=" + epicId +
                ", name=" + name +
                ", description=" + description +
                ", status=" + status.toString();
    }
}
