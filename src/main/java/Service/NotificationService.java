package Service;

import Entities.User;
import jakarta.mail.MessagingException;
import util.VerificationCodeUtil;
import java.io.UnsupportedEncodingException;

/**
 * Façade unifiée pour l'envoi de notifications Email.
 *
 * Utilise EmailService (SMTP Gmail).
 *
 * Cas d'usage :
 * - Code de vérification (Email)
 * - Confirmation d'inscription (Email)
 * - Réinitialisation de mot de passe (Email)
 * - Notifications générales (Email)
 */
public class NotificationService {

    private static NotificationService instance;

    private final EmailService emailService;

    private NotificationService() {
        this.emailService = EmailService.getInstance();
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────
    // CODE DE VÉRIFICATION (OTP)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Génère et envoie un code OTP à l'utilisateur par Email.
     * Le code est stocké automatiquement pour vérification ultérieure.
     *
     * @param user l'utilisateur destinataire
     * @return le code OTP généré
     */
    public String sendVerificationCode(User user) {
        String code = VerificationCodeUtil.generateCode();
        String name = user.getFullName();
        String email = user.getEmail();

        // Stocker le code pour vérification
        VerificationCodeUtil.storeCode(email, code);

        // Envoyer par Email
        try {
            emailService.sendVerificationCode(email, name, code);
            System.out.println("[NotificationService] Code OTP envoyé par email à : " + email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Erreur email OTP : " + e.getMessage());
        }

        return code;
    }

    /**
     * Vérifie un code OTP pour un email donné.
     *
     * @param email email de l'utilisateur
     * @param code  code saisi par l'utilisateur
     * @return true si le code est valide et non expiré
     */
    public boolean verifyCode(String email, String code) {
        return VerificationCodeUtil.verifyCode(email, code);
    }

    // ─────────────────────────────────────────────────────────────────
    // CONFIRMATION D'INSCRIPTION
    // ─────────────────────────────────────────────────────────────────

    /**
     * Envoie un email de confirmation après l'inscription réussie.
     *
     * @param user l'utilisateur qui vient de s'inscrire
     */
    public void sendRegistrationConfirmation(User user) {
        try {
            emailService.sendRegistrationConfirmation(user.getEmail(), user.getFullName());
            System.out.println("[NotificationService] Email de bienvenue envoyé à : " + user.getEmail());
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Erreur email inscription : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // RÉINITIALISATION MOT DE PASSE
    // ─────────────────────────────────────────────────────────────────

    /**
     * Génère et envoie un code de réinitialisation de mot de passe par Email.
     *
     * @param user l'utilisateur qui réinitialise
     * @return le code OTP généré
     */
    public String sendPasswordReset(User user) {
        String code = VerificationCodeUtil.generateCode();
        String name = user.getFullName();
        String email = user.getEmail();

        // Stocker le code
        VerificationCodeUtil.storeCode(email, code);

        // Email
        try {
            emailService.sendPasswordReset(email, name, code);
            System.out.println("[NotificationService] Email reset mdp envoyé à : " + email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Erreur email reset mdp : " + e.getMessage());
        }

        return code;
    }

    /**
     * Version simplifiée — envoi par email uniquement.
     * Utile dans le flux "Mot de passe oublié" existant.
     *
     * @param email email de l'utilisateur
     * @param name  prénom/nom de l'utilisateur
     * @return le code OTP généré
     */
    public String sendPasswordResetByEmail(String email, String name) {
        String code = VerificationCodeUtil.generateCode();
        VerificationCodeUtil.storeCode(email, code);

        try {
            emailService.sendPasswordReset(email, name, code);
            System.out.println("[NotificationService] Code reset envoyé à : " + email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Erreur email reset mdp : " + e.getMessage());
        }

        return code;
    }

    // ─────────────────────────────────────────────────────────────────
    // NOTIFICATIONS GÉNÉRALES
    // ─────────────────────────────────────────────────────────────────

    /**
     * Envoie une notification par Email à un utilisateur.
     *
     * @param user    l'utilisateur destinataire
     * @param subject sujet de la notification (email uniquement)
     * @param message message de la notification
     */
    public void sendNotification(User user, String subject, String message) {
        // Email
        try {
            emailService.sendNotification(user.getEmail(), user.getFullName(), subject, message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Erreur email notification : " + e.getMessage());
        }
    }

    /**
     * Envoie une notification par email simple (sans entité User).
     *
     * @param toEmail  email du destinataire
     * @param userName prénom/nom du destinataire
     * @param subject  sujet
     * @param message  message HTML
     */
    public void sendEmailNotification(String toEmail, String userName, String subject, String message) {
        try {
            emailService.sendNotification(toEmail, userName, subject, message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("[NotificationService] Erreur email notification : " + e.getMessage());
        }
    }
}
