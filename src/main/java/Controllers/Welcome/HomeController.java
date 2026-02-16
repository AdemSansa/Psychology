package Controllers.Welcome;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import util.SceneManager;

public class HomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {

        // Tell SceneManager where pages should load
        SceneManager.setContentArea(contentArea);

        // Default page
        SceneManager.loadPage("/com/example/psy/intro/dashboard.fxml");
    }

    @FXML
    public void gotoUsers() {
        SceneManager.loadPage("/com/example/psy/User/users.fxml");
    }

    @FXML
    public void gotoEvents() {
        SceneManager.loadPage("/com/example/psy/Event/events.fxml");
    }

    @FXML
    public void gotoTherapists() {
        SceneManager.loadPage("/com/example/psy/Therapist/therapist_crud.fxml");
    }

    @FXML
    public void gotoAppoitnments() {
        SceneManager.loadPage("/com/example/psy/Appointment/AppointmentCalendar.fxml");
    }
}