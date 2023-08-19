package com.muathaj_salahj.boxingking.NotificationUtils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.muathaj_salahj.boxingking.R;

public class NotificationScheduler {

    // This function schedules a repeating notification.
    public static void scheduleRepeatingNotification(Context context) {

        // getting the number of hours for the notifications repeating process
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int hours_number = sharedPref.getInt(context.getResources().getString(R.string.hours_number), context.getResources().getInteger(R.integer.hours_number));

        // Interval for repeating notification in milliseconds
        long interval_millis = (long) hours_number * 60 * 1000; // This calculation is for minutes for the purpouse of testing

        // Create notification channel for the notification
        createNotificationChannel(context);

        // Create a broadcast Intent and a PendingIntent for the NotificationReceiver class
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Schedule the repeating notification using the AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval_millis, interval_millis, pendingIntent);
    }


     // This function creates a notification channel.
    private static void createNotificationChannel(Context context) {
        // getting the chanel info from the from the application resources settings_defaults.xml
        CharSequence name = context.getResources().getString(R.string.chanel_name);
        String description = context.getResources().getString(R.string.chanel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // Create a new notification channel with the specified ID, name, and importance
        NotificationChannel channel = new NotificationChannel(context.getResources().getString(R.string.chanel_id), name, importance);

        // Set the channel description
        channel.setDescription(description);

        // Get the NotificationManager system service
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        // Create the notification channel
        notificationManager.createNotificationChannel(channel);
    }


     // This function cancel the repeating notification.
    public static void cancelRepeatingNotification(Context context) {
        // Create a broadcast Intent and a PendingIntent for the NotificationReceiver class
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel the repeating notification
        alarmManager.cancel(pendingIntent);

        // Cancel the PendingIntent
        pendingIntent.cancel();
    }
}

