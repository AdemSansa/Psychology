package Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;
import java.io.InputStream;

public class ImgBBService {
    private static ImgBBService instance;
    private String apiKey;
    private final HttpClient httpClient;

    private ImgBBService() {
        this.httpClient = HttpClient.newHttpClient();
        loadConfig();
    }

    public static synchronized ImgBBService getInstance() {
        if (instance == null) {
            instance = new ImgBBService();
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                this.apiKey = prop.getProperty("imgbb.api_key");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String uploadImage(File file) throws Exception {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return uploadImage(fileContent);
    }

    public String uploadImage(byte[] fileContent) throws Exception {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new Exception("ImgBB API key is not configured in config.properties");
        }

        String base64Image = Base64.getEncoder().encodeToString(fileContent);
        String form = "image=" + java.net.URLEncoder.encode(base64Image, "UTF-8");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imgbb.com/1/upload?key=" + apiKey))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Simplified JSON parsing to get the URL
            String body = response.body();
            int urlIndex = body.indexOf("\"url\":\"") + 7;
            int endIndex = body.indexOf("\"", urlIndex);
            String url = body.substring(urlIndex, endIndex).replace("\\/", "/");
            return url;
        } else {
            throw new Exception("Failed to upload image to ImgBB: " + response.body());
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
