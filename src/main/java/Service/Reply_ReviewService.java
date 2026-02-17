package Service;

import Database.dbconnect;
import Entities.ReviewReply;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Reply_ReviewService implements Iservice<ReviewReply> {

    private final Connection cnx;

    public Reply_ReviewService() {
        cnx = dbconnect.getInstance().getConnection();
    }

    @Override
    public void create(ReviewReply reply) throws SQLException {

        String sql = "INSERT INTO review_reply (content, id_review, id_therapist) VALUES (?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, reply.getContent());
            ps.setInt(2, reply.getReviewId());
            ps.setInt(3, reply.getIdTherapist());
            ps.executeUpdate();
        }

        System.out.println("Reply added successfully!");
    }

    @Override
    public List<ReviewReply> list() throws SQLException {

        List<ReviewReply> replies = new ArrayList<>();
        String sql = "SELECT * FROM review_reply";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                replies.add(new ReviewReply(
                        rs.getInt("id_reply"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("id_review"),
                        rs.getInt("id_therapist")
                ));
            }
        }

        return replies;
    }

    @Override
    public ReviewReply read(int id) throws SQLException {

        String sql = "SELECT * FROM review_reply WHERE id_reply=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ReviewReply(
                        rs.getInt("id_reply"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("id_review"),
                        rs.getInt("id_therapist")
                );
            }
        }
        return null;
    }

    @Override
    public void update(ReviewReply reply) throws SQLException {

        String sql = "UPDATE review_reply SET content=? WHERE id_reply=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, reply.getContent());
            ps.setInt(2, reply.getIdReply());
            ps.executeUpdate();
        }

        System.out.println("Reply updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM review_reply WHERE id_reply=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        System.out.println("Reply deleted successfully!");
    }
}
