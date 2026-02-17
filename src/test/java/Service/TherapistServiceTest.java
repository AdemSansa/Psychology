package Service;

import Entities.Therapistis;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TherapistServiceTest {

    static TherapistService service;
    static int idTest;
    static String testEmail = "test@email.com";

    @BeforeAll
    static void setup() {
        service = new TherapistService();
    }

    @Test
    @Order(1)
    void testCreateTherapist() throws SQLException {

        Therapistis t = new Therapistis();
        t.setFirstName("Unit");
        t.setLastName("Test");
        t.setEmail(testEmail);
        t.setPassword("123456");
        t.setPhoneNumber("12345678");
        t.setSpecialization("Psychologist");
        t.setDescription("Test description");
        t.setConsultationType("Online");
        t.setStatus("ACTIVE");

        service.create(t);

        List<Therapistis> list = service.list();

        assertFalse(list.isEmpty());

        Therapistis created = list.stream()
                .filter(th -> th.getEmail().equals(testEmail))
                .findFirst()
                .orElse(null);

        assertNotNull(created);

        idTest = created.getId();
    }

    @Test
    @Order(2)
    void testReadTherapist() throws SQLException {

        Therapistis t = service.read(idTest);

        assertNotNull(t);
        assertEquals(testEmail, t.getEmail());
    }

    @Test
    @Order(3)
    void testUpdateTherapist() throws SQLException {

        Therapistis t = service.read(idTest);
        t.setFirstName("UpdatedName");

        service.update(t);

        Therapistis updated = service.read(idTest);

        assertEquals("UpdatedName", updated.getFirstName());
    }

    @Test
    @Order(4)
    void testUpdatePassword() throws SQLException {

        service.updatePassword(testEmail, "newPassword");

        Therapistis t = service.read(idTest);

        assertEquals("newPassword", t.getPassword());
    }

    @Test
    @Order(5)
    void testDeleteTherapist() throws SQLException {

        service.delete(idTest);

        Therapistis deleted = service.read(idTest);

        assertNull(deleted);
    }
}
