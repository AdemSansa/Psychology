package services;

import Database.dbconnect;
import Entities.Note;
import Service.NoteService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private ResultSet mockResultSet;

    private MockedStatic<dbconnect> mockedDbConnect;
    private NoteService noteService;

    @BeforeEach
    void setUp() throws SQLException {
        dbconnect mockDb = mock(dbconnect.class);
        when(mockDb.getConnection()).thenReturn(mockConnection);
        mockedDbConnect = mockStatic(dbconnect.class);
        mockedDbConnect.when(dbconnect::getInstance).thenReturn(mockDb);

        noteService = new NoteService();
    }

    @AfterEach
    void tearDown() {
        mockedDbConnect.close();
    }

    @Test
    void testCreateNote() throws SQLException {
        Note n = new Note();
        n.setContent("Test note");
        n.setMood("Happy");
        n.setCreatedAt(LocalDateTime.now());
        n.setAppointmentId(1);
        n.setTherapistId(2);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        noteService.create(n);

        verify(mockPreparedStatement).setString(eq(1), eq("Test note"));
        verify(mockPreparedStatement).setString(eq(2), eq("Happy"));
        verify(mockPreparedStatement).setInt(eq(4), eq(1));
        verify(mockPreparedStatement).setInt(eq(5), eq(2));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testListByAppointment() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("content")).thenReturn("Note 1");
        when(mockResultSet.getString("mood")).thenReturn("Calm");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getInt("appointment_id")).thenReturn(1);
        when(mockResultSet.getInt("therapist_id")).thenReturn(2);

        List<Note> notes = noteService.listByAppointment(1);

        assertEquals(1, notes.size());
        assertEquals("Note 1", notes.get(0).getContent());
    }
}
