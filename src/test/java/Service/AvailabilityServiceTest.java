package Service;

import Entities.Availabilities;
import Entities.Day;
import org.junit.jupiter.api.*;

import java.sql.Time;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AvailabilityServiceTest {

    static AvailabilityService service;
    static int availabilityIdTest;
    static int therapistIdTest = 1; // doit exister en base

    @BeforeAll
    static void setup() {
        service = new AvailabilityService();
    }

    @Test
    @Order(1)
    void testCreateAvailability() throws SQLException {

        Availabilities a = new Availabilities();
        a.setDay(Day.MONDAY);
        a.setStartTime(Time.valueOf("09:00:00"));
        a.setEndTime(Time.valueOf("11:00:00"));
        a.setAvailable(true);
        a.setTherapistId(therapistIdTest);

        service.create(a);

        List<Availabilities> list = service.list();

        assertFalse(list.isEmpty());

        Availabilities created = list.stream()
                .filter(av -> av.getTherapistId() == therapistIdTest &&
                        av.getDay() == Day.MONDAY)
                .findFirst()
                .orElse(null);

        assertNotNull(created);

        availabilityIdTest = created.getId();
    }

    @Test
    @Order(2)
    void testReadAvailability() throws SQLException {

        Availabilities a = service.read(availabilityIdTest);
        assertNotNull(a);
        assertEquals(Day.MONDAY, a.getDay());
    }

    @Test
    @Order(3)
    void testUpdateAvailability() throws SQLException {

        Availabilities a = service.read(availabilityIdTest);
        a.setStartTime(Time.valueOf("10:00:00"));

        service.update(a);

        Availabilities updated = service.read(availabilityIdTest);

        assertEquals(Time.valueOf("10:00:00"), updated.getStartTime());
    }

    @Test
    @Order(4)
    void testListByTherapistId() throws SQLException {

        List<Availabilities> list = service.listByTherapistId(therapistIdTest);

        assertFalse(list.isEmpty());
    }

    @Test
    @Order(5)
    void testDeleteAvailability() throws SQLException {

        service.delete(availabilityIdTest);

        Availabilities deleted = service.read(availabilityIdTest);

        assertNull(deleted);
    }
}
