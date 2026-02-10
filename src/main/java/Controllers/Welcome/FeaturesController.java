package Controllers.Welcome;

import javafx.fxml.FXML;
import util.SceneManager;

public class FeaturesController {


    @FXML
    public void GotoWellbeingPrompt() {
        SceneManager.switchScene("/com/example/psy/intro/wellbeing_prompt.fxml");
    }
    @FXML
    public void GotoHome() {
        SceneManager.switchScene("/com/example/psy/intro/home.fxml");
    }
}
