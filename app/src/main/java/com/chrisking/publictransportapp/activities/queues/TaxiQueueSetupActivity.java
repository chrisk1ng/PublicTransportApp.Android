package com.chrisking.publictransportapp.activities.queues;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.chrisking.publictransportapp.services.geofencing.GeofencePersistence;
import com.chrisking.publictransportapp.services.geofencing.GeofenceTrasitionService;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.city.CityPersistence;
import com.chrisking.publictransportapp.classes.City;
import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import transportapisdk.StopQueryOptions;
import transportapisdk.TransportApiClient;
import transportapisdk.TransportApiClientSettings;
import transportapisdk.TransportApiResult;
import transportapisdk.models.Stop;

public class TaxiQueueSetupActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private TextView mProgressText;
    private static final int GEOFENCE_RADIUS = 100;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private LatLng mUserLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private GeofencePersistence mGeofencePersistence;
    private City mSavedCity;
    private boolean mSavedNearbyStops = false;
    private boolean mSavedHomeStops = false;
    private boolean mSavedWorkStops = false;
    private boolean mHasNearbyStops = false;
    private String mGeofenceType = GeofencePersistence.LOCATION_TYPE;

    private void init(){
        mProgressText = (TextView) findViewById(R.id.progressTextView);
        mGeofencePersistence = new GeofencePersistence(this);
        mSavedCity = new CityPersistence(this).getSavedCity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_queue_setup);

        init();

        mProgressText.setText(R.string.taxiqueues_fetchinglocation);

        mGeofencePersistence.clearType(GeofencePersistence.LOCATION_TYPE);

        checkLocationPermission();

        buildGoogleApiClient();
    }

    public void showRateDialog(@StringRes int title, @StringRes int message){
        showRateDialog(getString(title), getString(message));
    }

    public void showRateDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface arg0) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationServices();
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
            else{
                showRateDialog(R.string.title_error, R.string.location_required);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private void requestLocation(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void startLocationServices(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        requestLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    getActivity(), 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mUserLocation = latLng;
        mGeofenceType = GeofencePersistence.LOCATION_TYPE;

        mProgressText.setText(R.string.taxiqueues_fetchingstops);

        new GetStopsTask().execute();

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void SetupGeofences(List<Stop> stops){
        mProgressText.setText(R.string.taxiqueues_loadgingeofences);

        if (!stops.isEmpty()){
            mHasNearbyStops = true;
        }

        for (Stop stop : stops) {
            Geofence geofence = createGeofence(stop);
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );

            NamedGeofence geo = new NamedGeofence();
            geo.id = stop.getId();
            geo.name = stop.getName();
            geo.latitude = stop.getGeometry().getCoordinates()[1];
            geo.longitude = stop.getGeometry().getCoordinates()[0];
            geo.type = mGeofenceType;

            addGeofence(geofenceRequest, geo);
        }

        if (mGeofenceType.equals(GeofencePersistence.LOCATION_TYPE)){
            mSavedNearbyStops = true;
        }
        else if (mGeofenceType.equals(GeofencePersistence.HOME_TYPE)){
            mSavedHomeStops = true;
        }
        else if (mGeofenceType.equals(GeofencePersistence.WORK_TYPE)){
            mSavedWorkStops = true;
        }

        SetupComplete();
    }

    private void SetupComplete(){
        if (mSavedNearbyStops && !mSavedHomeStops && !mSavedWorkStops){
            LoadHomeGeofences();
        }
        else if (mSavedNearbyStops && mSavedHomeStops && !mSavedWorkStops){
            LoadWorkGeofences();
        }
        else {
            if (mGeofencePersistence.getGeofenceIds().isEmpty() && !mHasNearbyStops){
                showRateDialog(R.string.title_error, R.string.not_supported_tip_description);
            }
            else {
                SharedPreferences settings = this.getSharedPreferences("settings", 0);

                SharedPreferences.Editor editor = settings.edit();

                editor.putBoolean(TaxiQueues.TAXI_QUEUES_SETUP_STORAGE_KEY, true);
                editor.commit();

                String setupCompleteDescription = getString(R.string.taxiqueues_setupcomplete_1) + " " + mSavedCity.getTaxiName() + " " + getString(R.string.taxiqueues_setupcomplete_2);
                showRateDialog(getString(R.string.title_success), setupCompleteDescription);
            }
        }
    }

    private void LoadWorkGeofences(){
        String workLocation = getActivity().getSharedPreferences("settings", 0).getString("worklocation", null);

        if (workLocation != null) {
            String[] workLatlong =  workLocation.split(",");
            double workLatitude = Double.parseDouble(workLatlong[0]);
            double workLongitude = Double.parseDouble(workLatlong[1]);

            mUserLocation = new LatLng(workLatitude, workLongitude);
            mGeofenceType = GeofencePersistence.WORK_TYPE;

            mProgressText.setText(R.string.taxiqueues_fetchingworkstops);

            new GetStopsTask().execute();
        }
        else{
            mSavedWorkStops = true;

            SetupComplete();
        }
    }

    private void LoadHomeGeofences(){
        String homeLocation = getActivity().getSharedPreferences("settings", 0).getString("homelocation", null);

        if (homeLocation != null) {
            String[] homeLatlong =  homeLocation.split(",");
            double homeLatitude = Double.parseDouble(homeLatlong[0]);
            double homeLongitude = Double.parseDouble(homeLatlong[1]);

            mUserLocation = new LatLng(homeLatitude, homeLongitude);
            mGeofenceType = GeofencePersistence.HOME_TYPE;

            mProgressText.setText(R.string.taxiqueues_fetchinghomestops);

            new GetStopsTask().execute();
        }
        else{
            mSavedHomeStops = true;

            SetupComplete();
        }
    }

    private void removeGeofences() {
        List<String> geofenceIds = mGeofencePersistence.getGeofenceIds();

        PendingResult<Status> result = LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                geofenceIds
        );

        result.setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {
                mGeofencePersistence.clearAll();
            }
        });
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        geoFencePendingIntent = PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );

        return geoFencePendingIntent;
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(final GeofencingRequest request, final NamedGeofence geo) {
        if (checkLocationPermission()) {
            PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            );

            result.setResultCallback(new ResultCallback<Status>() {

                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        // 4. If successful, save the geofence
                        mGeofencePersistence.addNamedGeofence(geo);
                    } else {
                    }
                }
            });
        }
    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    public Geofence createGeofence(Stop stop) {
        LatLng coordinate = new LatLng(stop.getGeometry().getCoordinates()[1], stop.getGeometry().getCoordinates()[0]);

        return new Geofence.Builder()
                .setRequestId(stop.getId())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(coordinate.latitude, coordinate.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    protected TransportApiClient defaultClient = new TransportApiClient(new TransportApiClientSettings(ApplicationExtension.ClientId(), ApplicationExtension.ClientSecret(), 60, ApplicationExtension.UniqueContextId()));

    private class GetStopsTask extends AsyncTask<Void, Void, TransportApiResult<List<Stop>>> {

        protected TransportApiResult<List<Stop>> doInBackground(Void... params){
            List<String> onlyModes = new ArrayList<String>();
            onlyModes.add("sharetaxi");

            StopQueryOptions options = new StopQueryOptions(null, null, onlyModes, null, null, false, 33, 0, null);

            TransportApiResult<List<Stop>> stops = defaultClient.getStopsNearby(options, mUserLocation.latitude, mUserLocation.longitude, 50000);

            return stops;
        }

        protected void onPostExecute(TransportApiResult<List<Stop>> stops) {

            if (stops.isSuccess == false) {
                removeGeofences();
                showRateDialog(R.string.title_error, R.string.error_check_internet);
                return;
            }
            else{
                SetupGeofences(stops.data);
            }
        }
    }
}
