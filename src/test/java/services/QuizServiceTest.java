package services;

import Database.dbconnect;
import Entities.Question;
import Entities.Quiz;
import Service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private QuizService quizService;
    private MockedStatic<dbconnect> mockedDbConnect;

    @BeforeEach
    void setUp() throws SQLException {
        quizService = new QuizService();

        // Mock the dbconnect singleton
        dbconnect mockDbConnect = mock(dbconnect.class);
        when(mockDbConnect.getConnection()).thenReturn(mockConnection);

        mockedDbConnect = mockStatic(dbconnect.class);
        mockedDbConnect.when(dbconnect::getInstance).thenReturn(mockDbConnect);
    }

    @Test
    void testCreate() throws SQLException {
        // Arrange
        Quiz quiz = new Quiz("Anxiety Test", "Test for anxiety levels", "Mental Health", true);
        quiz.setMinScore(0);
        quiz.setMaxScore(100);
        quiz.setTotalQuestions(10);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        quizService.create(quiz);

        // Assert
        verify(mockPreparedStatement).setString(1, "Anxiety Test");
        verify(mockPreparedStatement).setString(2, "Test for anxiety levels");
        verify(mockPreparedStatement).setString(3, "Mental Health");
        verify(mockPreparedStatement).setInt(4, 10);
        verify(mockPreparedStatement).setBoolean(5, true);
        verify(mockPreparedStatement).setInt(6, 0);
        verify(mockPreparedStatement).setInt(7, 100);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testCreateAndReturnId() throws SQLException {
        // Arrange
        Quiz quiz = new Quiz("Depression Test", "Test for depression", "Mental Health", true);
        quiz.setMinScore(0);
        quiz.setMaxScore(50);
        quiz.setTotalQuestions(5);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(42);

        // Act
        int generatedId = quizService.createAndReturnId(quiz);

        // Assert
        assertEquals(42, generatedId);
        verify(mockPreparedStatement).setString(1, "Depression Test");
        verify(mockPreparedStatement).setString(2, "Test for depression");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testList() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        
        // Mock two quizzes in result set
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("Quiz 1", "Quiz 2");
        when(mockResultSet.getString("description")).thenReturn("Description 1", "Description 2");
        when(mockResultSet.getString("category")).thenReturn("Category 1", "Category 2");
        when(mockResultSet.getInt("total_questions")).thenReturn(5, 10);
        when(mockResultSet.getBoolean("active")).thenReturn(true, false);
        when(mockResultSet.getInt("min_score")).thenReturn(0, 0);
        when(mockResultSet.getInt("max_score")).thenReturn(25, 50);

        // Act
        List<Quiz> quizzes = quizService.list();

        // Assert
        assertNotNull(quizzes);
        assertEquals(2, quizzes.size());
        assertEquals("Quiz 1", quizzes.get(0).getTitle());
        assertEquals("Quiz 2", quizzes.get(1).getTitle());
        assertEquals(5, quizzes.get(0).getTotalQuestions());
        assertEquals(10, quizzes.get(1).getTotalQuestions());
        assertTrue(quizzes.get(0).isActive());
        assertFalse(quizzes.get(1).isActive());
    }

    @Test
    void testRead() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("title")).thenReturn("Stress Test");
        when(mockResultSet.getString("description")).thenReturn("Test for stress levels");
        when(mockResultSet.getString("category")).thenReturn("Stress");
        when(mockResultSet.getInt("total_questions")).thenReturn(8);
        when(mockResultSet.getBoolean("active")).thenReturn(true);
        when(mockResultSet.getInt("min_score")).thenReturn(0);
        when(mockResultSet.getInt("max_score")).thenReturn(40);

        // Act
        Quiz quiz = quizService.read(1);

        // Assert
        assertNotNull(quiz);
        assertEquals("Stress Test", quiz.getTitle());
        assertEquals("Test for stress levels", quiz.getDescription());
        assertEquals("Stress", quiz.getCategory());
        assertEquals(8, quiz.getTotalQuestions());
        assertTrue(quiz.isActive());
        assertEquals(0, quiz.getMinScore());
        assertEquals(40, quiz.getMaxScore());
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testReadNonExistent() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No results

        // Act
        Quiz quiz = quizService.read(999);

        // Assert
        assertNull(quiz);
        verify(mockPreparedStatement).setInt(1, 999);
    }

    @Test
    void testUpdate() throws SQLException {
        // Arrange
        Quiz quiz = new Quiz("Updated Quiz", "Updated description", "Updated Category", false);
        quiz.setId(1L);
        quiz.setMinScore(5);
        quiz.setMaxScore(95);
        quiz.setTotalQuestions(15);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        quizService.update(quiz);

        // Assert
        verify(mockPreparedStatement).setString(1, "Updated Quiz");
        verify(mockPreparedStatement).setString(2, "Updated description");
        verify(mockPreparedStatement).setString(3, "Updated Category");
        verify(mockPreparedStatement).setInt(4, 15);
        verify(mockPreparedStatement).setBoolean(5, false);
        verify(mockPreparedStatement).setInt(6, 5);
        verify(mockPreparedStatement).setInt(7, 95);
        verify(mockPreparedStatement).setInt(9, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDelete() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        quizService.delete(1);

        // Assert
        // Verify that delete was called twice (once for quiz_question, once for quiz)
        verify(mockConnection, times(2)).prepareStatement(anyString());
        verify(mockPreparedStatement, times(2)).setInt(1, 1);
        verify(mockPreparedStatement, times(2)).executeUpdate();
    }

    @Test
    void testAddQuestionToQuiz() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        quizService.addQuestionToQuiz(1, 5);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 5);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testAddQuestionToQuizDuplicateHandling() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        
        // Simulate SQL constraint violation (duplicate entry)
        SQLException duplicateException = new SQLException("Duplicate entry", "23000");
        when(mockPreparedStatement.executeUpdate()).thenThrow(duplicateException);

        // Act & Assert - Should not throw exception due to duplicate handling
        assertDoesNotThrow(() -> quizService.addQuestionToQuiz(1, 5));
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 5);
    }

    @Test
    void testRemoveQuestionFromQuiz() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        quizService.removeQuestionFromQuiz(1, 5);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 5);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testGetQuestionsForQuiz() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Mock three questions in result set
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getLong("id")).thenReturn(1L, 2L, 3L);
        when(mockResultSet.getString("question_text"))
                .thenReturn("Question 1?", "Question 2?", "Question 3?");
        when(mockResultSet.getString("image_path"))
                .thenReturn(null, "/path/to/image.jpg", null);
        when(mockResultSet.getBoolean("required"))
                .thenReturn(true, true, false);

        // Act
        List<Question> questions = quizService.getQuestionsForQuiz(1);

        // Assert
        assertNotNull(questions);
        assertEquals(3, questions.size());
        assertEquals("Question 1?", questions.get(0).getQuestionText());
        assertEquals("Question 2?", questions.get(1).getQuestionText());
        assertEquals("Question 3?", questions.get(2).getQuestionText());
        assertNull(questions.get(0).getImagePath());
        assertEquals("/path/to/image.jpg", questions.get(1).getImagePath());
        assertTrue(questions.get(0).isRequired());
        assertFalse(questions.get(2).isRequired());
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void testGetQuestionsForQuizEmpty() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No questions

        // Act
        List<Question> questions = quizService.getQuestionsForQuiz(1);

        // Assert
        assertNotNull(questions);
        assertEquals(0, questions.size());
        verify(mockPreparedStatement).setInt(1, 1);
    }
}
