package at.edu.c02.ledcontroller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    private static final long SPIN_SLEEP_MILLIS = 200L;
    /**
     * This is the main program entry point. TODO: add new commands when implementing additional features.
     */
    public static void main(String[] args) throws IOException {
        LedController ledController = new LedControllerImpl(new ApiServiceImpl());

        String input = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(!input.equalsIgnoreCase("exit"))
        {
            System.out.println("=== LED Controller ===");
            System.out.println("Enter 'demo' to send a demo request");
            System.out.println("Enter 'status' to query a specific LED");
            System.out.println("Enter 'groupstatus' to list all group LEDs");
            System.out.println("Enter 'setled' to set LED color");
            System.out.println("Enter 'spinningled' to start the spinning LED effect");
            System.out.println("Enter 'exit' to exit the program");
            System.out.print("> ");
            input = reader.readLine();
            if(input == null) {
                break;
            }
            if(input.equalsIgnoreCase("demo"))
            {
                ledController.demo();
            }
            else if (input.equalsIgnoreCase("status"))
            {
                System.out.println("Please specify LED ID:");
                System.out.print("> ");
                String idInput = reader.readLine();
                try {
                    int id = Integer.parseInt(idInput);
                    JSONObject light = ledController.getLight(id);
                    boolean isOn = light.getBoolean("on");
                    String color = light.getString("color");
                    System.out.println("LED " + light.getInt("id") + " is currently " + (isOn ? "on" : "off") + ". Color: " + color + ".");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid LED ID. Please enter a number.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            else if (input.equalsIgnoreCase("groupstatus"))
            {
                JSONArray groupLights = ledController.getGroupLeds();
                if (groupLights.length() == 0) {
                    System.out.println("No grouped LEDs found.");
                } else {
                    for (int i = 0; i < groupLights.length(); i++) {
                        JSONObject light = groupLights.getJSONObject(i);
                        boolean isOn = light.getBoolean("on");
                        String color = light.getString("color");
                        System.out.println("LED " + light.getInt("id") + " is currently " + (isOn ? "on" : "off") + ". Color: " + color + ".");
                    }
                }
            }
            else if (input.equalsIgnoreCase("setled")) {
                System.out.println("Which LED?");
                System.out.print("> ");
                int id;
                try {
                    id = Integer.parseInt(reader.readLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid LED ID.");
                    continue;
                }

                System.out.println("Which color?");
                System.out.print("> ");
                String color = reader.readLine();  // e.g. "#ff00"

                try {
                    ledController.setLed(id, color);
                    System.out.println("LED color set!");
                } catch (IOException e) {
                    System.out.println("Failed to set LED color: " + e.getMessage());
                }
            } else if (input.equalsIgnoreCase("spinningled")) {
                System.out.println("Which color?");
                System.out.print("> ");
                String color = reader.readLine();

                System.out.println("How many turns?");
                System.out.print("> ");
                String turnsInput = reader.readLine();
                int turns;
                try {
                    turns = Integer.parseInt(turnsInput);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number of turns.");
                    continue;
                }

                System.out.println("Starting SpinningLed effect...");
                try {
                    ledController.spinningLed(color, turns, SPIN_SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    System.out.println("SpinningLed effect interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
