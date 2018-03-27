package com.chrisking.publictransportapp.services.geofencing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.chrisking.publictransportapp.classes.QueueState;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

/**
 * Created by ChrisKing on 2017/07/09.
 */

public class GeofenceNotificationReceiver extends BroadcastReceiver {
    public static final String YES_ACTION = "YES_ACTION";
    public static final String NO_ACTION = "NO_ACTION";
    public static final int SUBSCRIBE_NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String fenceId = intent.getStringExtra("fenceId");

        if (!action.equals(GeofenceTrasitionService.UNKNOWN_ACTION)){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            QueueState queueState = new QueueState();
            queueState.setUid(databaseReference.child("queues").push().getKey());
            queueState.setDate(new Date().getTime());
            queueState.setRankId(fenceId);
            if (action.equals(GeofenceTrasitionService.NORMAL_ACTION))
                queueState.setState("normal");
            else if (action.equals(GeofenceTrasitionService.LONG_ACTION))
                queueState.setState("long");
            databaseReference.child("queues").child(queueState.getUid()).setValue(queueState);
        }

        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        GeofencePersistence geofencePersistence = new GeofencePersistence(context);
        NamedGeofence namedGeofence = geofencePersistence.find(fenceId);
        if (!namedGeofence.isSubscribed && namedGeofence.numTimesAskedToSubscribe < 3) {
            String getNotifiedText = context.getString(R.string.taxiqueues_subscribe_to_rank) + " " + namedGeofence.name + "?";

            sendNotification(context, namedGeofence, getNotifiedText);

            namedGeofence.numTimesAskedToSubscribe += 1;
            geofencePersistence.addNamedGeofence(namedGeofence);
        }
    }

    private void sendNotification(Context context, NamedGeofence namedGeofence, String msg) {
        Intent yesIntent = new Intent(context, GeofenceNotificationSubscribeReceiver.class);
        yesIntent.setAction(YES_ACTION);
        yesIntent.putExtra("fenceId", namedGeofence.id);

        Intent noIntent = new Intent(context, GeofenceNotificationSubscribeReceiver.class);
        noIntent.setAction(NO_ACTION);
        noIntent.putExtra("fenceId", namedGeofence.id);

        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context, 0, noIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                SUBSCRIBE_NOTIFICATION_ID,
                createNotification(context, msg, pendingIntentYes, pendingIntentNo));
    }

    // Create a notification
    private Notification createNotification(Context context, String msg, PendingIntent yes, PendingIntent no) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder
                .setSmallIcon(R.drawable.sharetaxi24)
                .setColor(ContextCompat.getColor(context, R.color.colorDarkPurple))
                .setContentText(msg)
                .setContentIntent(null)
                .addAction(R.drawable.bus24, "yes", yes)
                .addAction(R.drawable.bus24, "no", no)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }
}
