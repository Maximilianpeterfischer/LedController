package at.edu.c02.ledcontroller;

import org.json.JSONObject;

import java.io.IOException;

public interface ApiService {
    JSONObject getLights() throws IOException;

    // NEU: Einzelne LED per ID abfragen
    JSONObject getLight(int id) throws IOException;

    // NEU: LED setzen
    JSONObject setLight(int id, String color, boolean state) throws IOException;

    // NEU: LED löschen
    void deleteLight(int id) throws IOException;
}

