package com.gabriele.nextstop;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Fermata {
    private String id;
    private String stopName;
    private double latitude;
    private double longitude;

    public Fermata(String id, String stopName, double latitude, double longitude) {
        this.id = id;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public static ArrayList<Fermata> fromJson(String jsonString) {
        ArrayList<Fermata> stopsList = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray stopsArray = root.getJSONArray("stops");

            for (int i = 0; i < stopsArray.length(); i++) {
                JSONObject stopObj = stopsArray.getJSONObject(i);
                String id = stopObj.getString("id");
                String stopName = stopObj.getString("stop_name");

                JSONObject geometry = stopObj.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                double longitude = coordinates.getDouble(0);
                double latitude = coordinates.getDouble(1);

                Fermata stop = new Fermata(id, stopName, latitude, longitude);
                stopsList.add(stop);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stopsList;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getStopName() {
        return stopName;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "\uD83D\uDCCD Fermata - " + stopName;
    }
}
