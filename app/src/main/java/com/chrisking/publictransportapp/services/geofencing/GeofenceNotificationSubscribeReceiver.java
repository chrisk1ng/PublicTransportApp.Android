package com.chrisking.publictransportapp.services.geofencing;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by ChrisKing on 2017/07/09.
 */

public class GeofenceNotificationSubscribeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String fenceId = intent.getStringExtra("fenceId");
        GeofencePersistence geofencePersistence = new GeofencePersistence(context);
        NamedGeofence namedGeofence = geofencePersistence.find(fenceId);

        if (action.equals(GeofenceNotificationReceiver.YES_ACTION)) {
            namedGeofence.isSubscribed = true;
            FirebaseMessaging.getInstance().subscribeToTopic(fenceId);
        }
        else {
            namedGeofence.isSubscribed = false;
            namedGeofence.numTimesAskedToSubscribe += 1;
        }

        geofencePersistence.addNamedGeofence(namedGeofence);

        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
