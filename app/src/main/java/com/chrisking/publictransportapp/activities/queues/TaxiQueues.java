package com.chrisking.publictransportapp.activities.queues;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chrisking.publictransportapp.services.geofencing.GeofencePersistence;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.city.CityPersistence;
import com.chrisking.publictransportapp.classes.City;
import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class TaxiQueues extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    public static final String TAXI_QUEUES_SETUP_STORAGE_KEY = "taxiqueuessetup";

    private Button mSetupButton;
    private Button mTurnOffButton;
    private Button mAdvancedButton;
    private LinearLayout mOptionsLayout;
    private ImageView mQueueImage;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_taxi_queues, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_activity_taxi_queues);
        FlurryAgent.logEvent("TaxiQueues");

        init(getView(), getActivity());
    }

    @Override
    public void onResume()
    {
        super.onResume();

        showOrHideSetupButton(getActivity());
    }

    private void init (final View view, final Activity activity){
        mQueueImage = (ImageView) view.findViewById(R.id.queueImage);
        mQueueImage.getBackground().setColorFilter(ContextCompat.getColor(activity, R.color.colorLightPurple), PorterDuff.Mode.SRC_ATOP);

        mSetupButton = (Button) view.findViewById(R.id.setup);
        mSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent push = new Intent(getActivity(), TaxiQueueSetupActivity.class);

                startActivity(push);
            }
        });

        mTurnOffButton = (Button) view.findViewById(R.id.turnOffButton);
        mTurnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildGoogleApiClient();
            }
        });

        mAdvancedButton = (Button) view.findViewById(R.id.advancedButton);
        mAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent push = new Intent(getActivity(), TaxiQueueConfigureActivity.class);

                startActivity(push);
            }
        });

        mOptionsLayout = (LinearLayout) view.findViewById(R.id.optionsLayout);

        City savedCity = new CityPersistence(activity).getSavedCity();

        TextView taxiQueuesDescriptionTextView = (TextView) view.findViewById(R.id.taxiQueuesDescriptionTextView);
        taxiQueuesDescriptionTextView.setText(savedCity.getTaxiName() + " " + getString(R.string.taxiqueues_description));

        showOrHideSetupButton(activity);
    }

    private void showOrHideSetupButton(final Activity activity){
        SharedPreferences settings = activity.getSharedPreferences("settings", 0);
        if (settings.getBoolean(TaxiQueues.TAXI_QUEUES_SETUP_STORAGE_KEY, false))
        {
            mSetupButton.setVisibility(View.INVISIBLE);
            mOptionsLayout.setVisibility(View.VISIBLE);
        }
        else{
            mSetupButton.setVisibility(View.VISIBLE);
            mOptionsLayout.setVisibility(View.INVISIBLE);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private void turnOfMonitoring(){
        SharedPreferences settings = getActivity().getSharedPreferences("settings", 0);

        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(TaxiQueues.TAXI_QUEUES_SETUP_STORAGE_KEY, false);
        editor.commit();

        showOrHideSetupButton(getActivity());
    }

    private void removeGeofences() {
        final GeofencePersistence geofencePersistence = new GeofencePersistence(getActivity());

        List<NamedGeofence> namedGeofences = geofencePersistence.getNamedGeofences();
        for (NamedGeofence namedFence : namedGeofences){
            if (namedFence.isSubscribed){
                FirebaseMessaging.getInstance().unsubscribeFromTopic(namedFence.id);
            }
        }
        List<String> geofenceIds = geofencePersistence.getGeofenceIds();

        PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(
            mGoogleApiClient,
            geofenceIds
        );

        result.setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    showSuccessfullyDisabledDialog();
                } else {
                    showFailedToDisableDialog();
                }

                geofencePersistence.clearAll();
                turnOfMonitoring();
            }
        });
    }

    public void showSuccessfullyDisabledDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_success)
                .setMessage(R.string.taxiqueues_removed_success_description)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public void showFailedToDisableDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_error)
                .setMessage(R.string.taxiqueues_removed_failed_description)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        removeGeofences();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
