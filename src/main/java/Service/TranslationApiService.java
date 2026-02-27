package Service;

import java.net.*;
import java.io.*;

public class TranslationApiService {

    public String translate(String text, String targetLang) {

        try {
            String json = """
            {
              "q": "%s",
              "source": "fr",
              "target": "%s",
              "format": "text"
            }
            """.formatted(text, targetLang);

            URL url = new URL("https://libretranslate.com/translate");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            con.getOutputStream().write(json.getBytes());

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream())
            );

            String response = br.readLine();

            // extraction simple du texte traduit
            int start = response.indexOf(":\"") + 2;
            int end = response.lastIndexOf("\"");

            return response.substring(start, end);

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Translation failed";
        }
    }
}