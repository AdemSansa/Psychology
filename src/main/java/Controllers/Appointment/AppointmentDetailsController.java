package Controllers.Appointment;



import Entities.Appointment;
import Service.AppointmentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AppointmentDetailsController {

    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private Label statusLabel;

    private Appointment appointment;
    private AppointmentService service = new AppointmentService();

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;

        dateLabel.setText("Date: " + appointment.getAppointmentDate());
        timeLabel.setText("Time: " + appointment.getStartTime() + " - " + appointment.getEndTime());
        statusLabel.setText(appointment.getStatus());
    }

    @FXML
    private void cancelAppointment() {
        try {
            appointment.setStatus("cancelled");
            service.update(appointment);
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) dateLabel.getScene().getWindow();
        stage.close();
    }

    public static void openDetails(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AppointmentDetailsController.class.getResource("/com/example/psy/Appointment/AppointmentDetails.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            AppointmentDetailsController controller = loader.getController();
            controller.setAppointment(appointment);

            stage.setTitle("Appointment Details");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
