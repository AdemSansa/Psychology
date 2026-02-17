import Service.UserService;
import Entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestJUnit {

    @Test
    void testUserCRUD() {

        try {
            UserService service = new UserService();

            // CREATE
            User user = new User();
            user.setFirstName("JUnit");
            user.setLastName("Test");
            user.setEmail("junit@test.com");
            user.setPassword("123456");
            user.setRole("user");

            service.create(user);

            // EMAIL EXISTS
            boolean exists = service.emailExists("junit@test.com");
            assertTrue(exists);

            // READ
            User userFromDb = service.readByEmail("junit@test.com");
            assertNotNull(userFromDb);

            // UPDATE
            userFromDb.setFirstName("Updated");
            service.update(userFromDb);

            User updatedUser = service.readByEmail("junit@test.com");
            assertEquals("Updated", updatedUser.getFirstName());

            // DELETE
            service.delete(updatedUser.getId());

            User deletedUser = service.readByEmail("junit@test.com");
            assertNull(deletedUser);

        } catch (Exception e) {
            fail("Erreur : " + e.getMessage());
        }
    }
}
