package service;

import model.Task;

import java.util.List;

public interface HistoryManager {
    <T extends Task> void addInHistory(T task);
    List<Task> getHistory();
}
