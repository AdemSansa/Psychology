package Controllers.Welcome;

import javafx.fxml.FXML;
import util.SceneManager;

public class HomeController {


    @FXML
    public void gotoFeatures() {
        SceneManager.switchScene("/com/example/psy/intro/features.fxml");
    }

    @FXML
    public void gotoUsers() {
        SceneManager.switchScene("/com/example/psy/User/users.fxml");
    }

    @FXML
    public void gotoEvents() {
        SceneManager.switchScene("/com/example/psy/Event/events.fxml");
    }

    @FXML
    public void gotoTherapists() { SceneManager.switchScene("/com/example/psy/Therapist/therapist_crud.fxml"); }
}
