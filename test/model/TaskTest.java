package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void shouldCreateTaskWithCorrectFields() {
        Task task = new Task("Task1", "Description", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());

        assertEquals("Task1", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
    }

    @Test
    void shouldUpdateFieldsCorrectly() {
        Task task = new Task("Task1", "Description", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        task.setStatus(Status.IN_PROGRESS);
        task.setId(42);

        assertEquals(Status.IN_PROGRESS, task.getStatus());
        assertEquals(42, task.getId());
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        Task t1 = new Task("Task", "Desc", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        t1.setId(1);

        Task t2 = new Task("Task", "Desc", Status.IN_PROGRESS, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        t2.setId(1);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void equalityByIdRegardlessOfStatusOrDesc() {
        Task t1 = new Task("A", "X", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        Task t2 = new Task("A", "X", Status.DONE, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        t1.setId(1);
        t2.setId(1);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void inequalityIfIdDifferent() {
        Task t1 = new Task("A", "X", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        Task t2 = new Task("A", "X", Status.NEW, Duration.ofHours(2).plusMinutes(30), LocalDateTime.now());
        t1.setId(1);
        t2.setId(2);
        assertNotEquals(t1, t2);
    }
}
