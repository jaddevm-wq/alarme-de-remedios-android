package com.jadde.alarmemedicamentos;

import org.json.JSONException;
import org.json.JSONObject;

public final class Dose {
    public static final String BLACK = "black";
    public static final String GREEN = "green";
    public static final String PURPLE = "purple";
    public static final String OINTMENT = "ointment";

    public final int id;
    public final long triggerAtMillis;
    public final String time;
    public final String medicine;
    public final String color;
    public final int phase;

    public Dose(int id, long triggerAtMillis, String time, String medicine, String color, int phase) {
        this.id = id;
        this.triggerAtMillis = triggerAtMillis;
        this.time = time;
        this.medicine = medicine;
        this.color = color;
        this.phase = phase;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("triggerAtMillis", triggerAtMillis);
        json.put("time", time);
        json.put("medicine", medicine);
        json.put("color", color);
        json.put("phase", phase);
        return json;
    }

    public static Dose fromJson(JSONObject json) throws JSONException {
        return new Dose(
                json.getInt("id"),
                json.getLong("triggerAtMillis"),
                json.getString("time"),
                json.getString("medicine"),
                json.getString("color"),
                json.getInt("phase")
        );
    }
}
