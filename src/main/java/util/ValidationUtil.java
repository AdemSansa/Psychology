package util;

import javafx.scene.control.Alert;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "^\\+?[0-9]{8,15}$";

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && Pattern.matches(EMAIL_REGEX, email);
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.matches(PHONE_REGEX, phone);
    }

    public static boolean isPositiveInteger(String str) {
        try {
            int val = Integer.parseInt(str);
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFutureDate(LocalDate date) {
        return date != null && (date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now()));
    }

    public static boolean isAfter(LocalDateTime end, LocalDateTime start) {
        return end != null && start != null && end.isAfter(start);
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
