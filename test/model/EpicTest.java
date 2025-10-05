package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void shouldCreateEpicWithNoSubtasks() {
        Epic epic = new Epic("Epic1", "Description", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldAddAndRemoveSubtaskIds() {
        Epic epic = new Epic("Epic1", "Description", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
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
        Epic epic = new Epic("Epic", "Test", Duration.ofHours(2).plusMinutes(30), LocalDateTime.of(2000, 9, 18, 2, 30, 0, 0));
        epic.setId(10);
        epic.setEndTime(epic.getStartTime().plus(epic.getDuration()));
        String str = epic.toString();
        assertTrue(str.contains("id=10"));
        assertTrue(str.contains("name=Epic"));
        assertTrue(str.contains("description=Test"));
        assertTrue(str.contains("duration=2:30"));
        assertTrue(str.contains("startTime=18.09.2000 02:30"));
        assertTrue(str.contains("endTime=18.09.2000 05:00"));
    }

    @Test
    void cannotAddEpicAsItsOwnSubtask() {
        Epic epic = new Epic("E", "D", Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        epic.setId(5);
        epic.addSubtaskId(5);
        assertTrue(epic.getSubtaskIds().contains(5));
    }
}
