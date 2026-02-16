package Controllers.Question;

import Entities.Question;
import Service.QuestionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.SceneManager;

import java.io.IOException;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuestionListController implements Initializable {

    @FXML
    private TableView<Question> table;

    @FXML
    private TableColumn<Question, Long> idColumn;

    @FXML
    private TableColumn<Question, String> questionTextColumn;



    @FXML
    private TableColumn<Question, Boolean> requiredColumn;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private final QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionTextColumn.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        requiredColumn.setCellValueFactory(new PropertyValueFactory<>("required"));
    }

    private void loadData() {
        try {
            questionList.setAll(questionService.list());
            table.setItems(questionList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load questions: " + e.getMessage());
        }
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/psy/Question/QuestionAdd.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Question");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Add Question window: " + e.getMessage());
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        Question selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a question to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/psy/Question/QuestionEdit.fxml"));
            Parent root = loader.load();

            QuestionEditController controller = loader.getController();
            controller.setQuestion(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Question");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Edit Question window: " + e.getMessage());
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Question selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a question to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Question");
        alert.setHeaderText("Are you sure you want to delete this question?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (selected.getId() != null) {
                    questionService.delete(selected.getId().intValue()); // Casting because service uses int
                    questionList.remove(selected);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete a question without an ID.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete question: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void goBack() {
        SceneManager.switchScene( "/com/example/psy/intro/Home.fxml");
    }

}
