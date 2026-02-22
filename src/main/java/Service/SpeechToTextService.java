package Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle Speech-to-Text transcription using AssemblyAI.
 */
public class SpeechToTextService {

    // IMPORTANT: User needs to provide their own API key here.
    private static final String API_KEY = "b5ed1e6ebdd9401280a3495d2d669dbe";
    private static final String UPLOAD_URL = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_URL = "https://api.assemblyai.com/v2/transcript";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Transcribes an audio file to text.
     *
     * @param audioFile the recorded audio file
     * @return the transcribed text
     * @throws Exception if transcription fails
     */
    public String transcribe(File audioFile) throws Exception {
        if (API_KEY == null || API_KEY.trim().isEmpty() || API_KEY.equals("YOUR_ASSEMBLYAI_API_KEY")) {
            throw new IllegalStateException(
                    "AssemblyAI API Key not configured. Please add your key in SpeechToTextService.java");
        }

        // 1. Upload the file
        String uploadUrl = uploadFile(audioFile);

        // 2. Start transcription
        String transcriptId = startTranscription(uploadUrl);

        // 3. Poll for results
        return pollForTranscription(transcriptId);
    }

    private String uploadFile(File audioFile) throws IOException, InterruptedException {
        byte[] fileContent = Files.readAllBytes(audioFile.toPath());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOAD_URL))
                .header("Authorization", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileContent))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to upload audio file. Status: " + response.statusCode());
        }

        JsonNode jsonNode = objectMapper.readTree(response.body());
        return jsonNode.get("upload_url").asText();
    }

    private String startTranscription(String audioUrl) throws IOException, InterruptedException {
        String jsonBody = String.format(
                "{\"audio_url\": \"%s\", \"speech_models\": [\"universal-3-pro\"]}",
                audioUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRANSCRIPT_URL))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to start transcription. Status: " + response.statusCode() + " - " + response.body());
        }

        JsonNode jsonNode = objectMapper.readTree(response.body());
        return jsonNode.get("id").asText();
    }

    private String pollForTranscription(String id) throws Exception {
        String statusUrl = TRANSCRIPT_URL + "/" + id;

        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(statusUrl))
                    .header("Authorization", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());
            String status = jsonNode.get("status").asText();

            if (status.equals("completed")) {
                return jsonNode.get("text").asText();
            } else if (status.equals("error")) {
                throw new Exception("Transcription error: " + jsonNode.get("error").asText());
            }

            // Wait before polling again
            TimeUnit.SECONDS.sleep(3);
        }
    }
}
