package Service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SentimentAnalysisService {

    private static final String API_TOKEN = "AIzaSyAY9cvdVpIC1Qc-ZS8Q4vpsTe1SQTtBmIM";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public String analyzeSentiment(String text) {
        try {
            // Create a simple sentiment analysis prompt
            String prompt = "Analyze the sentiment of this text and respond with only one word: POSITIVE, NEGATIVE, or NEUTRAL. Text: " + text;
            
            URL url = new URL(API_URL + "?key=" + API_TOKEN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n")
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject content = candidate.getJSONObject("content");
                    JSONArray parts = content.getJSONArray("parts");
                    if (parts.length() > 0) {
                        String sentiment = parts.getJSONObject(0).getString("text").trim().toUpperCase();
                        return convertSentimentToEmoji(sentiment);
                    }
                }
            } else {
                System.err.println("API Error: " + responseCode);
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream())
                );
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("Error Response: " + errorResponse.toString());
            }

        } catch (Exception e) {
            System.err.println("Sentiment analysis error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback to simple keyword-based analysis
        return simpleSentimentAnalysis(text);
    }

    private String convertSentimentToEmoji(String sentiment) {
        switch (sentiment) {
            case "POSITIVE":
                return "😊 Positif";
            case "NEGATIVE":
                return "😡 Négatif";
            case "NEUTRAL":
                return "😐 Neutre";
            default:
                return "❓ Inconnu";
        }
    }

    private String simpleSentimentAnalysis(String text) {
        String lowerText = text.toLowerCase();
        
        // Positive keywords
        if (lowerText.contains("bon") || lowerText.contains("bien") || lowerText.contains("excellent") ||
            lowerText.contains("super") || lowerText.contains("génial") || lowerText.contains("merci") ||
            lowerText.contains("heureux") || lowerText.contains("content") || lowerText.contains("satisfait")) {
            return "😊 Positif";
        }
        
        // Negative keywords
        if (lowerText.contains("mauvais") || lowerText.contains("nul") || lowerText.contains("horrible") ||
            lowerText.contains("terrible") || lowerText.contains("déçu") || lowerText.contains("colère") ||
            lowerText.contains("problème") || lowerText.contains("erreur") || lowerText.contains("échec")) {
            return "😡 Négatif";
        }
        
        return "😐 Neutre";
    }
}
