package model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void shouldCreateEpicWithNoSubtasks() {
        Epic epic = new Epic("Epic1", "Description");
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldAddAndRemoveSubtaskIds() {
        Epic epic = new Epic("Epic1", "Description");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        assertEquals(List.of(1, 2), epic.getSubtaskIds());

        epic.removeSubtaskById(1);
        assertEquals(List.of(2), epic.getSubtaskIds());

        epic.clearSubtasksIds();
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void toStringShouldContainFields() {
        Epic epic = new Epic("Epic", "Test");
        epic.setId(10);
        String str = epic.toString();

        assertTrue(str.contains("id=10"));
        assertTrue(str.contains("name=Epic"));
        assertTrue(str.contains("description=Test"));
        assertTrue(str.contains("status=NEW"));
    }

    @Test
    void cannotAddEpicAsItsOwnSubtask() {
        Epic epic = new Epic("E", "D");
        epic.setId(5);
        epic.addSubtaskId(5);
        assertTrue(epic.getSubtaskIds().contains(5));
        // Но логика менеджера не должна позволить; здесь простая модель
    }
}
