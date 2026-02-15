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

public class QuestionEditController implements Initializable {

    @FXML
    private TextArea txtQuestionText;


    @FXML
    private CheckBox chkRequired;

    @FXML
    private TextField txtImagePath;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnCancel;

    private final QuestionService questionService = new QuestionService();
    private Question currentQuestion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Default to required
        chkRequired.setSelected(true);
    }

    public void setQuestion(Question question) {
        this.currentQuestion = question;
        if (question != null) {
            txtQuestionText.setText(question.getQuestionText());
            chkRequired.setSelected(question.isRequired());
            txtImagePath.setText(question.getImagePath() != null ? question.getImagePath() : "");
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        try {
            if (currentQuestion == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No question selected for editing.");
                return;
            }

            String questionText = txtQuestionText.getText();
            boolean required = chkRequired.isSelected();
            String imagePath = txtImagePath.getText();

            if (questionText == null || questionText.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Question text cannot be empty.");
                return;
            }

            currentQuestion.setQuestionText(questionText);
            currentQuestion.setRequired(required);
            currentQuestion.setImagePath(imagePath);

            questionService.update(currentQuestion);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Question updated successfully!");
            // Close window
            // Close window
            txtQuestionText.getScene().getWindow().hide();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numbers for Quiz ID and Order Index.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update question: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Close the dialog/window
        txtQuestionText.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
