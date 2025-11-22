package at.edu.c02.ledcontroller;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiServiceImpl implements ApiService {

    private static final String BASE_URL = "https://balanced-civet-91.hasura.app/api/rest";
    private static final String DEFAULT_GROUP_ID = "Group G"; // wird in Story 3.1 ersetzt

    static String getGroupId() {
        return System.getProperty("hasura.group.id",
                System.getenv().getOrDefault("HASURA_GROUP_ID", DEFAULT_GROUP_ID));
    }

    // Hilfsmethode für HTTP-Requests
    JSONObject sendRequest(String path, String method, JSONObject body) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("X-Hasura-Group-ID", getGroupId());

        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] payload = body.toString().getBytes();
            connection.getOutputStream().write(payload);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Error: " + method + " " + path + " failed with code " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        int character;
        while ((character = reader.read()) != -1) {
            sb.append((char) character);
        }

        return sb.isEmpty() ? new JSONObject() : new JSONObject(sb.toString());
    }

    // Hilfsmethode für GET-Requests
    JSONObject sendGetRequest(String path) throws IOException {
        return sendRequest(path, "GET", null);
    }

    @Override
    public JSONObject getLights() throws IOException {
        return sendGetRequest("/getLights");
    }

    @Override
    public JSONObject getLight(int id) throws IOException {
        return sendGetRequest("/lights/" + id);
    }

    @Override
    public JSONObject setLight(int id, String color, boolean state) throws IOException {
        JSONObject body = new JSONObject()
                .put("id", id)
                .put("color", color)
                .put("state", state);
        return sendRequest("/setLight", "PUT", body);
    }
}
