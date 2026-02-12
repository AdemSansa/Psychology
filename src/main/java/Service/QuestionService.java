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
        String sql = "INSERT INTO question (quiz_id, question_text, order_index,required,created_at,image_path) VALUES (?, ?, ?, ?,?, ?)";

        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, question.getQuizId());
        ps.setString(2, question.getQuestionText());
        ps.setInt(3, question.getOrderIndex());

        ps.setBoolean(4, question.isRequired());
        ps.setTime(5, new java.sql.Time(System.currentTimeMillis()));
        ps.setString(6, question.getImagePath());


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
            q.setQuiz(rs.getInt("quiz_id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setOrderIndex(rs.getInt("order_index"));
            q.setImagePath(rs.getString("image_path"));
            q.setRequired(rs.getBoolean("required"));
            q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());




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
            q.setQuiz(rs.getInt("quiz_id"));
            q.setQuestionText(rs.getString("question_text"));
            q.setOrderIndex(rs.getInt("order_index"));
            q.setImagePath(rs.getString("image_path"));
            q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            q.setRequired(rs.getBoolean("required"));
        }
        return q;

    }

    @Override
    public void update(Question question) throws SQLException {
        String sql = "UPDATE question SET quiz_id = ?, question_text = ?, order_index = ?, required = ? WHERE id = ?";

        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql);
        ps.setInt(1, question.getQuizId());
        ps.setString(2, question.getQuestionText());
        ps.setInt(3, question.getOrderIndex());
        ps.setBoolean(4, question.isRequired());



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
}
