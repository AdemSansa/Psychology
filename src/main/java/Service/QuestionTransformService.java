package Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * to get rewritten versions of a question.
 */
public class QuestionTransformService {

    private static final String API_URL = "https://question-transformer.onrender.com/transform";

    /**
     * Sends a question to the transform API and returns up to 5 rewritten
     * suggestions.
     *
     * @param question the original question text
     * @return list of rewritten question suggestions
     * @throws IOException          if a network error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<String> getSuggestions(String question) throws IOException, InterruptedException {
        // Build JSON body
        String json = "{\"question\": \"" + escapeJson(question) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API returned status " + response.statusCode() + ": " + response.body());
        }

        return parseJsonSuggestions(response.body());
    }

    /**
     * Lightweight JSON string value extractor.
     * Pulls all string values from JSON key-value pairs like "key": "value".
     */
    private List<String> parseJsonSuggestions(String jsonBody) {
        List<String> suggestions = new ArrayList<>();

        // Match JSON string values: "key": "value"
        Pattern pattern = Pattern.compile("\"[^\"]+\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(jsonBody);

        while (matcher.find()) {
            String value = matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
            suggestions.add(value);
        }

        return suggestions;
    }

    /**
     * Escapes special characters for safe JSON embedding.
     */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
