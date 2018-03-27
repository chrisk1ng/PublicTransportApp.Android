package com.chrisking.publictransportapp.services.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.whereto.WhereToActivity;
import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.chrisking.publictransportapp.services.geofencing.GeofencePersistence;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ChrisKing on 2017/07/09.
 */

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String fenceId = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
            String state = remoteMessage.getData().get("state");

            Intent resultIntent = new Intent(this, WhereToActivity.class);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            NamedGeofence namedGeofence = new GeofencePersistence(this).find(fenceId);
            // Creating and sending Notification
            NotificationManager notificatioMng = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificatioMng.notify(0, createNotification(namedGeofence.name, message, state, resultPendingIntent));
        }
    }

    private Notification createNotification(String title, String msg, String state, PendingIntent resultPendingIntent) {
        int color = ContextCompat.getColor(this, R.color.colorDarkPurple);
        if (state.equals("long"))
            color = Color.RED;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.sharetaxi24)
                .setColor(color)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }
}
