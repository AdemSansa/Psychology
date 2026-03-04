package util;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour générer et vérifier les codes OTP (One-Time Password).
 * Les codes ont 6 chiffres et expirent après 10 minutes.
 */
public class VerificationCodeUtil {

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRATION_MS = 10 * 60 * 1000; // 10 minutes
    private static final SecureRandom random = new SecureRandom();

    // Map: email/téléphone -> [code, timestamp]
    private static final Map<String, long[]> codeStorage = new HashMap<>();

    /**
     * Génère un code OTP à 6 chiffres.
     */
    public static String generateCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Stocke un code OTP associé à un identifiant (email ou numéro de téléphone).
     * 
     * @param identifier email ou numéro de téléphone
     * @param code       le code OTP généré
     */
    public static void storeCode(String identifier, String code) {
        long[] entry = { Long.parseLong(code), System.currentTimeMillis() };
        codeStorage.put(identifier.toLowerCase(), entry);
    }

    /**
     * Vérifie si le code fourni est valide et non expiré pour l'identifiant donné.
     * Supprime le code après vérification réussie.
     * 
     * @param identifier email ou numéro de téléphone
     * @param code       le code à vérifier
     * @return true si le code est correct et valide, false sinon
     */
    public static boolean verifyCode(String identifier, String code) {
        String key = identifier.toLowerCase();
        long[] entry = codeStorage.get(key);

        if (entry == null) {
            return false; // Aucun code trouvé
        }

        long storedCode = entry[0];
        long timestamp = entry[1];

        // Vérifier l'expiration (10 min)
        if (System.currentTimeMillis() - timestamp > EXPIRATION_MS) {
            codeStorage.remove(key);
            return false; // Code expiré
        }

        // Vérifier le code
        if (storedCode == Long.parseLong(code)) {
            codeStorage.remove(key); // Supprimer après utilisation
            return true;
        }

        return false;
    }

    /**
     * Invalide manuellement un code (utile pour annulation).
     * 
     * @param identifier email ou numéro de téléphone
     */
    public static void invalidateCode(String identifier) {
        codeStorage.remove(identifier.toLowerCase());
    }

    /**
     * Vérifie si un code OTP est encore en attente pour cet identifiant.
     * 
     * @param identifier email ou numéro de téléphone
     * @return true si un code actif existe
     */
    public static boolean hasPendingCode(String identifier) {
        String key = identifier.toLowerCase();
        long[] entry = codeStorage.get(key);
        if (entry == null)
            return false;
        if (System.currentTimeMillis() - entry[1] > EXPIRATION_MS) {
            codeStorage.remove(key);
            return false;
        }
        return true;
    }
}
