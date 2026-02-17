package Controllers.Welcome;

import Service.AuthService;
import Entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import util.SceneManager;
import util.Session;

import java.text.BreakIterator;

public class HomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    private javafx.scene.control.Label welcomeLabel;

    @FXML
    private javafx.scene.control.Button btnDashboard;
    @FXML
    private javafx.scene.control.Button btnUsers;
    @FXML
    private javafx.scene.control.Button btnTherapists;
    @FXML
    private javafx.scene.control.Button btnEvents;
    @FXML
    private javafx.scene.control.Button btnAppointments;
    @FXML
    private javafx.scene.control.Button btnForum;
    @FXML
    private javafx.scene.control.Button btnQuestions;
    @FXML
    private javafx.scene.control.Button btnQuizzes;
    @FXML
    private javafx.scene.control.Button btnQuizAssessment;

    @FXML
    public void initialize() {

        User user = Session.getInstance().getUser();
        if (user != null) {
            System.out.println("Welcome " + user.getFullName() + " (" + user.getRole() + ")");
            updateSidebarVisibility(user.getRole());

            // Update welcome label
            String displayName = user.getFullName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = user.getRole();
            }
            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue, " + displayName);
            }

        } else {
            System.out.println("No user logged in session.");
            // Handle case where no user is logged in if necessary, maybe redirect to login
            // or show defaults
        }

        // Tell SceneManager where pages should load
        SceneManager.setContentArea(contentArea);
        // Default page could also depend on role, but sticking to dashboard for now
        SceneManager.loadPage("/com/example/psy/intro/dashboard.fxml");
    }

    private void updateSidebarVisibility(String role) {
        // Default: hide everything or set a baseline
        setButtonVisible(btnDashboard, true); // Everyone sees dashboard?
        setButtonVisible(btnUsers, false);
        setButtonVisible(btnTherapists, false);
        setButtonVisible(btnEvents, false);
        setButtonVisible(btnAppointments, false);
        setButtonVisible(btnForum, false);
        setButtonVisible(btnQuestions, false);
        setButtonVisible(btnQuizzes, false);
        setButtonVisible(btnQuizAssessment, false);

        if (role == null)
            return;

        switch (role.toLowerCase()) {
            case "admin":
                setButtonVisible(btnUsers, true);
                setButtonVisible(btnTherapists, true); // Admin manages therapists
                setButtonVisible(btnEvents, true);
                setButtonVisible(btnForum, true); // Admin moderates forum
                setButtonVisible(btnQuestions, true);
                setButtonVisible(btnQuizzes, true);
                break;
            case "patient":
                setButtonVisible(btnTherapists, true); // Patient views therapists
                setButtonVisible(btnAppointments, true);
                setButtonVisible(btnForum, true);
                setButtonVisible(btnQuizAssessment, true);
                break;
            case "therapist":
                setButtonVisible(btnAppointments, true);
                setButtonVisible(btnForum, true);
                setButtonVisible(btnEvents, true);
                setButtonVisible(btnTherapists, true);
                break;
            default:
                // Unknown role, minimal access
                break;
        }
    }

    private void setButtonVisible(javafx.scene.control.Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    @FXML
    public void handleLogout() {
        AuthService.getInstance().logout();
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
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

    @FXML

    public void gotoQuestions() {
        SceneManager.loadPage("/com/example/psy/Question/Question.fxml");
    }

    @FXML
    public void gotoReview() {
        SceneManager.loadPage("/com/example/psy/forum/forum.fxml");
    }

    public void gotoQuiz() {
        SceneManager.loadPage("/com/example/psy/Quiz/Quiz.fxml");
    }

    public void gotoQuizAssesment() {
        SceneManager.loadPage("/com/example/psy/QuizAssesment/quizList.fxml");
    }

    public void logout() {
        Session.getInstance().clear();
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }

}
