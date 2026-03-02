package Service;

import Entities.Availabilities;
import Entities.Therapistis;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AIMatchmakingService {

    private static final String API_KEY = "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";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private final TherapistService therapistService;
    private final AvailabilityService availabilityService;

    public AIMatchmakingService() {
        this.therapistService = new TherapistService();
        this.availabilityService = new AvailabilityService();
    }

    public MatchmakingResult findBestTherapist(String patientMessage) throws Exception {
        String therapistDataStr = buildTherapistDataContext();
        String prompt = buildPrompt(patientMessage, therapistDataStr);
        String aiResponse = callGroqAPI(prompt);
        return parseAIResponse(aiResponse);
    }

    private String buildTherapistDataContext() throws SQLException {
        List<Therapistis> therapists = therapistService.list();
        StringBuilder sb = new StringBuilder();
        sb.append("Available Therapists:\n");
        for (Therapistis t : therapists) {
            sb.append(String.format(
                    "- ID: %d | Name: %s %s | Specialization: %s | Description: %s\n",
                    t.getId(), t.getFirstName(), t.getLastName(),
                    t.getSpecialization(), t.getDescription()));
            List<Availabilities> availabilities = availabilityService.listByTherapistId(t.getId());
            if (availabilities.isEmpty()) {
                sb.append("  (No availability)\n");
            } else {
                sb.append("  Availabilities:\n");
                for (Availabilities a : availabilities) {
                    if (a.isAvailable()) {
                        sb.append(String.format(
                                "    -> %s from %s to %s\n",
                                a.getDay(), a.getStartTime(), a.getEndTime()));
                    }
                }
            }
        }
        return sb.toString();
    }

    private String buildPrompt(String patientMessage, String therapistContext) {
        return "You are an empathetic and professional AI psychologist assistant.\n"
                + "Patient statement: \"" + patientMessage + "\"\n\n"
                + "Context (Available Therapists):\n" + therapistContext + "\n\n"
                + "Your task is to provide a supportive and informative recommendation.\n"
                + "Guidelines for the 'reasoning' field:\n"
                + " - Start with deep empathy: Acknowledge the patient's struggle and validate their feelings.\n"
                + " - Provide brief insight: Briefly mention what their condition involves from a professional perspective.\n"
                + " - Explain the match: Clearly justify why this specific therapist is the best fit.\n"
                + " - Tone: Warm, professional, and encouraging.\n\n"
                + "Additionally, recommend 2-3 books that could help the patient handle their specific situation.\n"
                + "Respond ONLY in strict JSON (no text before or after, no markdown):\n"
                + "{\n"
                + "  \"therapistId\": <NUMERIC_ID>,\n"
                + "  \"reasoning\": \"<Your combined empathy, insight, and justification in English>\",\n"
                + "  \"recommendedSlot\": \"<recommended slot in English>\",\n"
                + "  \"recommendedBooks\": [\n"
                + "    {\n"
                + "      \"title\": \"<Book Title>\",\n"
                + "      \"author\": \"<Author Name>\",\n"
                + "      \"description\": \"<Mini description of 1 sentence why this book helps>\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
    }

    private String callGroqAPI(String prompt) throws IOException, InterruptedException {
        // Format OpenAI-compatible (Groq use the same format)
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("Groq API Error: " + response.body());
            throw new IOException("Groq API Error: HTTP " + response.statusCode());
        }

        return response.body();
    }

    private MatchmakingResult parseAIResponse(String responseBody) throws Exception {
        // OpenAI response format: choices[0].message.content
        JSONObject responseObj = new JSONObject(responseBody);
        JSONArray choices = responseObj.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageObj = firstChoice.getJSONObject("message");
        String text = messageObj.getString("content");

        String cleanJson = text.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*$", "").trim();

        JSONObject aiJson = new JSONObject(cleanJson);
        int therapistId = aiJson.getInt("therapistId");
        String reasoning = aiJson.getString("reasoning");
        String suggestedSlot = aiJson.optString("recommendedSlot", "To be defined with the therapist");

        List<Book> books = new ArrayList<>();
        if (aiJson.has("recommendedBooks")) {
            JSONArray booksArray = aiJson.getJSONArray("recommendedBooks");
            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject bObj = booksArray.getJSONObject(i);
                String title = bObj.getString("title");
                String author = bObj.getString("author");
                String desc = bObj.optString("description", "");

                // Open Library simple cover URL (predictive)
                String coverUrl = String.format("https://covers.openlibrary.org/b/title/%s-M.jpg",
                        title.replace(" ", "%20"));

                books.add(new Book(title, author, desc, coverUrl));
            }
        }

        Therapistis recommended = therapistService.read(therapistId);
        if (recommended == null) {
            throw new Exception("Invalid therapist ID: " + therapistId);
        }

        return new MatchmakingResult(recommended, reasoning, suggestedSlot, books);
    }

    public static class MatchmakingResult {

        private final Therapistis therapist;
        private final String explanation;
        private final String suggestedSlot;
        private final List<Book> recommendedBooks;

        public MatchmakingResult(Therapistis therapist, String explanation, String suggestedSlot,
                List<Book> recommendedBooks) {
            this.therapist = therapist;
            this.explanation = explanation;
            this.suggestedSlot = suggestedSlot;
            this.recommendedBooks = recommendedBooks;
        }

        public Therapistis getTherapist() {
            return therapist;
        }

        public String getExplanation() {
            return explanation;
        }

        public String getSuggestedSlot() {
            return suggestedSlot;
        }

        public List<Book> getRecommendedBooks() {
            return recommendedBooks;
        }
    }

    public static class Book {
        private final String title;
        private final String author;
        private final String description;
        private final String coverUrl;

        public Book(String title, String author, String description, String coverUrl) {
            this.title = title;
            this.author = author;
            this.description = description;
            this.coverUrl = coverUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getDescription() {
            return description;
        }

        public String getCoverUrl() {
            return coverUrl;
        }
    }
}
