package Service;

import org.json.JSONObject;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Properties;

public class FaceIDService {
    private static FaceIDService instance;
    private String apiKey;
    private String apiSecret;
    private final HttpClient httpClient;

    private FaceIDService() {
        this.httpClient = HttpClient.newHttpClient();
        loadConfig();
    }

    public static synchronized FaceIDService getInstance() {
        if (instance == null) {
            instance = new FaceIDService();
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                this.apiKey = prop.getProperty("faceplusplus.api_key");
                this.apiSecret = prop.getProperty("faceplusplus.api_secret");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Compares a live captured image (byte array) with a reference image URL.
     * Returns the similarity score (0-100).
     */
    public double compareFaces(byte[] liveImageBytes, String referenceImageUrl) throws Exception {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_FACEPLUSPLUS_KEY_HERE")) {
            throw new Exception("Face++ API Key non configurée dans config.properties");
        }

        // API Face++ Compare (using US or CN region - US is usually safer for global)
        String url = "https://api-us.faceplusplus.com/facepp/v3/compare";

        String boundary = "---" + System.currentTimeMillis();

        // Build multipart body
        byte[] multipartBody = buildMultipartBody(boundary, liveImageBytes, referenceImageUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            if (json.has("confidence")) {
                return json.getDouble("confidence");
            } else {
                throw new Exception("Format de réponse invalide de Face++");
            }
        } else {
            // Log full error for debugging
            System.err.println("Détails Erreur Face++: " + response.body());
            throw new Exception("Erreur Face++ API (" + response.statusCode() + "): " + response.body());
        }
    }

    private byte[] buildMultipartBody(String boundary, byte[] fileData, String imageUrl) throws Exception {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();

        // api_key
        addPart(byteStream, boundary, "api_key", apiKey);
        // api_secret
        addPart(byteStream, boundary, "api_secret", apiSecret);
        // image_url2 (Reference)
        addPart(byteStream, boundary, "image_url2", imageUrl);

        // image_file1 (Live capture)
        byteStream.write(("--" + boundary + "\r\n").getBytes());
        byteStream.write(
                ("Content-Disposition: form-data; name=\"image_file1\"; filename=\"capture.jpg\"\r\n").getBytes());
        byteStream.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
        byteStream.write(fileData);
        byteStream.write(("\r\n").getBytes());

        // End
        byteStream.write(("--" + boundary + "--\r\n").getBytes());

        return byteStream.toByteArray();
    }

    private void addPart(java.io.ByteArrayOutputStream os, String boundary, String name, String value)
            throws Exception {
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        os.write((value + "\r\n").getBytes());
    }
}
