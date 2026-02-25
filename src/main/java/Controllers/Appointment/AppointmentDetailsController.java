package Controllers.Appointment;

import Entities.Appointment;
import Entities.Note;
import Service.AppointmentService;
import Service.NoteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AppointmentDetailsController {

    @FXML
    private Label dateLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private Button confirmBtn;
    @FXML
    private Button cancelBtn;

    @FXML
    private VBox notesContainer;
    @FXML
    private Button addNoteBtn;
    @FXML
    private Button aiSummaryBtn;

    private Appointment appointment;
    private final AppointmentService appointmentService = new AppointmentService();
    private final NoteService noteService = new NoteService();
    private AppointmentCalendarController calendarController;

    public void setCalendarController(AppointmentCalendarController controller) {
        this.calendarController = controller;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;

        dateLabel.setText("Date: " + appointment.getAppointmentDate());
        timeLabel.setText("Time: " + appointment.getStartTime() + " - " + appointment.getEndTime());

        boolean isTherapist = Session.getInstance().getUser() != null &&
                "therapist".equalsIgnoreCase(Session.getInstance().getUser().getRole());

        String status = appointment.getStatus();

        if (isTherapist) {
            if ("pending".equalsIgnoreCase(status)) {
                confirmBtn.setVisible(true);
                confirmBtn.setManaged(true);
                statusLabel.setText("Status: Pending");
            } else if ("completed".equalsIgnoreCase(status)) {
                confirmBtn.setVisible(false);
                confirmBtn.setManaged(false);
                cancelBtn.setVisible(false);
                cancelBtn.setManaged(false);
                statusLabel.setText("Status: Completed");
            } else {
                confirmBtn.setVisible(false);
                confirmBtn.setManaged(false);
                statusLabel.setText("Status: " + status);
            }
        } else {
            confirmBtn.setVisible(false);
            confirmBtn.setManaged(false);

            if ("pending".equalsIgnoreCase(status)) {
                statusLabel.setText("Status: Appointment is still pending");
            } else if ("confirmed".equalsIgnoreCase(status)) {
                statusLabel.setText("Status: Appointment confirmed");
            } else if ("completed".equalsIgnoreCase(status)) {
                statusLabel.setText("Status: Completed");
            } else {
                statusLabel.setText("Status: " + status);
            }
        }

        boolean canAddNote = isTherapist && !"pending".equalsIgnoreCase(status);
        addNoteBtn.setVisible(canAddNote);
        addNoteBtn.setManaged(canAddNote);

        loadNotes();
    }

    @FXML
    private void generateAISummary() {
        try {
            var notes = noteService.listByAppointment(appointment.getId());
            if (notes.isEmpty()) {
                showAlert("No notes available", "There are no notes to summarize for this session.",
                        Alert.AlertType.INFORMATION);
                return;
            }

            // Combine all notes into one for the AI to summarize
            StringBuilder allNotesData = new StringBuilder();
            for (Note n : notes) {
                allNotesData.append("Note: ").append(n.getContent()).append(" (Mood: ").append(n.getMood())
                        .append(")\n");
            }

            Alert thinkingAlert = new Alert(Alert.AlertType.INFORMATION);
            thinkingAlert.setTitle("Generating AI Summary");
            thinkingAlert.setHeaderText("Connecting to AI...");
            thinkingAlert.setContentText("Please wait while the session notes are being summarized.");
            thinkingAlert.show();

            // Run API call in a background thread to keep UI responsive
            new Thread(() -> {
                Service.AISummaryService summaryService = new Service.AISummaryService();
                String summary = summaryService.generateSummary(allNotesData.toString());

                javafx.application.Platform.runLater(() -> {
                    thinkingAlert.close();

                    Dialog<String> summaryDialog = new Dialog<>();
                    summaryDialog.setTitle("✨ AI Session Summary");

                    TextArea textArea = new TextArea(summary);
                    textArea.setWrapText(true);
                    textArea.setEditable(false);
                    textArea.setPrefSize(400, 300);

                    summaryDialog.getDialogPane().setContent(textArea);
                    summaryDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                    summaryDialog.showAndWait();
                });
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load notes for summary.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void confirmAppointment() {
        try {
            appointment.setStatus("confirmed");
            appointmentService.update(appointment);

            if (calendarController != null)
                calendarController.refreshCalendar();

            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelAppointment() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Are you sure you want to cancel this appointment?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                appointmentService.delete(appointment.getId());

                if (calendarController != null)
                    calendarController.refreshCalendar();

                closeWindow();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to cancel the appointment");
                errorAlert.setContentText(e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    @FXML
    private void addNote() {
        if (!"therapist".equalsIgnoreCase(Session.getInstance().getUser().getRole()))
            return;

        if ("pending".equalsIgnoreCase(appointment.getStatus()))
            return;

        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle("Add Note");

        TextField contentField = new TextField();
        contentField.setPromptText("Content");
        TextField moodField = new TextField();
        moodField.setPromptText("Mood");

        VBox box = new VBox(10, new Label("Content:"), contentField, new Label("Mood:"), moodField);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Note(contentField.getText(), moodField.getText(),
                        appointment.getId(), Session.getInstance().getConnectedTherapistId());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(note -> {
            try {
                noteService.create(note);
                appointment.setStatus("completed");
                appointmentService.update(appointment);
                cancelBtn.setVisible(false);
                cancelBtn.setManaged(false);
                confirmBtn.setVisible(false);
                confirmBtn.setManaged(false);
                statusLabel.setText("Status: Completed");
                loadNotes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadNotes() {
        notesContainer.getChildren().clear();
        boolean isTherapist = Session.getInstance().getUser() != null &&
                "therapist".equalsIgnoreCase(Session.getInstance().getUser().getRole());

        try {
            var notes = noteService.listByAppointment(appointment.getId());

            for (Note note : notes) {
                HBox noteBox = new HBox(10);
                Label contentLabel = new Label(note.getContent() + " (Mood: " + note.getMood() + ")");
                contentLabel.setWrapText(true);

                noteBox.getChildren().add(contentLabel);

                if (isTherapist) {
                    Button editBtn = new Button("Edit");
                    editBtn.setOnAction(e -> editNoteDialog(note));
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setOnAction(e -> deleteNoteConfirm(note));
                    noteBox.getChildren().addAll(editBtn, deleteBtn);
                }

                notesContainer.getChildren().add(noteBox);
            }

            if (!notes.isEmpty()) {
                cancelBtn.setVisible(false);
                cancelBtn.setManaged(false);
                statusLabel.setText("Status: Completed");
                confirmBtn.setVisible(false);
                confirmBtn.setManaged(false);
                appointment.setStatus("completed");
                appointmentService.update(appointment);

                if (isTherapist) {
                    aiSummaryBtn.setVisible(true);
                    aiSummaryBtn.setManaged(true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editNoteDialog(Note note) {
        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle("Edit Note");

        TextField contentField = new TextField(note.getContent());
        TextField moodField = new TextField(note.getMood());

        VBox box = new VBox(10, new Label("Content:"), contentField, new Label("Mood:"), moodField);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                note.setContent(contentField.getText());
                note.setMood(moodField.getText());
                return note;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(n -> {
            try {
                noteService.update(n);
                loadNotes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteNoteConfirm(Note note) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this note?");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    noteService.delete(note.getId());
                    loadNotes();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }

    public static AppointmentDetailsController openDetails(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AppointmentDetailsController.class.getResource(
                            "/com/example/psy/Appointment/AppointmentDetails.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            AppointmentDetailsController controller = loader.getController();
            controller.setAppointment(appointment);

            stage.setTitle("Appointment Details");
            stage.show();

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
