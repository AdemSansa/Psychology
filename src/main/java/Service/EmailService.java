package Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Service d'envoi d'emails via SMTP Gmail.
 * Utilise JavaMail (jakarta.mail).
 *
 * Configuration dans : src/main/resources/config.properties
 * email.sender=votre_email@gmail.com
 * email.password=votre_app_password
 */
public class EmailService {

    private static EmailService instance;

    private final String senderEmail;
    private final String senderPassword;
    private final String smtpHost;
    private final int smtpPort;
    private final String appName;

    public EmailService() {
        Properties config = loadConfig();
        this.senderEmail = config.getProperty("email.sender");
        this.senderPassword = config.getProperty("email.password");
        this.smtpHost = config.getProperty("email.smtp.host", "smtp.gmail.com");
        this.smtpPort = Integer.parseInt(config.getProperty("email.smtp.port", "587"));
        this.appName = config.getProperty("email.app.name", "Psychology App");
    }

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────
    // MÉTHODES PUBLIQUES
    // ─────────────────────────────────────────────────────────────────

    /**
     * Envoie un code de vérification OTP à l'utilisateur.
     *
     * @param toEmail  email du destinataire
     * @param userName prénom/nom de l'utilisateur
     * @param code     code OTP à 6 chiffres
     */
    public void sendVerificationCode(String toEmail, String userName, String code)
            throws MessagingException, UnsupportedEncodingException {
        String subject = "📋 Votre code de vérification — " + appName;
        String body = buildVerificationCodeEmail(userName, code);
        sendEmail(toEmail, subject, body);
    }

    /**
     * Envoie un email de confirmation d'inscription (bienvenue).
     *
     * @param toEmail  email du destinataire
     * @param userName prénom/nom de l'utilisateur
     */
    public void sendRegistrationConfirmation(String toEmail, String userName)
            throws MessagingException, UnsupportedEncodingException {
        String subject = "🎉 Bienvenue sur " + appName + " !";
        String body = buildRegistrationConfirmationEmail(userName);
        sendEmail(toEmail, subject, body);
    }

    /**
     * Envoie un code de réinitialisation de mot de passe.
     *
     * @param toEmail  email du destinataire
     * @param userName prénom/nom de l'utilisateur
     * @param code     code OTP à 6 chiffres
     */
    public void sendPasswordReset(String toEmail, String userName, String code)
            throws MessagingException, UnsupportedEncodingException {
        String subject = "🔑 Réinitialisation de votre mot de passe — " + appName;
        String body = buildPasswordResetEmail(userName, code);
        sendEmail(toEmail, subject, body);
    }

    /**
     * Envoie une notification personnalisée.
     *
     * @param toEmail  email du destinataire
     * @param userName prénom/nom de l'utilisateur
     * @param subject  sujet de l'email
     * @param message  message HTML ou texte
     */
    public void sendNotification(String toEmail, String userName, String subject, String message)
            throws MessagingException, UnsupportedEncodingException {
        String body = buildNotificationEmail(userName, message);
        sendEmail(toEmail, subject, body);
    }

    // ─────────────────────────────────────────────────────────────────
    // MÉTHODES PRIVÉES — Construction des templates HTML
    // ─────────────────────────────────────────────────────────────────

    private String buildVerificationCodeEmail(String userName, String code) {
        return "<html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;'>"
                + "<div style='max-width:500px;margin:auto;background:white;border-radius:12px;padding:30px;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color:#4A90D9;text-align:center;'>🔐 Code de vérification</h2>"
                + "<p>Bonjour <strong>" + userName + "</strong>,</p>"
                + "<p>Votre code de vérification est :</p>"
                + "<div style='text-align:center;margin:20px 0;'>"
                + "<span style='font-size:36px;font-weight:bold;letter-spacing:8px;color:#2C3E50;background:#EBF5FB;padding:12px 24px;border-radius:8px;border:2px dashed #4A90D9;'>"
                + code + "</span></div>"
                + "<p style='color:#7F8C8D;font-size:13px;'>Ce code expire dans <strong>10 minutes</strong>.</p>"
                + "<p style='color:#7F8C8D;font-size:13px;'>Si vous n'avez pas demandé ce code, ignorez cet email.</p>"
                + "<hr style='border:none;border-top:1px solid #ECF0F1;margin:20px 0;'/>"
                + "<p style='text-align:center;color:#BDC3C7;font-size:12px;'>" + appName
                + " — Tous droits réservés</p>"
                + "</div></body></html>";
    }

