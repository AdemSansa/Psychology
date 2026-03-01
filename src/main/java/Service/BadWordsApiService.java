package Service;

import java.net.*;
import java.io.*;

public class BadWordsApiService {

    public boolean containsBadWords(String text) {
        try {
            String encoded = URLEncoder.encode(text, "UTF-8");
            String urlStr =
                    "https://www.purgomalum.com/service/containsprofanity?text=" + encoded;

            URL url = new URL(urlStr);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream())
            );

            String response = br.readLine(); // true / false
            return Boolean.parseBoolean(response);

        } catch (Exception e) {
            e.printStackTrace();
            return true; // sécurité
        }
    }
}