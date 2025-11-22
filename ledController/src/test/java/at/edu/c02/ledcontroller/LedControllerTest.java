package at.edu.c02.ledcontroller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class LedControllerTest {

    @Test
    public void getGroupLedsUsesApiServiceAndFiltersToGroupedLights() throws Exception {
        ApiService apiService = mock(ApiService.class);
        LedController controller = new LedControllerImpl(apiService);

        JSONObject groupLightA = new JSONObject()
                .put("id", 1)
                .put("color", "#fff")
                .put("on", true)
                .put("groupByGroup", new JSONObject().put("name", "A"));

        JSONObject groupLightB = new JSONObject()
                .put("id", 2)
                .put("color", "#000")
                .put("on", false)
                .put("groupByGroup", new JSONObject().put("name", "B"));

        JSONObject withoutGroup = new JSONObject()
                .put("id", 3)
                .put("color", "#123456")
                .put("on", true);

        JSONObject nullGroup = new JSONObject()
                .put("id", 4)
                .put("color", "#abcdef")
                .put("on", false)
                .put("groupByGroup", JSONObject.NULL);

        JSONArray lights = new JSONArray()
                .put(groupLightA)
                .put(withoutGroup)
                .put(groupLightB)
                .put(nullGroup);

        when(apiService.getLights()).thenReturn(new JSONObject().put("lights", lights));

        JSONArray result = controller.getGroupLeds();

        JSONArray expected = new JSONArray()
                .put(groupLightA)
                .put(groupLightB);

        assertEquals(expected.toString(), result.toString());
        verify(apiService, times(1)).getLights();
        verify(apiService, never()).getLight(anyInt());
    }

    @Test
    public void getLightReturnsSingleLightFromApi() throws Exception {
        ApiService apiService = mock(ApiService.class);
        LedController controller = new LedControllerImpl(apiService);

        JSONObject light = new JSONObject()
                .put("id", 10)
                .put("color", "#0f0")
                .put("on", true);

        when(apiService.getLight(10)).thenReturn(new JSONObject()
                .put("lights", new JSONArray().put(light)));

        JSONObject result = controller.getLight(10);

        assertEquals(light.toString(), result.toString());
        verify(apiService, times(1)).getLight(10);
        verify(apiService, never()).getLights();
    }

    @Test
    public void turnOffAllLedsCallsSetLightForConfiguredIds() throws Exception {
        ApiService apiService = mock(ApiService.class);
        LedController controller = new LedControllerImpl(apiService);

        when(apiService.setLight(anyInt(), anyString(), anyBoolean())).thenReturn(new JSONObject());

        controller.turnOffAllLeds();

        verify(apiService).setLight(20, "#000000", false);
        verify(apiService).setLight(21, "#000000", false);
        verify(apiService).setLight(22, "#000000", false);
        verify(apiService).setLight(23, "#000000", false);
        verify(apiService).setLight(24, "#000000", false);
        verify(apiService).setLight(25, "#000000", false);
        verify(apiService).setLight(26, "#000000", false);
        verify(apiService).setLight(27, "#000000", false);
        verify(apiService, times(8)).setLight(anyInt(), eq("#000000"), eq(false));
        verifyNoMoreInteractions(apiService);
    }

    @Test
    public void spinningLedLightsUpLedsInSequence() throws Exception {
        ApiService apiService = mock(ApiService.class);
        Sleeper sleeper = mock(Sleeper.class);
        LedControllerImpl controller = new LedControllerImpl(apiService, sleeper);

        when(apiService.setLight(anyInt(), anyString(), anyBoolean())).thenReturn(new JSONObject());

        controller.spinningLed("#ff0000", 1, 5L);

        InOrder inOrder = inOrder(apiService, sleeper);

        inOrder.verify(apiService).setLight(20, "#000000", false);
        inOrder.verify(apiService).setLight(21, "#000000", false);
        inOrder.verify(apiService).setLight(22, "#000000", false);
        inOrder.verify(apiService).setLight(23, "#000000", false);
        inOrder.verify(apiService).setLight(24, "#000000", false);
        inOrder.verify(apiService).setLight(25, "#000000", false);
        inOrder.verify(apiService).setLight(26, "#000000", false);
        inOrder.verify(apiService).setLight(27, "#000000", false);

        inOrder.verify(apiService).setLight(20, "#ff0000", true);

        int[] ids = {20, 21, 22, 23, 24, 25, 26, 27};
        for (int i = 0; i < ids.length - 1; i++) {
            inOrder.verify(sleeper).sleep(5L);
            inOrder.verify(apiService).setLight(ids[i], "#000000", false);
            inOrder.verify(apiService).setLight(ids[i + 1], "#ff0000", true);
        }

        inOrder.verify(sleeper).sleep(5L);
        inOrder.verify(apiService).setLight(27, "#000000", false);

        inOrder.verify(apiService).setLight(20, "#000000", false);
        inOrder.verify(apiService).setLight(21, "#000000", false);
        inOrder.verify(apiService).setLight(22, "#000000", false);
        inOrder.verify(apiService).setLight(23, "#000000", false);
        inOrder.verify(apiService).setLight(24, "#000000", false);
        inOrder.verify(apiService).setLight(25, "#000000", false);
        inOrder.verify(apiService).setLight(26, "#000000", false);
        inOrder.verify(apiService).setLight(27, "#000000", false);

        verify(apiService, times(1)).setLight(20, "#ff0000", true);
        verify(apiService, times(1)).setLight(21, "#ff0000", true);
        verify(apiService, times(1)).setLight(22, "#ff0000", true);
        verify(apiService, times(1)).setLight(23, "#ff0000", true);
        verify(apiService, times(1)).setLight(24, "#ff0000", true);
        verify(apiService, times(1)).setLight(25, "#ff0000", true);
        verify(apiService, times(1)).setLight(26, "#ff0000", true);
        verify(apiService, times(1)).setLight(27, "#ff0000", true);
        verify(sleeper, times(8)).sleep(5L);
        verifyNoMoreInteractions(apiService, sleeper);
    }

    @Test
    public void spinningLedWithNoTurnsJustTurnsOff() throws Exception {
        ApiService apiService = mock(ApiService.class);
        Sleeper sleeper = mock(Sleeper.class);
        LedControllerImpl controller = new LedControllerImpl(apiService, sleeper);

        when(apiService.setLight(anyInt(), anyString(), anyBoolean())).thenReturn(new JSONObject());

        controller.spinningLed("#00ff00", 0, 10L);

        verify(apiService).setLight(20, "#000000", false);
        verify(apiService).setLight(21, "#000000", false);
        verify(apiService).setLight(22, "#000000", false);
        verify(apiService).setLight(23, "#000000", false);
        verify(apiService).setLight(24, "#000000", false);
        verify(apiService).setLight(25, "#000000", false);
        verify(apiService).setLight(26, "#000000", false);
        verify(apiService).setLight(27, "#000000", false);
        verify(apiService, never()).setLight(anyInt(), eq("#00ff00"), eq(true));
        verifyNoMoreInteractions(apiService);
        verifyNoMoreInteractions(sleeper);
    }

    @Test
    public void showTimeDoesNothingWhenNoGroupLeds() throws Exception {
        ApiService apiService = mock(ApiService.class);
        LedControllerImpl controller = new LedControllerImpl(apiService);

        // lights[] leer
        JSONArray lights = new JSONArray();
        when(apiService.getLights()).thenReturn(new JSONObject().put("lights", lights));

        controller.showTime(12, 0, 0);

        // Keine LED → kein setLight()
        verify(apiService, never()).setLight(anyInt(), anyString(), anyBoolean());
    }

    @Test
    public void showTimeSetsCorrectColorsForSimpleTime() throws Exception {
        ApiService apiService = mock(ApiService.class);
        LedControllerImpl controller = new LedControllerImpl(apiService);

        // Gruppe simulieren: LED IDs 20–27
        JSONArray lights = new JSONArray();
        for (int id = 20; id <= 27; id++) {
            lights.put(new JSONObject()
                    .put("id", id)
                    .put("color", "#000")
                    .put("on", true)
                    .put("groupByGroup", new JSONObject().put("name", "G")));
        }
        when(apiService.getLights()).thenReturn(new JSONObject().put("lights", lights));

        controller.showTime(3, 0, 0);  // 03:00:00

        // Anzahl LEDs
        int ledCount = lights.length();

        // Indexberechnung folgt deiner Implementierung:
        int hourIndex   = controller.mapHourToIndex(3, 0, ledCount);   // sollte 3 sein
        int minuteIndex = controller.mapToIndex(0, 60, ledCount);      // 0
        int secondIndex = controller.mapToIndex(0, 60, ledCount);      // 0

        int hourId   = 20 + hourIndex;
        int minuteId = 20 + minuteIndex;
        int secondId = 20 + secondIndex;

        // Erwartete Farben:
        // Minuten + Sekunden auf derselben LED → Grün + Blau = Cyan
        verify(apiService).setLight(hourId, "#ff0000", true);   // rot
        verify(apiService).setLight(minuteId, "#00ffff", true); // cyan

        // Alle LEDs wurden gesetzt
        verify(apiService, times(ledCount)).setLight(anyInt(), anyString(), anyBoolean());
    }

    @Test
    public void showTimeWhiteWhenAllOnSameLed() throws Exception {
        ApiService apiService = mock(ApiService.class);
        LedControllerImpl controller = new LedControllerImpl(apiService);

        // Gruppe simulieren
        JSONArray lights = new JSONArray();
        for (int id = 20; id <= 27; id++) {
            lights.put(new JSONObject()
                    .put("id", id)
                    .put("color", "#000")
                    .put("on", true)
                    .put("groupByGroup", new JSONObject().put("name", "G")));
        }
        when(apiService.getLights()).thenReturn(new JSONObject().put("lights", lights));

        controller.showTime(0, 0, 0);  // H=M=S=0

        // LED 20 ist Index 0
        verify(apiService).setLight(20, "#ffffff", true);
        verify(apiService, times(8)).setLight(anyInt(), anyString(), anyBoolean());
    }


}
