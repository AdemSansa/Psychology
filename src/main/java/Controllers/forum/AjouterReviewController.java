package Controllers.forum;

import Entities.Review;
import Service.ReviewService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class AjouterReviewController {

    @FXML
    private TextArea demandeTextArea;

    private final ReviewService reviewService = new ReviewService();

    @FXML
    private void envoyerReview() {

        String contenu = demandeTextArea.getText();

        if (contenu == null || contenu.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez écrire votre message.");
            alert.show();
            return;
        }

        try {
            // ⚠ Remplace 1 par l'id réel du user connecté
            int idUserConnecte = 1;

            Review review = new Review(contenu, idUserConnecte);

            reviewService.create(review);   //  ICI tu utilises ton service

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Message envoyé avec succès !");
            alert.show();

            demandeTextArea.clear();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de l'enregistrement !");
            alert.show();
        }
    }
}
