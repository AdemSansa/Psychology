package Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class TranslationApiService {

    // URL de l'API LibreTranslate (serveur public ou local)
    private final String apiUrl = "https://de.libretranslate.com/translate";
    private final String fallbackApiUrl = "https://libretranslate.de/translate";
    private final String alternativeApiUrl = "https://translate.terraprint.co/translate";
    private final String apiKey = ""; // Ajoutez votre clé API ici

    /**
     * Traduit un texte depuis une langue source vers une langue cible.
     *
     * @param text      Le texte à traduire
     * @param targetLang Code de la langue cible (ex: "en" pour anglais)
     * @return Texte traduit ou message d'erreur
     */
    public String translate(String text, String targetLang) {
        System.out.println("=== Starting Translation ===");
        System.out.println("Text: " + text);
        System.out.println("Target Language: " + targetLang);
        
        // Try the simple Google-based translation first
        SimpleTranslationService simpleService = new SimpleTranslationService();
        String result = simpleService.translate(text, targetLang);
        
        if (!result.startsWith("Translation failed") && !result.contains("[" + targetLang.toUpperCase() + "]")) {
            System.out.println("=== Translation Successful (Simple Service) ===");
            return result;
        }
        
        // If simple service fails, try the original APIs
        System.out.println("Simple service failed, trying original APIs...");
        result = tryTranslate(text, targetLang, apiUrl, apiKey);
        if (!result.startsWith("Translation failed") && !result.startsWith("Translation API error")) {
            System.out.println("=== Translation Successful (Main API) ===");
            return result;
        }
        
        // If main API fails, try fallback without API key
        System.out.println("Main API failed, trying fallback...");
        result = tryTranslate(text, targetLang, fallbackApiUrl, null);
        if (!result.startsWith("Translation failed") && !result.startsWith("Translation API error")) {
            System.out.println("=== Translation Successful (Fallback API) ===");
            return result;
        }
        
        // Try alternative API
        System.out.println("Fallback API failed, trying alternative...");
        result = tryTranslate(text, targetLang, alternativeApiUrl, null);
        if (!result.startsWith("Translation failed") && !result.startsWith("Translation API error")) {
            System.out.println("=== Translation Successful (Alternative API) ===");
            return result;
        }
        
        // If all APIs fail, return a simple mock translation for testing
        System.out.println("All APIs failed, returning mock translation for testing");
        System.out.println("=== Translation Complete (Mock) ===");
        return mockTranslate(text, targetLang);
    }
    
    private String mockTranslate(String text, String targetLang) {
        // Simple mock translation for testing purposes
        if (targetLang.equals("en")) {
            return "[EN] " + text + " (mock translation)";
        } else if (targetLang.equals("es")) {
            return "[ES] " + text + " (traducción simulada)";
        } else if (targetLang.equals("de")) {
            return "[DE] " + text + " (simulierte Übersetzung)";
        } else {
            return "[" + targetLang.toUpperCase() + "] " + text + " (mock translation)";
        }
    }
    
    private String tryTranslate(String text, String targetLang, String apiUrl, String apiKey) {
        try {
            // Création de l'objet JSON à envoyer
            JSONObject requestJson = new JSONObject();
            requestJson.put("q", text);
            requestJson.put("source", "auto");   // Détection automatique
            requestJson.put("target", targetLang);
            requestJson.put("format", "text");
            
            // N'inclure l'api_key que si elle n'est pas nulle et non vide
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                requestJson.put("api_key", apiKey);
            }

            // Connexion à l'API
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 secondes timeout
            conn.setReadTimeout(10000);   // 10 secondes timeout

            // Envoi du JSON
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = requestJson.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lecture de la réponse
            int status = conn.getResponseCode();
            InputStreamReader reader;
            if (status >= 200 && status < 300) {
                reader = new InputStreamReader(conn.getInputStream(), "utf-8");
            } else {
                reader = new InputStreamReader(conn.getErrorStream(), "utf-8");
            }

            BufferedReader br = new BufferedReader(reader);
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                response.append(line).append("\n"); // Keep newlines for better debugging
            }
            br.close();

            // Debug: Afficher le statut et la réponse brute
            System.out.println("API URL: " + apiUrl);
            System.out.println("HTTP Status: " + status);
            System.out.println("Raw Response: " + response.toString());

            // Convertir la réponse en JSON et vérifier les erreurs
            String responseStr = response.toString().trim();
            
            // Vérifier si la réponse est vide ou ne commence pas par {
            if (responseStr.isEmpty() || !responseStr.startsWith("{")) {
                System.out.println("Invalid JSON response - not starting with {");
                System.out.println("Response content: " + responseStr);
                return "Translation failed: Invalid JSON response - " + responseStr;
            }
            
            try {
                JSONObject json = new JSONObject(responseStr);
                
                // Vérifier si la réponse contient une erreur
                if (json.has("error")) {
                    String errorMsg = json.getString("error");
                    System.out.println("API Error: " + errorMsg);
                    return "Translation API error: " + errorMsg;
                }
                
                // Vérifier si le champ translatedText existe
                if (json.has("translatedText")) {
                    return json.getString("translatedText");
                } else {
                    System.out.println("Invalid response format - missing translatedText");
                    return "Translation failed: Invalid response format - " + responseStr;
                }
            } catch (Exception jsonEx) {
                System.out.println("JSON parsing error: " + jsonEx.getMessage());
                System.out.println("Response that failed to parse: " + responseStr);
                return "Translation failed: JSON parsing error - " + jsonEx.getMessage();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Translation failed: " + e.getMessage();
        }
    }
}