package Controllers.Appointment;

import Entities.Appointment;
import Service.AppointmentService;
import util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.util.List;

public class PastAppointmentsController {

    @FXML
    private ListView<Appointment> pastAppointmentsListView;

    private AppointmentService appointmentService;

    public PastAppointmentsController() {
        this.appointmentService = new AppointmentService();
    }

    @FXML
    public void initialize() {
        if (Session.getInstance().getUser() == null ||
                !"patient".equalsIgnoreCase(Session.getInstance().getUser().getRole())) {
            return;
        }

        loadPatientCompletedAppointments();
    }

    private void loadPatientCompletedAppointments() {
        // Since listByPatient doesn't exist, we must add it to AppointmentService
        try {
            int patientId = Session.getInstance().getUser().getId();
            List<Appointment> list = appointmentService.listByPatient(patientId);

            ObservableList<Appointment> completed = FXCollections.observableArrayList();
            for (Appointment a : list) {
                if ("completed".equalsIgnoreCase(a.getStatus())) {
                    completed.add(a);
                }
            }

            pastAppointmentsListView.setItems(completed);
            pastAppointmentsListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Appointment item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox box = new VBox(5);
                        Label dateLabel = new Label("Date: " + item.getAppointmentDate() + " | Time: "
                                + item.getStartTime() + " - " + item.getEndTime());
                        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3e2c23;");
                        Label typeLabel = new Label("Type: " + item.getType());
                        Label statusLabel = new Label("Status: " + item.getStatus());

                        box.getChildren().addAll(dateLabel, typeLabel, statusLabel);
                        setGraphic(box);
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
