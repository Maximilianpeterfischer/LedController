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

    /**
     * Load secret from a file named "secret.txt" located in the project root.
     *

    /**
     * Needed for E2E test
     */
    private static String loadSecretStatic() throws IOException {
        Path secretPath = Path.of("secret.txt");
        return Files.readString(secretPath).trim();
    }

    // Wird vom statischen E2E Test benötigt
    public static String getGroupId() throws IOException {
        return loadSecretStatic();
    }

    private String loadSecret() throws IOException {
        Path secretPath = Path.of("secret.txt");
        return Files.readString(secretPath).trim();
    }

    /**
     * GET /getLights
     */
    @Override
    public JSONObject getLights() throws IOException {
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/getLights");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: getLights request failed with response code " + responseCode);
        }

        return readJsonResponse(connection);
    }

    /**
     * GET /getLight?id=XX
     */
    @Override
    public JSONObject getLight(int id) throws IOException {
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/getLight?id=" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: getLight request failed with response code " + responseCode);
        }

        return readJsonResponse(connection);
    }

    /**
     * POST /setLight
     */
    @Override
    public JSONObject setLight(int id, String color, boolean state) throws IOException {
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/setLight");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);
        connection.setDoOutput(true);

        String body = String.format(
                "{\"id\": %d, \"color\": \"%s\", \"on\": %b}",
                id, color, state
        );

        connection.getOutputStream().write(body.getBytes());

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error: setLight request failed with response code " + responseCode);
        }

        return readJsonResponse(connection);
    }

    /**
     * Helper to read JSON from HTTP connection
     */
    private JSONObject readJsonResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            sb.append((char) c);
        }
        return new JSONObject(sb.toString());
    }
}
