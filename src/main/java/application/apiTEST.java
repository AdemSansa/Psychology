package application;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class apiTEST {

     public static void main(String[] args) throws IOException, InterruptedException {
         // Build the request
         String json = "{\"question\": \"Do you feel anxious when facing new social situations?\"}";
         HttpRequest request = HttpRequest.newBuilder()
                 .uri(URI.create("http://localhost:8000/transform"))
                 .header("Content-Type", "application/json")
                 .POST(HttpRequest.BodyPublishers.ofString(json))
                 .build();
// Send it
         HttpClient client = HttpClient.newHttpClient();
         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
         System.out.println(response.body()); // JSON with 5 rewritten versions
     }


}
