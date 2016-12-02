package com.alan.washer.washercleaner;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class AlarmNotification {

    private static final String NOTIFICATION_TAG = "Alarm";

    public static void notify(final Context context, final String info, Class activityClass) {
        final String title = "Washer";
        int icon = R.drawable.appicon;

        Intent intent = new Intent(context,activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

}
