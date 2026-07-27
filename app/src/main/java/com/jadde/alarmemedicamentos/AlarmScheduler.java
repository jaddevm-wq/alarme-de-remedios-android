package com.jadde.alarmemedicamentos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class AlarmScheduler {
    private static final String PREFS = "scheduled_medicines";
    private static final String KEY_DOSES = "doses";

    private AlarmScheduler() {
    }

    public static int scheduleAll(Context context, List<Dose> doses) {
        cancelAll(context);
        long now = System.currentTimeMillis();
        int scheduled = 0;
        for (Dose dose : doses) {
            if (dose.triggerAtMillis > now) {
                schedule(context, dose);
                scheduled++;
            }
        }
        save(context, doses);
        return scheduled;
    }

    public static void schedule(Context context, Dose dose) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }

        PendingIntent pendingIntent = pendingIntent(context, dose, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dose.triggerAtMillis,
                    pendingIntent
            );
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, dose.triggerAtMillis, pendingIntent);
        }
    }

    public static void cancelAll(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            for (Dose dose : load(context)) {
                PendingIntent pendingIntent = pendingIntent(
                        context,
                        dose,
                        PendingIntent.FLAG_NO_CREATE
                );
                if (pendingIntent != null) {
                    manager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_DOSES)
                .apply();
    }

    public static void rescheduleFuture(Context context) {
        long now = System.currentTimeMillis();
        for (Dose dose : load(context)) {
            if (dose.triggerAtMillis > now) {
                schedule(context, dose);
            }
        }
    }

    public static List<Dose> load(Context context) {
        List<Dose> doses = new ArrayList<>();
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = preferences.getString(KEY_DOSES, "[]");
        try {
            JSONArray json = new JSONArray(raw);
            for (int i = 0; i < json.length(); i++) {
                doses.add(Dose.fromJson(json.getJSONObject(i)));
            }
        } catch (JSONException ignored) {
            // Invalid local state is safely ignored.
        }
        return doses;
    }

    private static void save(Context context, List<Dose> doses) {
        JSONArray json = new JSONArray();
        for (Dose dose : doses) {
            try {
                json.put(dose.toJson());
            } catch (JSONException ignored) {
                // A single invalid item must not block the remaining alarms.
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_DOSES, json.toString())
                .apply();
    }

    private static PendingIntent pendingIntent(Context context, Dose dose, int baseFlags) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("dose_id", dose.id);
        intent.putExtra("time", dose.time);
        intent.putExtra("medicine", dose.medicine);
        intent.putExtra("color", dose.color);
        intent.putExtra("phase", dose.phase);

        int flags = baseFlags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, dose.id, intent, flags);
    }
}
