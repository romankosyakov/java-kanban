package service;

import model.Task;

import java.util.List;

public interface HistoryManager {

    void addInHistory(Task task);

    List<Task> getHistory();
}
