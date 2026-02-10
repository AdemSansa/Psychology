package Controllers.User;

import Entities.User;
import Service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import util.SceneManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    private final ObservableList<User> users = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // configure columns
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        if (emailColumn != null) emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (roleColumn != null) roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // load users from DAO
        try {
            UserService dao = new UserService();
            List<User> list = dao.list();
            if (list != null) {
                users.setAll(list);
            }
            System.out.println("Loaded " + users.size() + " users from database.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userTable != null) userTable.setItems(users);
    }

    public void GoToUserAdd() {
        SceneManager.switchScene("/com/example/psy/User/userAdd.fxml");
    }
    public void GoToUserEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        UserEditController controller =
                SceneManager.switchSceneWithController(
                        "/com/example/psy/User/userEdit.fxml"
                );

        controller.setUser(selected);
    }
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner un utilisateur.");
            return;
        }

        // 1️⃣ Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + selected.getFullName() + " ?");

        // Show and wait for user response
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // 2️⃣ Delete from DB
                    UserService dao = new UserService();
                    dao.delete(selected.getId());

                    // 3️⃣ Remove from observable list (updates TableView)
                    users.remove(selected);

                    // Optional success alert
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur : " + e.getMessage());
                }
            }
        });
    }

    // Utility method for showing alerts
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



}