    private String buildRegistrationConfirmationEmail(String userName) {
        return "<html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;'>"
                + "<div style='max-width:500px;margin:auto;background:white;border-radius:12px;padding:30px;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color:#2ECC71;text-align:center;'>🎉 Bienvenue sur " + appName + " !</h2>"
                + "<p>Bonjour <strong>" + userName + "</strong>,</p>"
                + "<p>Nous sommes ravis de vous accueillir sur notre plateforme de bien-être psychologique.</p>"
                + "<p>Votre compte a été créé avec succès. Vous pouvez désormais :</p>"
                + "<ul style='color:#2C3E50;'>"
                + "<li>🧠 Consulter nos thérapeutes qualifiés</li>"
                + "<li>📅 Prendre des rendez-vous</li>"
                + "<li>📝 Accéder aux quiz et évaluations</li>"
                + "<li>💬 Participer à notre forum</li>"
                + "</ul>"
                + "<p>Merci de nous faire confiance pour votre bien-être.</p>"
                + "<hr style='border:none;border-top:1px solid #ECF0F1;margin:20px 0;'/>"
                + "<p style='text-align:center;color:#BDC3C7;font-size:12px;'>" + appName
                + " — Tous droits réservés</p>"
                + "</div></body></html>";
    }

    private String buildPasswordResetEmail(String userName, String code) {
        return "<html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;'>"
                + "<div style='max-width:500px;margin:auto;background:white;border-radius:12px;padding:30px;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color:#E74C3C;text-align:center;'>🔑 Réinitialisation de mot de passe</h2>"
                + "<p>Bonjour <strong>" + userName + "</strong>,</p>"
                + "<p>Vous avez demandé la réinitialisation de votre mot de passe. Voici votre code :</p>"
                + "<div style='text-align:center;margin:20px 0;'>"
                + "<span style='font-size:36px;font-weight:bold;letter-spacing:8px;color:#E74C3C;background:#FDEDEC;padding:12px 24px;border-radius:8px;border:2px dashed #E74C3C;'>"
                + code + "</span></div>"
                + "<p style='color:#7F8C8D;font-size:13px;'>⚠️ Ce code expire dans <strong>10 minutes</strong>.</p>"
                + "<p style='color:#7F8C8D;font-size:13px;'>Si vous n'avez pas demandé cette réinitialisation, sécurisez votre compte immédiatement.</p>"
                + "<hr style='border:none;border-top:1px solid #ECF0F1;margin:20px 0;'/>"
                + "<p style='text-align:center;color:#BDC3C7;font-size:12px;'>" + appName
                + " — Tous droits réservés</p>"
                + "</div></body></html>";
    }

    private String buildNotificationEmail(String userName, String message) {
        return "<html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:30px;'>"
                + "<div style='max-width:500px;margin:auto;background:white;border-radius:12px;padding:30px;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color:#4A90D9;text-align:center;'>🔔 Notification</h2>"
                + "<p>Bonjour <strong>" + userName + "</strong>,</p>"
                + "<div style='background:#EBF5FB;border-left:4px solid #4A90D9;padding:15px;border-radius:4px;margin:15px 0;'>"
                + "<p style='margin:0;'>" + message + "</p></div>"
                + "<hr style='border:none;border-top:1px solid #ECF0F1;margin:20px 0;'/>"
                + "<p style='text-align:center;color:#BDC3C7;font-size:12px;'>" + appName
                + " — Tous droits réservés</p>"
                + "</div></body></html>";
    }

    // ─────────────────────────────────────────────────────────────────
    // MÉTHODES UTILITAIRES
    // ─────────────────────────────────────────────────────────────────

    /**
     * Envoie un email HTML via SMTP Gmail (TLS/STARTTLS).
     */
    public void sendEmail(String toEmail, String subject, String htmlBody)
            throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.ssl.trust", smtpHost);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail, appName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("[EmailService] Email envoyé à : " + toEmail + " | Sujet: " + subject);
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.err.println("[EmailService] ATTENTION: config.properties introuvable dans les ressources.");
            }
        } catch (IOException e) {
            System.err.println("[EmailService] Erreur lors du chargement de la config: " + e.getMessage());
        }
        return props;
    }
}
