package Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;

public class SimpleTranslationService {
    
    /**
     * Simple translation service using Google Translate API (unofficial)
     * This provides basic translation functionality without requiring API keys
     */
    public String translate(String text, String targetLang) {
        try {
            // Use a simple Google Translate API alternative
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + 
                          targetLang + "&dt=t&q=" + URLEncoder.encode(text, "UTF-8");
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // Parse the Google Translate response
                String jsonResponse = response.toString();
                if (jsonResponse.startsWith("[[")) {
                    // Extract the translated text from Google's response format
                    String[] parts = jsonResponse.split("\"");
                    if (parts.length > 1) {
                        return parts[1]; // This is usually the translated text
                    }
                }
                
                return "Translation failed: Could not parse response";
            } else {
                return "Translation failed: HTTP " + status;
            }
            
        } catch (Exception e) {
            System.out.println("Simple translation error: " + e.getMessage());
            // Return a basic translation for common phrases
            return basicTranslation(text, targetLang);
        }
    }
    
    private String basicTranslation(String text, String targetLang) {
        // Basic translations for common phrases
        text = text.toLowerCase().trim();
        
        if (targetLang.equals("en")) {
            if (text.contains("bonjour")) return "Hello";
            if (text.contains("merci")) return "Thank you";
            if (text.contains("au revoir")) return "Goodbye";
            if (text.contains("oui")) return "Yes";
            if (text.contains("non")) return "No";
            if (text.contains("salut")) return "Hi";
            if (text.contains("comment")) return "How";
        } else if (targetLang.equals("es")) {
            if (text.contains("bonjour")) return "Hola";
            if (text.contains("merci")) return "Gracias";
            if (text.contains("au revoir")) return "Adiós";
            if (text.contains("oui")) return "Sí";
            if (text.contains("non")) return "No";
        } else if (targetLang.equals("de")) {
            if (text.contains("bonjour")) return "Hallo";
            if (text.contains("merci")) return "Danke";
            if (text.contains("au revoir")) return "Auf Wiedersehen";
            if (text.contains("oui")) return "Ja";
            if (text.contains("non")) return "Nein";
        }
        
        // If no basic translation found, return with language indicator
        return "[" + targetLang.toUpperCase() + "] " + text;
    }
}
