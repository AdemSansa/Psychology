package Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to handle translations using the MyMemory API (Free).
 */
public class TranslationService {

    /**
     * Translates text from one language to another.
     *
     * @param text       the text to translate
     * @param fromLang   source language code (e.g., "en")
     * @param targetLang target language code (e.g., "fr", "ar")
     * @return the translated text
     * @throws IOException          if a network error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public String translate(String text, String fromLang, String targetLang) throws IOException, InterruptedException {
        if (fromLang.equals(targetLang)) {
            return text;
        }

        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String langPair = fromLang + "|" + targetLang;
        String encodedLangPair = URLEncoder.encode(langPair, StandardCharsets.UTF_8);

        String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + encodedLangPair;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Translation API returned status " + response.statusCode());
        }

        return parseTranslationJackson(response.body());
    }

    /**
     * Extracts translatedText from JSON response using Jackson.
     */
    private String parseTranslationJackson(String jsonBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonBody);
            JsonNode responseData = root.path("responseData");
            if (!responseData.isMissingNode()) {
                return responseData.path("translatedText").asText();
            }
            // Fallback to top-level if responseData is not there
            return root.path("translatedText").asText("Translation error");
        } catch (Exception e) {
            // Fallback to regex if Jackson fails for some reason
            return parseTranslation(jsonBody);
        }
    }

    /**
     * Extracts translatedText from JSON response using regex.
     */
    private String parseTranslation(String jsonBody) {
        // Match "translatedText":"..."
        Pattern pattern = Pattern.compile("\"translatedText\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(jsonBody);

        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
        }

        return "Translation error";
    }
}
