package util;

import Entities.Therapistis;
import Entities.User;
import Service.TherapistService;

import java.sql.SQLException;

public class Session {

    private static Session instance;

    private User currentUser;        // pour les patients/admin
   // private Therapistis currentTherapist; // pour les thérapeutes

    private Session() {
        // constructeur privé
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // ====== Patient / Admin ======
    public void setUser(User user) {
        this.currentUser = user;
    }

    public User getUser() {
        return currentUser;
    }

    // ====== Thérapeute ======
   /* public void setTherapist(Therapistis therapist) {
        this.currentTherapist = therapist;
    }

    public Therapistis getTherapist() {
        return currentTherapist;
    }*/

    // ====== Clear session ======
    public void clear() {
        currentUser = null;
      //  currentTherapist = null;
    }





























































    public Integer getConnectedTherapistId() {
        if (currentUser == null) return null;

        TherapistService therapistService = new TherapistService();
        try {
            Therapistis therapist = therapistService.readByEmail(currentUser.getEmail());
            if (therapist != null) {
                return therapist.getId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
