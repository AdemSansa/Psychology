package Service;

import Entities.Appointment;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppointmentServiceTest {

    static AppointmentService service;
    static int appointmentIdTest;

    @BeforeAll
    static void setup() {
        service = new AppointmentService();
    }

    @Test
    @Order(1)
    void testCreateAppointment() throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentDate(LocalDate.now());
        a.setStartTime(LocalTime.of(10, 0));
        a.setEndTime(LocalTime.of(11, 0));
        a.setStatus("pending");
        a.setTherapistId(18);  // Use valid IDs from your DB
        a.setPatientId(2);

        appointmentIdTest = service.createAndReturnId(a); // You need a createAndReturnId() in your service

        assertTrue(appointmentIdTest > 0);
    }

    @Test
    @Order(2)
    void testReadAppointment() throws SQLException {
        Appointment a = service.read(appointmentIdTest);

        assertNotNull(a);
        assertEquals("pending", a.getStatus());
        assertEquals(18, a.getTherapistId());
    }

    @Test
    @Order(3)
    void testUpdateAppointment() throws SQLException {
        Appointment a = service.read(appointmentIdTest);
        a.setStatus("confirmed");

        a.setId(appointmentIdTest);

        service.update(a);

        Appointment updated = service.read(appointmentIdTest);
        assertEquals("confirmed", updated.getStatus());
    }

    @Test
    @Order(4)
    void testListByTherapist() throws SQLException {
        List<Appointment> list = service.listByTherapist(1);

        assertTrue(list.size() > 0);
        assertEquals(1, list.get(0).getTherapistId());
    }

    @Test
    @Order(5)
    void testDeleteAppointment() throws SQLException {
        service.delete(appointmentIdTest);

        Appointment deleted = service.read(appointmentIdTest);
        assertNull(deleted);
    }
}
