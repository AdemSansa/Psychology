package Service;

import Database.dbconnect;
import Entities.Quiz;
import Entities.QuizResult;
import Entities.User;
import interfaces.Iservice; // Note: package name is lowercase 'interfaces' as seen in Iservice.java

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultService implements Iservice<QuizResult> {

    @Override
    public void create(QuizResult quizResult) throws SQLException {
        String sql = "INSERT INTO quiz_results (user_id, quiz_id, score, result, mood, taken_at) VALUES (?, ?, ?, ?, ?, ?)";

        // Using RETURN_GENERATED_KEYS to optionally get the ID back, though void return
        // type doesn't support returning it directly
        // If needed we can change signature or just ignore it.
        try (PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, quizResult.getUser().getId());
            ps.setLong(2, quizResult.getQuiz().getId());
            ps.setInt(3, quizResult.getScore());
            ps.setInt(4, quizResult.getResult());
            ps.setString(5, quizResult.getMood());

            // Handle takenAt, default to now if null
            if (quizResult.getTakenAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(quizResult.getTakenAt()));
            } else {
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    quizResult.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public List<QuizResult> list() throws SQLException {
        List<QuizResult> results = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results";

        try (Statement st = dbconnect.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                QuizResult qr = mapResultSetToQuizResult(rs);
                results.add(qr);
            }
        }
        return results;
    }

    @Override
    public QuizResult read(int id) throws SQLException {
        String sql = "SELECT * FROM quiz_results WHERE id = ?";
        QuizResult qr = null;

        try (PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    qr = mapResultSetToQuizResult(rs);
                }
            }
        }
        return qr;
    }

    @Override
    public void update(QuizResult quizResult) throws SQLException {
        String sql = "UPDATE quiz_results SET user_id = ?, quiz_id = ?, score = ?, result = ?, mood = ?, taken_at = ? WHERE id = ?";

        try (PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizResult.getUser().getId());
            ps.setLong(2, quizResult.getQuiz().getId());
            ps.setInt(3, quizResult.getScore());
            ps.setInt(4, quizResult.getResult());
            ps.setString(5, quizResult.getMood());

            if (quizResult.getTakenAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(quizResult.getTakenAt()));
            } else {
                // Keep existing or update to now? Usually update implies we might change it,
                // but if it's null in object we might want to respect DB.
                // For now, let's assume we update it to now if null or passed value.
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }

            ps.setLong(7, quizResult.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM quiz_results WHERE id = ?";
        try (PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Get all quiz results for a specific user, ordered by most recent first.
     * Loads full Quiz details (title, category, description) for each result.
     * 
     * @param userId The ID of the user
     * @return List of QuizResult objects with complete Quiz information
     * @throws SQLException if database error occurs
     */
    public List<QuizResult> getResultsByUserId(int userId) throws SQLException {
        List<QuizResult> results = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? ORDER BY taken_at DESC";

        try (PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                QuizService quizService = new QuizService();

                while (rs.next()) {
                    QuizResult qr = mapResultSetToQuizResult(rs);

                    // Load full quiz details instead of just ID
                    int quizId = rs.getInt("quiz_id");
                    Quiz fullQuiz = quizService.read(quizId);
                    if (fullQuiz != null) {
                        fullQuiz.setId((long) quizId);
                        qr.setQuiz(fullQuiz);
                    }

                    results.add(qr);
                }
            }
        }

        return results;
    }

    // Helper method to map ResultSet to QuizResult
    private QuizResult mapResultSetToQuizResult(ResultSet rs) throws SQLException {
        QuizResult qr = new QuizResult();
        qr.setId(rs.getLong("id"));
        qr.setScore(rs.getInt("score"));
        qr.setResult(rs.getInt("result"));
        qr.setMood(rs.getString("mood"));

        Timestamp timestamp = rs.getTimestamp("taken_at");
        if (timestamp != null) {
            qr.setTakenAt(timestamp.toLocalDateTime());
        }

        // Lazy load User and Quiz (just IDs for now to avoid overhead/circular
        // dependencies)
        // If full objects are needed, we would use UserService/QuizService here.
        User user = new User();
        user.setId(rs.getInt("user_id"));
        qr.setUser(user);

        Quiz quiz = new Quiz();
        quiz.setId(rs.getLong("quiz_id"));
        qr.setQuiz(quiz);

        return qr;
    }
}
