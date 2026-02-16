package Controllers.Quiz;

import Entities.Question;
import Entities.Quiz;
import Entities.QuizCategory;
import Service.QuestionService;
import Service.QuizService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuizAddController implements Initializable {

    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<QuizCategory> categoryComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Spinner<Integer> minScoreSpinner;
    @FXML
    private Spinner<Integer> maxScoreSpinner;

    @FXML
    private TableView<Question> questionsTable;
    @FXML
    private TableColumn<Question, String> colQuestionText;
    @FXML
    private TableColumn<Question, Boolean> colQuestionRequired;
    @FXML
    private TableColumn<Question, Void> colQuestionActions;

    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();

    // List to hold questions before they are saved to DB (linked to Quiz)
    private ObservableList<Question> pendingQuestions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupForm();
        setupTable();
    }

    private void setupForm() {
        categoryComboBox.setItems(FXCollections.observableArrayList(QuizCategory.values()));

        minScoreSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        maxScoreSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 10));
    }

    private void setupTable() {
        colQuestionText.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        colQuestionRequired.setCellValueFactory(new PropertyValueFactory<>("required"));

        addButtonToTable();
        questionsTable.setItems(pendingQuestions);
    }

    private void addButtonToTable() {
        Callback<TableColumn<Question, Void>, TableCell<Question, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Question, Void> call(final TableColumn<Question, Void> param) {
                return new TableCell<>() {
                    private final Button btnRemove = new Button("Remove");
                    private final HBox pane = new HBox(btnRemove);

                    {
                        btnRemove.getStyleClass().add("btn-danger");
                        btnRemove.setOnAction((ActionEvent event) -> {
                            Question question = getTableView().getItems().get(getIndex());
                            pendingQuestions.remove(question);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        colQuestionActions.setCellFactory(cellFactory);
    }

    @FXML
    void handleAddQuestionDialog(ActionEvent event) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("Select Question");
        dialog.setHeaderText("Choose a question to add");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Layout
        TableView<Question> table = new TableView<>();
        TableColumn<Question, String> textCol = new TableColumn<>("Question");
        textCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        table.getColumns().add(textCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            table.getItems().setAll(questionService.getAllQuestions());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load questions: " + e.getMessage());
            return;
        }

        dialog.getDialogPane().setContent(table);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return table.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            if (question != null && !pendingQuestions.contains(question)) {
                pendingQuestions.add(question);
            }
        });
    }

    @FXML
    void handleSaveQuiz(ActionEvent event) {
        if (titleField.getText().isEmpty() || categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title and Category are required.");
            return;
        }

        try {
            Quiz quiz = new Quiz();
            quiz.setTitle(titleField.getText());
            quiz.setDescription(descriptionArea.getText());
            quiz.setCategory(categoryComboBox.getValue().toString()); // Enum string value
            quiz.setMinScore(minScoreSpinner.getValue());
            quiz.setMaxScore(maxScoreSpinner.getValue());
            quiz.setTotalQuestions(pendingQuestions.size());
            quiz.setActive(true);

            // Save Quiz and get ID
            int quizId = quizService.createAndReturnId(quiz);

            if (quizId != -1) {
                // Save Questions (Add to junction table)
                for (Question q : pendingQuestions) {
                    if (q.getId() != null) {
                        quizService.addQuestionToQuiz(quizId, q.getId().intValue());
                    }
                }
                // No update needed on question entity itself as it is many-to-many

                showAlert(Alert.AlertType.INFORMATION, "Success", "Quiz created successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create quiz.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save quiz: " + e.getMessage());
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
