package Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AISummaryService {

    // You will need to set this environment variable or replace this placeholder
    private static final String API_KEY = System.getenv("AIzaSyAauKQQ-hOeo9UyQ9RRmgLKetbRaMOYOJk");
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String generateSummary(String patientNote) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: Gemini API key is missing. Please set the GEMINI_API_KEY environment variable.";
        }

        String prompt = "You are a professional psychology assistant. Summarize the following session note into three sections: \n"
                +
                "1. Key Topics\n" +
                "2. Action Items\n" +
                "3. Patient Concerns\n\n" +
                "Keep it concise, professional, and do not include any conversational filler. Here is the note:\n\n\"\"\""
                + patientNote + "\"\"\"";

        try {
            ObjectMapper mapper = new ObjectMapper();

            // Construct the exact JSON payload expected by Gemini API
            String requestBody = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\":[{\"text\": " + mapper.writeValueAsString(prompt) + "}]\n" +
                    "  }]\n" +
                    "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = mapper.readTree(response.body());
                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).path("content");
                    JsonNode parts = content.path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText();
                    }
                }
                return "Error: Could not parse response from Gemini.";
            } else {
                return "Error from Gemini API: " + response.statusCode() + " - " + response.body();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}
