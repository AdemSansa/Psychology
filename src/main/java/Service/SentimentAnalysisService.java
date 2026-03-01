package Service;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SentimentAnalysisService {

    private static final String API_TOKEN = "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";
    private static final String API_URL = "https://router.huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest";

    public String analyzeSentiment(String text) {
        try {
            // Create JSON request for Hugging Face API
            String jsonInputString = String.format("{\"inputs\":\"%s\"}", text.replace("\"", "\\\"").replace("\n", "\\n"));
            
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 second timeout
            conn.setReadTimeout(10000); // 10 second timeout

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Hugging Face API Response Code: " + responseCode);
            
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
                
                System.out.println("Raw Response: " + response.toString());

                try {
                    // Hugging Face returns a JSON array directly
                    JSONArray predictions = new JSONArray(response.toString());
                    
                    if (predictions.length() > 0) {
                        JSONArray firstPrediction = predictions.getJSONArray(0);
                        if (firstPrediction.length() > 0) {
                            JSONObject prediction = firstPrediction.getJSONObject(0);
                            String label = prediction.getString("label");
                            double score = prediction.getDouble("score");
                            
                            System.out.println("Sentiment: " + label + " (confidence: " + String.format("%.2f", score) + ")");
                            
                            return convertHuggingFaceLabel(label);
                        }
                    }
                } catch (Exception e) {
                    // Try parsing as JSONObject if array parsing fails
                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        
                        if (jsonResponse.has("error")) {
                            System.err.println("Hugging Face API Error: " + jsonResponse.getString("error"));
                            return "😐 Neutre";
                        }
                        
                        // Handle predictions object
                        if (jsonResponse.has("predictions") && jsonResponse.get("predictions") instanceof JSONArray) {
                            JSONArray predictions = jsonResponse.getJSONArray("predictions");
                            if (predictions.length() > 0) {
                                JSONObject prediction = predictions.getJSONObject(0);
                                String label = prediction.getString("label");
                                double score = prediction.getDouble("score");
                                
                                System.out.println("Sentiment: " + label + " (confidence: " + String.format("%.2f", score) + ")");
                                
                                return convertHuggingFaceLabel(label);
                            }
                        }
                    } catch (Exception e2) {
                        System.err.println("Failed to parse Hugging Face response: " + response.toString());
                        e2.printStackTrace();
                    }
                }
            } else if (responseCode == 404) {
                System.err.println("Model not found (404). The model may not be available or the endpoint is incorrect.");
                System.err.println("Trying fallback sentiment analysis...");
                return fallbackSentimentAnalysis(text);
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
            return fallbackSentimentAnalysis(text);
        }
        
        // Return neutral if API fails
        return "😐 Neutre";
    }

    private String fallbackSentimentAnalysis(String text) {
        // Simple keyword-based fallback
        String lowerText = text.toLowerCase();
        
        String[] positiveWords = {"bon", "excellent", "super", "génial", "merci", "heureux", "content"};
        String[] negativeWords = {"mauvais", "nul", "horrible", "terrible", "déçu", "colère", "problème"};
        
        for (String word : positiveWords) {
            if (lowerText.contains(word)) return "😊 Positif";
        }
        
        for (String word : negativeWords) {
            if (lowerText.contains(word)) return "😡 Négatif";
        }
        
        return "😐 Neutre";
    }

    private String convertHuggingFaceLabel(String label) {
        switch (label.toUpperCase()) {
            case "LABEL_2": // Positive
            case "POSITIVE":
                return "😊 Positif";
            case "LABEL_0": // Negative
            case "NEGATIVE":
                return "😡 Négatif";
            case "LABEL_1": // Neutral
            case "NEUTRAL":
                return "😐 Neutre";
            default:
                return "😐 Neutre";
        }
    }
}
