package at.edu.c02.ledcontroller;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class should handle all HTTP communication with the server.
 */
public class ApiServiceImpl implements ApiService {

    private String loadSecret() throws IOException {
        // Reads the file "secret.txt" located in project root
        Path secretPath = Path.of("secret.txt");
        return Files.readString(secretPath).trim();
    }

    @Override
    public JSONObject getLights() throws IOException {
        // Load secret from file
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/getLights");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // USE SECRET HERE
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: getLights request failed with response code " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();

        int character;
        while ((character = reader.read()) != -1) {
            sb.append((char) character);
        }

        return new JSONObject(sb.toString());
    }
}
