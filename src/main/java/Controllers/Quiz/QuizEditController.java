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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuizEditController implements Initializable {

    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<QuizCategory> categoryComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckBox activeCheckBox;
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

    private Quiz currentQuiz;
    private ObservableList<Question> questionsList = FXCollections.observableArrayList();
    private List<Question> questionsToDelete = new ArrayList<>();

    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        populateFields();
        loadQuestions();
    }

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
        questionsTable.setItems(questionsList);
    }

    private void populateFields() {
        if (currentQuiz == null)
            return;
        titleField.setText(currentQuiz.getTitle());
        descriptionArea.setText(currentQuiz.getDescription());
        activeCheckBox.setSelected(currentQuiz.isActive());

        if (currentQuiz.getCategory() != null) {
            try {
                categoryComboBox.setValue(QuizCategory.valueOf(currentQuiz.getCategory()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid category
            }
        }
        minScoreSpinner.getValueFactory().setValue(currentQuiz.getMinScore());
        maxScoreSpinner.getValueFactory().setValue(currentQuiz.getMaxScore());
    }

    private void loadQuestions() {
        if (currentQuiz == null)
            return;
        try {
            if (currentQuiz.getId() != null) {
                List<Question> qs = questionService.getQuestionsByQuizId(currentQuiz.getId().intValue());
                questionsList.setAll(qs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load questions: " + e.getMessage());
        }
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
                            if (question.getId() != null) {
                                questionsToDelete.add(question);
                            }
                            questionsList.remove(question);
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

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

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
            if (question != null && !questionsList.contains(question)) {
                questionsList.add(question);
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
            currentQuiz.setTitle(titleField.getText());
            currentQuiz.setDescription(descriptionArea.getText());
            currentQuiz.setCategory(categoryComboBox.getValue().toString());
            currentQuiz.setActive(activeCheckBox.isSelected());
            currentQuiz.setMinScore(minScoreSpinner.getValue());
            currentQuiz.setMaxScore(maxScoreSpinner.getValue());
            currentQuiz.setTotalQuestions(questionsList.size()); // Update count

            quizService.update(currentQuiz);

            // Remove questions from quiz (delete from junction table)
            for (Question q : questionsToDelete) {
                if (q.getId() != null && currentQuiz.getId() != null) {
                    quizService.removeQuestionFromQuiz(currentQuiz.getId().intValue(), q.getId().intValue());
                }
            }

            // Add new questions to quiz (insert into junction table)
            for (Question q : questionsList) {
                if (q.getId() != null && currentQuiz.getId() != null) {
                    quizService.addQuestionToQuiz(currentQuiz.getId().intValue(), q.getId().intValue());
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Quiz updated successfully!");
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update quiz: " + e.getMessage());
        }
    }

    @FXML
    void handleDeleteQuiz(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Quiz");
        alert.setHeaderText("Are you sure you want to delete this quiz?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (currentQuiz.getId() != null) {
                    quizService.delete(currentQuiz.getId().intValue());
                    closeWindow();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete quiz: " + e.getMessage());
            }
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
