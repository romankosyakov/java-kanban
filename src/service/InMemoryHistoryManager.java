package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> memoryList = new ArrayList<>();

    @Override
    public <T extends Task> void addInHistory(T task) {
        if (memoryList.size() > 10) {
            memoryList.removeFirst();
        }
        memoryList.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return memoryList;
    }
}
