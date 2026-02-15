package Service;

import Database.dbconnect;
import Entities.Question;
import Entities.Quiz;
import interfaces.Iservice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuizService implements Iservice<Quiz> {
    @Override
    public void create(Quiz quiz) throws SQLException {
        // Implementation remains void to satisfy interface, but logic moved to
        // createAndReturnId
        createAndReturnId(quiz);
    }

    public int createAndReturnId(Quiz quiz) throws SQLException {

        String sql = "INSERT INTO quiz (title, description,category,total_questions,active,min_score,max_score,created_at,updated_at) VALUES (?, ?,?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, quiz.getTitle());
        ps.setString(2, quiz.getDescription());
        ps.setString(3, quiz.getCategory());
        ps.setInt(4, quiz.getTotalQuestions());
        ps.setBoolean(5, quiz.isActive());
        ps.setInt(6, quiz.getMinScore());
        ps.setInt(7, quiz.getMaxScore());
        ps.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
        ps.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    @Override
    public List<Quiz> list() throws SQLException {
        String sql = "SELECT * FROM quiz";

        List<Quiz> quizes = new ArrayList<Quiz>();
        Statement st = dbconnect.getInstance().getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Quiz q = new Quiz();
            q.setId((long) rs.getInt("id"));
            q.setTitle(rs.getString("title"));
            q.setDescription(rs.getString("description"));
            q.setCategory(rs.getString("category"));
            q.setTotalQuestions(rs.getInt("total_questions"));
            q.setActive(rs.getBoolean("active"));
            q.setMinScore(rs.getInt("min_score"));
            q.setMaxScore(rs.getInt("max_score"));

            quizes.add(q);
        }
        return quizes;

    }

    @Override
    public Quiz read(int id) throws SQLException {
        String sql = "SELECT * FROM quiz WHERE id = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Quiz quiz = null;
        if (rs.next()) {
            quiz = new Quiz();
            quiz.setTitle(rs.getString("title"));
            quiz.setDescription(rs.getString("description"));
            quiz.setCategory(rs.getString("category"));
            quiz.setTotalQuestions(rs.getInt("total_questions"));
            quiz.setActive(rs.getBoolean("active"));
            quiz.setMinScore(rs.getInt("min_score"));
            quiz.setMaxScore(rs.getInt("max_score"));

        }
        return quiz;
    }

    @Override
    public void update(Quiz quiz) throws SQLException {

        String sql = "UPDATE quiz SET title = ?, description = ?, category = ?, total_questions = ?, active = ?, min_score = ?, max_score = ?, updated_at = ? WHERE id = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setString(1, quiz.getTitle());
        ps.setString(2, quiz.getDescription());
        ps.setString(3, quiz.getCategory());
        ps.setInt(4, quiz.getTotalQuestions());
        ps.setBoolean(5, quiz.isActive());
        ps.setInt(6, quiz.getMinScore());
        ps.setInt(7, quiz.getMaxScore());
        ps.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
        ps.setInt(9, quiz.getId().intValue());
        ps.executeUpdate();
        System.out.println("Quiz updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {
        // Delete related questions first (junction table)
        String sqlQuestions = "DELETE FROM quiz_question WHERE quiz_id = ?";
        PreparedStatement psQuestions = dbconnect.getInstance().getConnection().prepareStatement(sqlQuestions);
        psQuestions.setInt(1, id);
        psQuestions.executeUpdate();

        // Then delete the quiz
        String sql = "DELETE FROM quiz WHERE id = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        System.out.println("Deleting quiz with ID: " + id);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Quiz deleted successfully!");

    }

    public void addQuestionToQuiz(int quizId, int questionId) throws SQLException {
        String sql = "INSERT INTO quiz_question (quiz_id, question_id) VALUES (?, ?)";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, quizId);
        ps.setInt(2, questionId);
        try {
            ps.executeUpdate();
        } catch (SQLException e) {

            if (!e.getSQLState().startsWith("23")) {
                throw e;
            }
        }
    }

    public void removeQuestionFromQuiz(int quizId, int questionId) throws SQLException {
        String sql = "DELETE FROM quiz_question WHERE quiz_id = ? AND question_id = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, quizId);
        ps.setInt(2, questionId);
        ps.executeUpdate();
    }
}
