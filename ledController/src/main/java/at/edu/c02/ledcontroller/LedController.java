package at.edu.c02.ledcontroller;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;

public interface LedController {
    void demo() throws IOException;

    /**
     * Returns the statuses of all LEDs that are associated with a group.
     */
    JSONArray getGroupLeds() throws IOException;

    /**
     * Returns the status of a single LED by id.
     */
    JSONObject getLight(int id) throws IOException;

    void setLed(int id, String color) throws IOException;
    /**
     * Turns off all group LEDs.
     */
    void turnOffAllLeds() throws IOException;

    void spinningLed(String color, int turns, long sleepMillis) throws IOException, InterruptedException;

    void showTime() throws IOException;

    void showTime(int hours, int minutes, int seconds) throws IOException;

    static String mixColors(boolean hour, boolean minute, boolean second) {
        int r = hour ? 0xFF : 0;
        int g = minute ? 0xFF : 0;
        int b = second ? 0xFF : 0;

        return String.format("#%02x%02x%02x", r, g, b);
    }
    
     /**
     * Rotates the current group LED states/colors clockwise by the given number of steps.
     */
    void spinningWheel(int steps, long sleepMillis) throws IOException, InterruptedException;
}
