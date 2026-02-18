package services;

import Database.dbconnect;
import Entities.Appointment;
import Entities.Day;
import Service.AppointmentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockPreparedStatement;
    @Mock private ResultSet mockResultSet;

    private MockedStatic<dbconnect> mockedDbConnect;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() throws SQLException {
        // Mock dbconnect singleton
        dbconnect mockDb = mock(dbconnect.class);
        when(mockDb.getConnection()).thenReturn(mockConnection);
        mockedDbConnect = mockStatic(dbconnect.class);
        mockedDbConnect.when(dbconnect::getInstance).thenReturn(mockDb);

        appointmentService = new AppointmentService();
    }

    @AfterEach
    void tearDown() {
        mockedDbConnect.close();
    }

    @Test
    void testCreateAppointment_success() throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentDate(LocalDate.now());
        a.setStartTime(LocalTime.of(10, 0));
        a.setStatus("pending");
        a.setTherapistId(1);
        a.setPatientId(2);

        // Mock availability & slot validation
        AppointmentService spyService = spy(appointmentService);
        doReturn(true).when(spyService).isWithinAvailability(anyInt(), any(), any(), any());
        doReturn(true).when(spyService).isSlotValid(anyInt(), any(), any(), any(), any());

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        spyService.create(a);

        verify(mockPreparedStatement).setDate(eq(1), any());
        verify(mockPreparedStatement).setTime(eq(2), any());
        verify(mockPreparedStatement).setTime(eq(3), any());
        verify(mockPreparedStatement).setString(eq(4), eq("pending"));
        verify(mockPreparedStatement).setInt(eq(5), eq(1));
        verify(mockPreparedStatement).setInt(eq(6), eq(2));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteAppointment() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        appointmentService.delete(5);

        verify(mockPreparedStatement).setInt(1, 5);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testListByTherapist() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getDate("appointment_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getTime("start_time")).thenReturn(Time.valueOf("10:00:00"));
        when(mockResultSet.getTime("end_time")).thenReturn(Time.valueOf("11:30:00"));
        when(mockResultSet.getString("status")).thenReturn("pending");
        when(mockResultSet.getInt("therapist_id")).thenReturn(1);
        when(mockResultSet.getInt("patient_id")).thenReturn(2);

        List<Appointment> list = appointmentService.listByTherapist(1);

        assertEquals(1, list.size());
        assertEquals(1, list.get(0).getId());
    }
}
