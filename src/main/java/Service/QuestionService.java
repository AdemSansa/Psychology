package Service;

import Database.dbconnect;
import Entities.Question;
import interfaces.Iservice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements Iservice<Question> {
    @Override
    public void create(Question question) throws SQLException {
        String sql = "INSERT INTO question ( question_text,required,image_path) VALUES (?,  ?,?)";

        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setString(1, question.getQuestionText());

        ps.setBoolean(2, question.isRequired());
        ps.setString(3, question.getImagePath());

        ps.executeUpdate();
        System.out.println("Question added successfully!");

    }

    @Override
    public List<Question> list() throws SQLException {

        String sql = "SELECT * FROM question";
        List<Question> questions = new ArrayList<Question>();
        Statement st = dbconnect.getInstance().getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Question q = new Question();
            q.setId(rs.getLong("id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setImagePath(rs.getString("image_path"));
            q.setRequired(rs.getBoolean("required"));
            if (rs.getTimestamp("created_at") != null) {
                q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }

            questions.add(q);
        }
        return questions;

    }

    @Override
    public Question read(int id) throws SQLException {
        String sql = "SELECT * FROM question WHERE id = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Question q = null;
        if (rs.next()) {
            q = new Question();
            q.setId(rs.getLong("id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setImagePath(rs.getString("image_path"));
            if (rs.getTimestamp("created_at") != null) {
                q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }

            q.setRequired(rs.getBoolean("required"));
        }
        return q;

    }

    @Override
    public void update(Question question) throws SQLException {
        String sql = "UPDATE question SET question_text = ?, required = ? WHERE id = ?";

        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setString(1, question.getQuestionText());
        ps.setBoolean(2, question.isRequired());
        ps.setLong(3, question.getId());

        ps.executeUpdate();
        System.out.println("Question updated successfully!");

    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM question WHERE id = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Question deleted successfully!");

    }

    public List<Question> getQuestionsByQuizId(int quizId) throws SQLException {
        String sql = "SELECT q.* FROM question q JOIN quiz_question qq ON q.id = qq.question_id WHERE qq.quiz_id = ?";
        List<Question> questions = new ArrayList<>();
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, quizId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Question q = new Question();
            q.setId(rs.getLong("id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setImagePath(rs.getString("image_path"));
            q.setRequired(rs.getBoolean("required"));
            if (rs.getTimestamp("created_at") != null) {
                q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            questions.add(q);
        }
        return questions;
    }

    public List<Question> getAllQuestions() throws SQLException {
        String sql = "SELECT * FROM question";
        List<Question> questions = new ArrayList<>();
        Statement st = dbconnect.getInstance().getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Question q = new Question();
            q.setId(rs.getLong("id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setImagePath(rs.getString("image_path"));
            q.setRequired(rs.getBoolean("required"));
            if (rs.getTimestamp("created_at") != null) {
                q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            questions.add(q);
        }
        return questions;
    }
}
