package com.mercury.alarmer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class AlarmController {

    public static final String PREFERENCES = "MainPrefs";
    public static final String PREF_TITLE = "MainPrefs.title";
    public static final String PREF_DESCRIPTION = "MainPrefs.description";
    public static final String PREF_YEAR = "MainPrefs.year";
    public static final String PREF_MONTH = "MainPrefs.month";
    public static final String PREF_DAY = "MainPrefs.day";
    public static final String PREF_HOUR = "MainPrefs.hour";
    public static final String PREF_MINUTE = "MainPrefs.minute";
    public static final String PREF_SET = "MainPrefs.isSet";

    private static AlarmController instance;
    private Calendar calendar;
    private static Context context;
    private String title, description;
    private SharedPreferences preferences;
    private boolean alarmSet;
    private AlarmManager alarmManager;

    private AlarmController(Context c) {
        calendar = Calendar.getInstance();
        preferences = c.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        title = preferences.getString(PREF_TITLE, "");
        description = preferences.getString(PREF_DESCRIPTION, "");
        alarmSet = preferences.getBoolean(PREF_SET, false);
        if (alarmSet) {
            setDate(
                    preferences.getInt(PREF_YEAR, getYear()),
                    preferences.getInt(PREF_MONTH, getMonth()),
                    preferences.getInt(PREF_DAY, getDay())
            );
            setTime(
                    preferences.getInt(PREF_HOUR, getHour()),
                    preferences.getInt(PREF_MINUTE, getMinute())
            );
        }
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static AlarmController getInstance(Context c) {
        context = c;
        if (instance == null)
            instance = new AlarmController(c);
        return instance;
    }

    private String getMonthName(int month) {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.MONTH, month);
        return cal1.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }

    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return calendar.get(Calendar.MINUTE);
    }

    public String getDate() {
        return String.format("%d %s %d",
                calendar.get(Calendar.DAY_OF_MONTH),
                getMonthName(calendar.get(Calendar.MONTH)),
                calendar.get(Calendar.YEAR)
        );
    }

    public String getTime() {
        return String.format("%d:%d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTime(int hour, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void saveChanges() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_HOUR, getHour());
        editor.putInt(PREF_MINUTE, getMinute());
        editor.putInt(PREF_YEAR, getYear());
        editor.putInt(PREF_MONTH, getMonth());
        editor.putInt(PREF_DAY, getDay());
        editor.putString(PREF_TITLE, getTitle());
        editor.putString(PREF_DESCRIPTION, getDescription());
        editor.putBoolean(PREF_SET, alarmSet);
        editor.apply();
    }

    public void setDate(int year, int month, int day) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAlarmSet() {
        return alarmSet;
    }

    public void setAlarm() {
        if (isAlarmSet()) {
            alarmManager.cancel(getPendIntent(context));
            Toast.makeText(context, "Notification updated!", Toast.LENGTH_LONG).show();
        } else {
            alarmSet = true;
            Toast.makeText(context, "Notification created!", Toast.LENGTH_LONG).show();
        }
        saveChanges();
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), getPendIntent(context));

    }

    public static class AlarmReceiver extends BroadcastReceiver {

        public AlarmReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Alarm", "Oh Lord Jesus! It's a broadcast!!!");
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(android.R.drawable.sym_action_email);
            AlarmController controller = AlarmController.getInstance(context);
            mBuilder.setContentTitle(controller.getTitle());
            mBuilder.setContentText(controller.getDescription());
            PendingIntent pI = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
            mBuilder.setContentIntent(pI);
            mBuilder.setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, mBuilder.build());
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().clear().apply();
            wakeLock.release();
        }
    }

    public PendingIntent getPendIntent(Context c) {
        return PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), 0);
    }

    public boolean isTimeExpired() {
        return (calendar.getTimeInMillis() < System.currentTimeMillis());
    }

    public void reset() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        title = "";
        description = "";
        alarmSet = false;
    }
}
