package at.edu.c02.ledcontroller;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApiServiceImpl implements ApiService {

    // Static loader for E2E test
    public static String getGroupId() throws IOException {
        return loadSecretStatic();
    }

    private static String loadSecretStatic() throws IOException {
        return Files.readString(Path.of("secret.txt")).trim();
    }

    // Instance loader
    private String loadSecret() throws IOException {
        return Files.readString(Path.of("secret.txt")).trim();
    }

    @Override
    public JSONObject getLights() throws IOException {
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/getLights");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Error: getLights failed");
        }

        return readJson(connection);
    }

    @Override
    public JSONObject getLight(int id) throws IOException {
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/getLight?id=" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Error: getLight failed");
        }

        return readJson(connection);
    }

    @Override
    public JSONObject setLight(int id, String color, boolean state) throws IOException {
        String groupId = loadSecret();

        URL url = new URL("https://balanced-civet-91.hasura.app/api/rest/setLight");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Hasura-Group-ID", groupId);
        connection.setDoOutput(true);

        String body = """
                {"id": %d, "color": "%s", "on": %b}
                """.formatted(id, color, state);

        connection.getOutputStream().write(body.getBytes());

        if (connection.getResponseCode() != 200) {
            throw new IOException("Error: setLight failed");
        }

        return readJson(connection);
    }

    private JSONObject readJson(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        );
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            sb.append((char) c);
        }
        return new JSONObject(sb.toString());
    }
}
