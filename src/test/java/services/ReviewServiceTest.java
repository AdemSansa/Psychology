package services;
import Entities.Review;
import Service.ReviewService;
import org.junit.Before;
import  org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static net.fortuna.ical4j.model.LinkRelationType.service;
import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewServiceTest {
    static ReviewService reviewService;
    int idReview;
    int idUser=8;
    @BeforeAll
    static void setUp()
    {
        reviewService = new ReviewService();
    }
    @Test
    @Order(1)
    void testAddReview() throws SQLException {

        Review r  = new Review("jai bien appricié le site", idUser );
        reviewService.create(r);
        List<Review> reviews = reviewService.list();
        assertFalse(reviews.isEmpty());
        assertTrue(
                reviews.stream().anyMatch(rev->
                        rev.getContent().equals("jai bien appricié le site")
                )
        );
        idReview= reviews.getLast().getIdReview();

    }
    @Test
    @Order(2)
    void testUpdateReview() throws SQLException {
        Review r = new Review();
        r.setIdReview(idReview);
        r.setIdUser(idUser);
        r.setContent("jai bien modifié le content");
        reviewService.update(r);
        List<Review> reviews = reviewService.list();
        Review reviewUpdated = reviews.stream()
                .filter(rev -> rev.getIdReview()== idReview)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Review non trouvée"));
    assertEquals("jai bien modifié le content",reviewUpdated.getContent());
    }

    @Test
    @Order(3)
    void testDeleteReview() throws SQLException {
        reviewService.delete(idReview);
        List<Review> reviews = reviewService.list();
        boolean existe = reviews.stream().anyMatch(r -> r.getIdReview() == idReview);
        assertFalse(existe);
    }
    @AfterEach
    void cleanUp() throws SQLException {
        List<Review> reviews = reviewService.list();
        if (!reviews.isEmpty()) {
            Review last = reviews.get(reviews.size() - 1);
            reviewService.delete(last.getIdReview());
        }
    }


}
