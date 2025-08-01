package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    void shouldCreateSubtaskWithEpicId() {
        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, 100);
        subtask.setId(10);

        assertEquals(100, subtask.getEpicId());
        assertEquals("Sub", subtask.getName());
        assertEquals("Desc", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(10, subtask.getId());
    }

    @Test
    void toStringShouldIncludeEpicId() {
        Subtask subtask = new Subtask("Sub", "Desc", Status.DONE, 42);
        subtask.setId(1);
        String str = subtask.toString();

        assertTrue(str.contains("epicId=42"));
        assertTrue(str.contains("status=DONE"));
    }

    @Test
    void cannotSetSubtaskEpicIdEqualOwnId() {
        Subtask sub = new Subtask("S", "D", Status.NEW, 7);
        sub.setId(7);
        assertEquals(7, sub.getEpicId());
        assertEquals(sub.getId(), sub.getEpicId());
    }
}
