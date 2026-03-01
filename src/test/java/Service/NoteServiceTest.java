package Service;

import Entities.Note;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoteServiceTest {

    static NoteService service;
    static int noteIdTest;

    @BeforeAll
    static void setup() {
        service = new NoteService();
    }

    @Test
    @Order(1)
    void testCreateNote() throws SQLException {
        Note n = new Note();
        n.setContent("Test note");
        n.setMood("Happy");
        n.setCreatedAt(LocalDateTime.now());
        n.setAppointmentId(25); // use a valid appointment ID in your test DB
        n.setTherapistId(18);   // use a valid therapist ID in your test DB

        // assuming createAndReturnId is implemented like in QuizService
        noteIdTest = service.createAndReturnId(n);

        assertTrue(noteIdTest > 0, "Note ID should be greater than 0");
    }

    @Test
    @Order(2)
    void testReadNote() throws SQLException {
        Note n = service.read(noteIdTest);

        assertNotNull(n, "Note should not be null");
        assertEquals("Test note", n.getContent());
    }

    @Test
    @Order(3)
    void testUpdateNote() throws SQLException {
        Note n = service.read(noteIdTest);
        n.setContent("Updated Note");
        n.setMood("Calm");

        service.update(n);

        Note updated = service.read(noteIdTest);
        assertEquals("Updated Note", updated.getContent());
        assertEquals("Calm", updated.getMood());
    }

    @Test
    @Order(4)
    void testListByAppointment() throws SQLException {
        List<Note> notes = service.listByAppointment(25); // same appointment ID
        assertFalse(notes.isEmpty(), "Notes list should not be empty");
        assertEquals(noteIdTest, notes.get(0).getId());
    }

    @Test
    @Order(5)
    void testDeleteNote() throws SQLException {
        service.delete(noteIdTest);

        Note deleted = service.read(noteIdTest);
        assertNull(deleted, "Deleted note should be null");
    }
}
