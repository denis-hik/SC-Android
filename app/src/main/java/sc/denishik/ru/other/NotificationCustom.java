package sc.denishik.ru.other;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import sc.denishik.ru.MainActivity;
import sc.denishik.ru.R;

public class NotificationCustom {
    static public Notification showNotification(String channelId, Context context, String wsStatus, String speed) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId)
                    .setContentTitle("SC")
                    .setContentText("ws: " + wsStatus + " speed: " + speed)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.app_icon)
                    .setAutoCancel(false);
        } else {
            builder = new Notification.Builder(context).setContentTitle("SC")
                    .setContentText("ws: " + wsStatus + " speed: " + speed)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.app_icon)
                    .setAutoCancel(false);
        }

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.build();
        } else {
            // For devices running older versions than Oreo
            notification = builder.getNotification();
        }

        return notification;
    }
}
