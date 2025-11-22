package at.edu.c02.ledcontroller;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDateTime;


import java.io.IOException;

/**
 * This class handles the actual logic
 */
public class LedControllerImpl implements LedController {
    private static final int[] GROUP_LED_IDS = {
            20, 21, 22, 23, 24, 25, 26, 27
    };
    private final ApiService apiService;
    private final Sleeper sleeper;

    public LedControllerImpl(ApiService apiService)
    {
        this(apiService, Thread::sleep);
    }

    // Visible for testing
    LedControllerImpl(ApiService apiService, Sleeper sleeper) {
        this.apiService = apiService;
        this.sleeper = sleeper;
    }

    @Override
    public JSONObject getLight(int id) throws IOException
    {
        JSONObject response = apiService.getLight(id);
        JSONArray lights = response.getJSONArray("lights");
        if (lights.length() == 0) {
            throw new IllegalArgumentException("No light found for id " + id);
        }
        return lights.getJSONObject(0);
    }

    @Override
    public void setLed(int id, String color) throws IOException {
        boolean state = true; // LED einschalten
        apiService.setLight(id, color, state);
    }


    @Override
    public JSONArray getGroupLeds() throws IOException
    {
        JSONObject response = apiService.getLights();
        JSONArray lights = response.getJSONArray("lights");
        JSONArray groupLeds = new JSONArray();

        for (int i = 0; i < lights.length(); i++) {
            JSONObject light = lights.getJSONObject(i);
            JSONObject group = light.optJSONObject("groupByGroup");
            if (group != null && group.has("name") && !group.isNull("name")) {
                groupLeds.put(light);
            }
        }

        return groupLeds;
    }

    @Override
    public void turnOffAllLeds() throws IOException
    {
        for (int id : GROUP_LED_IDS) {
            apiService.setLight(id, "#000000", false);
        }
    }

    @Override
    public void demo() throws IOException
    {
        // Call `getLights`, the response is a json object in the form `{ "lights": [ { ... }, { ... } ] }`
        JSONObject response = apiService.getLights();
        // get the "lights" array from the response
        JSONArray lights = response.getJSONArray("lights");
        // read the first json object of the lights array
        JSONObject firstLight = lights.getJSONObject(0);
        // read int and string properties of the light
        System.out.println("First light id is: " + firstLight.getInt("id"));
        System.out.println("First light color is: " + firstLight.getString("color"));
    }

    @Override
    public void spinningLed(String color, int turns, long sleepMillis) throws IOException, InterruptedException
    {
        turnOffAllLeds();

        if (turns <= 0) {
            return;
        }

        int currentIndex = 0;
        apiService.setLight(GROUP_LED_IDS[currentIndex], color, true);

        int totalSteps = turns * GROUP_LED_IDS.length;
        for (int step = 1; step <= totalSteps; step++) {
            sleeper.sleep(sleepMillis);
            apiService.setLight(GROUP_LED_IDS[currentIndex], "#000000", false);
            if (step == totalSteps) {
                break;
            }

            currentIndex = (currentIndex + 1) % GROUP_LED_IDS.length;
            apiService.setLight(GROUP_LED_IDS[currentIndex], color, true);
        }

        turnOffAllLeds();
    }

    @Override
    public void showTime() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        showTime(now.getHour(), now.getMinute(), now.getSecond());
    }

    @Override
    public void showTime(int hours, int minutes, int seconds) throws IOException {
        JSONArray groupLeds = getGroupLeds();
        int ledCount = groupLeds.length();
        if (ledCount == 0) {
            return;
        }

        int hourIndex = mapHourToIndex(hours, minutes, ledCount);
        int minuteIndex = mapToIndex(minutes, 60, ledCount);
        int secondIndex = mapToIndex(seconds, 60, ledCount);

        for (int i = 0; i < ledCount; i++) {
            JSONObject led = groupLeds.getJSONObject(i);
            int id = led.getInt("id");

            boolean isHour   = (i == hourIndex);
            boolean isMinute = (i == minuteIndex);
            boolean isSecond = (i == secondIndex);

            String color = LedController.mixColors(isHour, isMinute, isSecond);
            boolean state = !color.equals("#000000"); // aus, wenn komplett schwarz

            apiService.setLight(id, color, state);
        }
    }

    int mapHourToIndex(int hours, int minutes, int ledCount) {
        int h12 = hours % 12;
        double totalHours = h12 + (minutes / 60.0);  // Stunden mit Minutenanteil
        double ratio = totalHours / 12.0;
        double pos = ratio * ledCount;
        int index = (int) Math.round(pos) % ledCount;
        return index;
    }

    int mapToIndex(int value, int maxExclusive, int ledCount) {
        double ratio = value / (double) maxExclusive;
        double pos = ratio * ledCount;
        int index = (int) Math.round(pos) % ledCount;
        return index;
    }


}
