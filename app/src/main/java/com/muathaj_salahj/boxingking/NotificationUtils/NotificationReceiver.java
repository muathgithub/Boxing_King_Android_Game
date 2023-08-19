package com.muathaj_salahj.boxingking.NotificationUtils;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.muathaj_salahj.boxingking.Activities.GameActivity;
import com.muathaj_salahj.boxingking.R;

import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    Random random = new Random();

    String[] motivationMessages = {
            "Time to step into the ring! Get ready to unleash your boxing skills!",
            "Embrace the challenge! Rise above your limits and become an unstoppable force!",
            "Train hard, fight smart! Success comes to those who are disciplined.",
            "Every punch brings you closer to victory! Keep pushing yourself!",
            "You've got the heart of a champion! Keep pushing your limits and never give up!",
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create an intent for launching an activity when the notification is clicked
        Intent notificationIntent = new Intent(context, GameActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification with random motivation message from the messages array above
        Notification notification = new NotificationCompat.Builder(context, context.getResources().getString(R.string.chanel_id))
                .setContentTitle("Boxing Motivation")
                .setContentText(motivationMessages[random.nextInt(motivationMessages.length)])
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(context.getResources().getInteger(R.integer.notification_id), notification);
    }
}
