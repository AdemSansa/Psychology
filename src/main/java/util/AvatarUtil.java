package util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.io.File;

public class AvatarUtil {

    private static final Color[] BACKGROUND_COLORS = {
            Color.web("#6f4e37"), Color.web("#d2b48c"), Color.web("#5d4037"),
            Color.web("#8d6e63"), Color.web("#a1887f"), Color.web("#4e342e"),
            Color.web("#795548"), Color.web("#3e2723")
    };

    public static String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            initials.append(firstName.trim().toUpperCase().charAt(0));
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            initials.append(lastName.trim().toUpperCase().charAt(0));
        }
        return initials.length() > 0 ? initials.toString() : "?";
    }

    private static Color getColorForName(String name) {
        if (name == null || name.isEmpty())
            return BACKGROUND_COLORS[0];
        int hash = name.hashCode();
        int index = Math.abs(hash) % BACKGROUND_COLORS.length;
        return BACKGROUND_COLORS[index];
    }

    public static void setAvatar(StackPane pane, Label label, String firstName, String lastName, String photoUrl) {
        pane.getChildren().clear();

        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            try {
                Image img;
                if (photoUrl.startsWith("http") || photoUrl.startsWith("file:")) {
                    img = new Image(photoUrl, true);
                } else {
                    File file = new File(photoUrl);
                    if (file.exists()) {
                        img = new Image(file.toURI().toString(), true);
                    } else {
                        // Fallback to initials if file not found
                        showInitials(pane, label, firstName, lastName);
                        return;
                    }
                }

                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(pane.getPrefWidth() > 0 ? pane.getPrefWidth() : 80);
                imageView.setFitHeight(pane.getPrefHeight() > 0 ? pane.getPrefHeight() : 80);
                imageView.setPreserveRatio(true);

                // Circular clipping
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle();
                clip.setCenterX(imageView.getFitWidth() / 2);
                clip.setCenterY(imageView.getFitHeight() / 2);
                clip.setRadius(Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);
                imageView.setClip(clip);

                pane.getChildren().add(imageView);
                pane.setStyle("-fx-background-color: transparent; -fx-background-radius: 50%;");
                return;
            } catch (Exception e) {
                System.err.println("Error loading avatar image: " + e.getMessage());
            }
        }

        showInitials(pane, label, firstName, lastName);
    }

    // Keep original signature for compatibility if needed, but updated logic
    public static void setAvatar(StackPane pane, Label label, String firstName, String lastName) {
        setAvatar(pane, label, firstName, lastName, null);
    }

    private static void showInitials(StackPane pane, Label label, String firstName, String lastName) {
        String initials = getInitials(firstName, lastName);
        String fullName = (firstName != null ? firstName : "") + (lastName != null ? lastName : "");
        Color baseColor = getColorForName(fullName);

        pane.getChildren().add(label);
        pane.setStyle("-fx-background-color: " + toRGBCode(baseColor) + "; " +
                "-fx-background-radius: 50%; " +
                "-fx-border-color: rgba(255,255,255,0.3); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 50%; " +
                "-fx-alignment: center;");

        label.setText(initials);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
    }

    private static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
