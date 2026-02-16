package Controllers.dashboards;

import Service.AuthService;
import javafx.fxml.FXML;
import util.SceneManager;

public class AdminDashboardController {

    @FXML
    public void handleLogout() {
        AuthService.getInstance().logout();
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }
}
