package Service;

import java.time.LocalDateTime;
import java.util.UUID;

public class VideoCallService {

    public static String generateMeetingLink(int appointmentId) {
        String roomId = "session-" + appointmentId + "-" + UUID.randomUUID().toString().substring(0, 8);
        return "https://meet.jit.si/" + roomId;
    }

    // Check if appointment time is now
    public static boolean isTimeForAppointment(LocalDateTime appointmentTime) {
        LocalDateTime now = LocalDateTime.now();
        // Allow 1 min window before & after start
        return !now.isBefore(appointmentTime.minusMinutes(1)) && !now.isAfter(appointmentTime.plusMinutes(1));
    }
}