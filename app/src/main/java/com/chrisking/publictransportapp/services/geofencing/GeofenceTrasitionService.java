package com.chrisking.publictransportapp.services.geofencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

/**
 * Created by ChrisKing on 2017/07/01.
 */

public class GeofenceTrasitionService extends IntentService {
    private static final String TAG = GeofenceTrasitionService.class.getSimpleName();
    public static final int GEOFENCE_NOTIFICATION_ID = 0;
    public static final String UNKNOWN_ACTION = "UNKNOWN_ACTION";
    public static final String NORMAL_ACTION = "NORMAL_ACTION";
    public static final String LONG_ACTION = "LONG_ACTION";
    private GeofencePersistence mGeoFencePersistence;

    public GeofenceTrasitionService() {
        super(TAG);

        mGeoFencePersistence = new GeofencePersistence(ApplicationExtension.getContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        // Retrieve GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Create a detail message with Geofences received
            getGeofenceTrasitionDetails(triggeringGeofences );
        }
    }

    // Create a detail message with Geofences received
    private void getGeofenceTrasitionDetails(List<Geofence> triggeringGeofences) {
        for ( Geofence geofence : triggeringGeofences ) {
            NamedGeofence geo = mGeoFencePersistence.find(geofence.getRequestId());
            if (geo != null) {

                String message = "How are the queues at " + geo.name + "?";

                sendNotification(geo, message);
            }
        }
    }

    // Send a notification
    private void sendNotification(NamedGeofence namedGeofence, String msg) {
        Intent dismissIntent = new Intent(this, GeofenceNotificationReceiver.class);
        dismissIntent.setAction(UNKNOWN_ACTION);

        Intent normalQueuesIntent = new Intent(this, GeofenceNotificationReceiver.class);
        normalQueuesIntent.setAction(NORMAL_ACTION);
        normalQueuesIntent.putExtra("fenceId", namedGeofence.id);

        Intent longQueuesIntent = new Intent(this, GeofenceNotificationReceiver.class);
        longQueuesIntent.setAction(LONG_ACTION);
        longQueuesIntent.putExtra("fenceId", namedGeofence.id);

        PendingIntent pendingIntentDismissNotification = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentNormalQueues = PendingIntent.getBroadcast(this, 0, normalQueuesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentLongQueues = PendingIntent.getBroadcast(this, 0, longQueuesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, pendingIntentDismissNotification, pendingIntentNormalQueues, pendingIntentLongQueues));
    }

    // Create a notification
    private Notification createNotification(String msg, PendingIntent dismiss, PendingIntent normalQueues, PendingIntent longQueues) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.sharetaxi24)
                .setColor(ContextCompat.getColor(this, R.color.colorDarkPurple))
                .setContentText(msg)
                .setContentIntent(null)
                .addAction(R.drawable.rail24, "Not sure", dismiss)
                .addAction(R.drawable.bus24, "Normal", normalQueues)
                .addAction(R.drawable.coach24, "Long!", longQueues)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}
