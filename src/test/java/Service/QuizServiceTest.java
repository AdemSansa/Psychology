package Service;

import Entities.Quiz;
import Entities.Question;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuizServiceTest {

    static QuizService service;
    static int quizIdTest;

    @BeforeAll
    static void setup() {
        service = new QuizService();
    }

    @Test
    @Order(1)
    void testCreateQuiz() throws SQLException {

        Quiz quiz = new Quiz();
        quiz.setTitle("Unit Test Quiz");
        quiz.setDescription("Description test");
        quiz.setCategory("Psychology");
        quiz.setTotalQuestions(5);
        quiz.setActive(true);
        quiz.setMinScore(0);
        quiz.setMaxScore(100);

        quizIdTest = service.createAndReturnId(quiz);

        assertTrue(quizIdTest > 0);
    }

    @Test
    @Order(2)
    void testReadQuiz() throws SQLException {

        Quiz quiz = service.read(quizIdTest);

        assertNotNull(quiz);
        assertEquals("Unit Test Quiz", quiz.getTitle());
    }

    @Test
    @Order(3)
    void testUpdateQuiz() throws SQLException {

        Quiz quiz = service.read(quizIdTest);
        quiz.setTitle("Updated Quiz");

        quiz.setId((long) quizIdTest);

        service.update(quiz);

        Quiz updated = service.read(quizIdTest);

        assertEquals("Updated Quiz", updated.getTitle());
    }

    @Test
    @Order(4)
    void testDeleteQuiz() throws SQLException {

        service.delete(quizIdTest);

        Quiz deleted = service.read(quizIdTest);

        assertNull(deleted);
    }
}
