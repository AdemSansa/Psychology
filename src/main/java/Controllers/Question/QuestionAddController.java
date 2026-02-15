package Controllers.Question;

import Entities.Question;
import Service.QuestionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QuestionAddController implements Initializable {

    @FXML
    private TextArea txtQuestionText;

    @FXML
    private CheckBox chkRequired;

    @FXML
    private TextField txtImagePath;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnCancel;

    private final QuestionService questionService = new QuestionService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Default to required
        chkRequired.setSelected(true);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            String questionText = txtQuestionText.getText();
            int orderIndex = 0; // Defaulting to 0 for now as requested
            boolean required = chkRequired.isSelected();
            String imagePath = txtImagePath.getText();

            if (questionText == null || questionText.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Question text cannot be empty.");
                return;
            }

            Question q = new Question(imagePath, questionText, orderIndex, required);
            questionService.create(q);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Question added successfully!");
            // Close window or clear fields
            txtQuestionText.clear();
            txtImagePath.clear();
            chkRequired.setSelected(true);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please check your input.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add question: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Close the dialog/window
        btnCancel.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
