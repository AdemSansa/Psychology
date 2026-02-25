package Service;

import java.awt.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

public class VideoCallService {

    public static String generateMeetingLink(int appointmentId) {
        String roomId = "session-" + appointmentId;
        return "https://meet.jit.si/" + roomId;
    }

    public static boolean isTimeForAppointment(LocalDateTime appointmentTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(appointmentTime.minusMinutes(1)) && !now.isAfter(appointmentTime.plusMinutes(1));
    }

    // ✅ Open the meeting in the default system browser
    public static void openMeetingInBrowser(String meetingLink) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(meetingLink));
            } else {
                System.out.println("Cannot open browser on this system.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}