package at.edu.c02.ledcontroller;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiServiceImpl implements ApiService {

    private static final String BASE_URL = "https://balanced-civet-91.hasura.app/api/rest";
    private static final String DEFAULT_GROUP_ID = ""; // wird in Story 3.1 ersetzt
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_BACKOFF_MS = 1000L;
    private static final long REQUEST_DELAY_MS = 1500L;

    static String getGroupId() {
        return DEFAULT_GROUP_ID;
    }

    // Hilfsmethode für HTTP-Requests
    JSONObject sendRequest(String path, String method, JSONObject body) throws IOException {
        URL url = new URL(BASE_URL + path);
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                Thread.sleep(REQUEST_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting to respect rate limit", e);
            }

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
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                int character;
                while ((character = reader.read()) != -1) {
                    sb.append((char) character);
                }
                return sb.isEmpty() ? new JSONObject() : new JSONObject(sb.toString());
            }

            if (responseCode == 429 && attempt < MAX_RETRIES) {
                long retryDelayMs = RETRY_BACKOFF_MS * (attempt + 1);
                String retryAfterHeader = connection.getHeaderField("Retry-After");
                if (retryAfterHeader != null) {
                    try {
                        long retryAfterSeconds = Long.parseLong(retryAfterHeader.trim());
                        retryDelayMs = Math.max(retryDelayMs, retryAfterSeconds * 1000);
                    } catch (NumberFormatException ignored) {
                        // ignore invalid header, fall back to calculated backoff
                    }
                }
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting to retry request", e);
                }
                continue;
            }

            throw new IOException("Error: " + method + " " + path + " failed with code " + responseCode);
        }
        throw new IOException("Error: " + method + " " + path + " failed after retries");
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

    @Override
    public void deleteLight(int id) throws IOException {
        sendRequest("/lights/" + id, "DELETE", null);
    }

}
