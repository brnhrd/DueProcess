package com.bernhardgruendling.dueprocess.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.ui.MainActivity;


public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    public static final int DEFAULT_NOTIFICATION_ID = 1;
    private static final String CHANNEL_NAME = "Default";

    public static void showNotification(
            Context context, String title, String msg, int notificationId, Intent intent) {
        if (intent == null) {
            intent = new Intent(context, MainActivity.class);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = getNotificationBuilder(context)
                .setContentTitle(title)
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_app)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentIntent(PendingIntent.getActivity(context, -1,
                        intent, 0))
                .build();
        notificationManager.notify(notificationId, notification);
    }

    private static NotificationCompat.Builder getNotificationBuilder(Context context) {
        createDefaultNotificationChannel(context);
        return new NotificationCompat.Builder(context, CHANNEL_NAME);
    }

    private static void createDefaultNotificationChannel(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = context.getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(CHANNEL_NAME,
                appName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

}