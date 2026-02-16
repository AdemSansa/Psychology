package util;

import Entities.Therapistis;
import Entities.User;

public class SessionManager {
    private static User currentUser;
    private static Therapistis currentTherapist;
    private static String currentRole; // admin, patient, therapist

    public static void setUserSession(User user) {
        currentUser = user;
        currentTherapist = null;
        currentRole = user.getRole();
    }

    public static void setTherapistSession(Therapistis therapist) {
        currentTherapist = therapist;
        currentUser = null;
        currentRole = "therapist";
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Therapistis getCurrentTherapist() {
        return currentTherapist;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static void logout() {
        currentUser = null;
        currentTherapist = null;
        currentRole = null;
    }

    public static boolean isLoggedIn() {
        return currentRole != null;
    }

    public static boolean isAdmin() {
        return "admin".equals(currentRole);
    }
}
