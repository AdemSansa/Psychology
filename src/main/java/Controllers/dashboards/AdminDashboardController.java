package Controllers.dashboards;

import Entities.User;
import Service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import util.AvatarUtil;
import util.SceneManager;
import util.Session;

public class AdminDashboardController {

    @FXML
    private StackPane avatarPane;

    @FXML
    private Label initialsLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        User user = Session.getInstance().getUser();
        if (user != null) {
            welcomeLabel.setText("Admin: " + user.getFirstName());
            AvatarUtil.setAvatar(avatarPane, initialsLabel, user.getFirstName(), user.getLastName(),
                    user.getPhotoUrl());
        }
    }

    @FXML
    public void handleLogout() {
        AuthService.getInstance().logout();
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }
}
