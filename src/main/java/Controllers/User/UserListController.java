package Controllers.User;

import Entities.User;
import Service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

    @FXML
    private TextField searchField;

    private final ObservableList<User> users = FXCollections.observableArrayList();

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Lier les colonnes aux getters de User
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName")); // fullName = firstName + " " + lastName
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Charger les utilisateurs
        loadUsers();

        // Permet de lancer la recherche en appuyant sur Enter
        searchField.setOnAction(e -> rechercherUser());
    }

    private void loadUsers() {
        try {
            List<User> list = userService.list();
            users.setAll(list);
            userTable.setItems(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------- Méthode de recherche -----------------------
    @FXML
    private void rechercherUser() {
        String motCle = searchField.getText().trim();
        try {
            List<User> resultats;
            if (!motCle.isEmpty()) {
                resultats = userService.rechercher(motCle);
            } else {
                resultats = userService.list();
            }
            users.setAll(resultats);
            userTable.setItems(users);
            System.out.println("Résultat recherche : " + resultats.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------- Ajouter un utilisateur -----------------------
    public void GoToUserAdd() {
        SceneManager.switchScene("/com/example/psy/User/userAdd.fxml");
    }

    // ----------------------- Modifier un utilisateur -----------------------
    public void GoToUserEdit() {

        User selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Avertissement",
                    "Veuillez sélectionner un utilisateur.");
            return;
        }

        UserEditController controller =
                SceneManager.switchSceneWithController(
                        "/com/example/psy/User/userEdit.fxml"
                );

        controller.setUser(selected); // envoie des données
    }

    // ----------------------- Supprimer un utilisateur -----------------------
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Avertissement",
                    "Veuillez sélectionner un utilisateur.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setContentText("Supprimer " + selected.getFullName() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(selected.getId());
                    users.remove(selected);
                    showAlert(Alert.AlertType.INFORMATION,
                            "Succès",
                            "Utilisateur supprimé.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ----------------------- Retour -----------------------
    @FXML
    private void GoBack() {
        SceneManager.switchScene("/com/example/psy/intro/home.fxml");
    }

    // ----------------------- Alertes -----------------------
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
