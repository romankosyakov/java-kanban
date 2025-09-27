package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    void defaultManagersAreInitialized() {
        TaskManager tm = Managers.getDefault();
        HistoryManager hm = Managers.getDefaultHistory();
        assertNotNull(tm);
        assertNotNull(hm);
        assertTrue(tm.getTasks().isEmpty());
        assertTrue(hm.getHistory().isEmpty());
    }

}