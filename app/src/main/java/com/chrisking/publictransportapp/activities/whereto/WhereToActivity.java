package com.chrisking.publictransportapp.activities.whereto;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chrisking.publictransportapp.activities.journeyoptions.JourneyOptionsActivity;
import com.chrisking.publictransportapp.activities.plancommute.PlanCommuteActivity;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.search.SearchActivity;
import com.chrisking.publictransportapp.classes.AppRater;
import com.chrisking.publictransportapp.classes.QueueState;
import com.chrisking.publictransportapp.classes.TaxiPrompter;
import com.chrisking.publictransportapp.classes.TripShare;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
import com.chrisking.publictransportapp.services.location.LocationMonitoringService;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.Date;
import java.util.List;
import transportapisdk.AgencyQueryOptions;
import transportapisdk.TransportApiClient;
import transportapisdk.TransportApiClientSettings;
import transportapisdk.TransportApiResult;
import transportapisdk.models.Agency;
import transportapisdk.models.Profile;
import transportapisdk.models.TimeType;

public class WhereToActivity extends Fragment implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener
{
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private GoogleMap mMap;
    private SharedPreferences mPrefs;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Button mDoneButton;
    private LatLng mStartLocation;
    private LatLng mStoredUserLocation;
    private LatLng endLocation;
    private LinearLayout mWhereToLayout;
    private LinearLayout mFilterButton;
    private ImageView mFilterImageView;
    private Profile mProfile = Profile.ClosestToTime;
    private TimeType mTimeType = TimeType.DepartAfter;
    private String mTime = null;
    private Button mRegionSupportButton;
    private ImageView mHomeImageView;
    private ImageView mWorkImageView;
    private LinearLayout mActiveTripLayout;
    private Button mTripShareStopButton;

    private void displayTip(){
        SharedPreferences prefs = getActivity().getSharedPreferences("tips", 0);
        if (prefs.getBoolean("planjourneytip", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("planjourneytip", true);
        editor.commit();

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.where_to_tip_welcome)
                .setMessage(R.string.where_to_tip_description)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void displayMyCommuteNotSetupTip(){

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.where_to_my_commute_not_setup_header)
                .setMessage(R.string.where_to_my_commute_not_setup_description)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                        fragmentManager.beginTransaction()
                                .add(R.id.content_frame, new PlanCommuteActivity())
                                .addToBackStack(getString(R.string.title_activity_plan_commute))
                                .commit();
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        showOnTrip(false);
    }


    private void init(final View view, final Activity activity)
    {
        mPrefs = getActivity().getSharedPreferences("settings", 0);

        mHomeImageView = (ImageView) view.findViewById(R.id.homeImageView);
        mHomeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String homeLocation = mPrefs.getString("homelocation", null);

                if (homeLocation != null) {
                    String[] homeLatlong =  homeLocation.split(",");
                    double homeLatitude = Double.parseDouble(homeLatlong[0]);
                    double homeLongitude = Double.parseDouble(homeLatlong[1]);

                    endLocation = new LatLng(homeLatitude, homeLongitude);

                    planJourney();
                }
                else{
                    displayMyCommuteNotSetupTip();
                }
            }
        });

        mWorkImageView = (ImageView) view.findViewById(R.id.workImageView);
        mWorkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String workLocation = mPrefs.getString("worklocation", null);

                if (workLocation != null) {
                    String[] workLatlong =  workLocation.split(",");
                    double workLatitude = Double.parseDouble(workLatlong[0]);
                    double workLongitude = Double.parseDouble(workLatlong[1]);

                    endLocation = new LatLng(workLatitude, workLongitude);

                    planJourney();
                }
                else{
                    displayMyCommuteNotSetupTip();
                }
            }
        });

        mFilterImageView = (ImageView) view.findViewById(R.id.filterImage);
        mRegionSupportButton = (Button) view.findViewById(R.id.regionSupport);
        mRegionSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shortcuts.displayRegionUnSupportedTip(getActivity());
            }
        });

        mFilterButton = (LinearLayout) view.findViewById(R.id.filter) ;
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("Filter Trip Planner");
                startActivityForResult(new Intent(activity, FilterOverlay.class), 1);
                activity.overridePendingTransition(R.anim.slide_up, R.anim.stay);
            }
        });

        mWhereToLayout = (LinearLayout) view.findViewById(R.id.whereToLayout);
        mWhereToLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent push = new Intent(activity, SearchActivity.class);

                FlurryAgent.logEvent("Search");
                startActivityForResult(push, 2);
            }
        });

        mActiveTripLayout = (LinearLayout) view.findViewById(R.id.activeTripLayout);
        mActiveTripLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = ((ApplicationExtension) getActivity().getApplicationContext()).getTripShareId();

                sendShareLink(uid);
            }
        });

        mTripShareStopButton = (Button) view.findViewById(R.id.tripShareStopButton);
        mTripShareStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocationMonitoringService.class);
                getActivity().stopService(intent);
                showOnTrip(true);
            }
        });

        mDoneButton = (Button) view.findViewById(R.id.done);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endLocation = mMap.getCameraPosition().target;

                planJourney();
            }
        });


    }

    private void planJourney(){
        if (mStartLocation == null){
            showNoStartLocationAlert();
        }
        else {
            Intent push = new Intent(getActivity(), JourneyOptionsActivity.class);
            push.putExtra("startLocation", mStartLocation);
            push.putExtra("endLocation", endLocation);
            if (mProfile == Profile.FewestTransfers)
                push.putExtra("Profile", "FewestTransfers");
            else
                push.putExtra("Profile", "ClosestToTime");

            if (mTimeType == TimeType.ArriveBefore)
                push.putExtra("TimeType", "ArriveBefore");
            else
                push.putExtra("TimeType", "DepartAfter");

            if (mTime != null)
                push.putExtra("Time", mTime);

            FlurryAgent.logEvent("PlanJourney");
            startActivity(push);
        }
    }

    public void sendShareLink(final String uid){

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://insta.trip?uid=" + uid))
                .setDynamicLinkDomain("enc6m.app.goo.gl")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        Uri dynamicLinkUri;

                        if (task.isSuccessful()) {
                            // Short link created
                            //Uri flowchartLink = task.getResult().getPreviewLink();

                            dynamicLinkUri = task.getResult().getShortLink();

                        } else {
                            // Error

                            DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                                    .setLink(Uri.parse("https://insta.trip?uid=" + uid))
                                    .setDynamicLinkDomain("enc6m.app.goo.gl")
                                    // Open links with this app on Android
                                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                                    // Open links with com.example.ios on iOS
                                    //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                                    .buildDynamicLink();

                            dynamicLinkUri = dynamicLink.getUri();
                        }

                        String shareText = getResources().getString(R.string.trip_share_share_text) + dynamicLinkUri.toString();
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "InstaTrip");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                });
    }

    private void showOnTrip(boolean forced){
        if (forced)
        {
            mActiveTripLayout.setVisibility(View.GONE);
            return;
        }
        if (!((ApplicationExtension) getActivity().getApplicationContext()).getIsBackgroundServiceRunning()) {
            mActiveTripLayout.setVisibility(View.GONE);
        }
        else{
            mActiveTripLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyBoard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_where_to, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_activity_where_to);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        init(getView(), getActivity());

        displayTip();

        checkLocationPermission();

        if (getActivity().getIntent().getStringExtra("calling-activity").equals("city-selector-activity")) {
            AppRater.app_launched(getActivity());

            TaxiPrompter.app_launched(getActivity(), getFragmentManager());
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // Try move the centre on location button.
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            View mapView = mapFragment.getView();
            if (mapView != null &&
                    mapView.findViewById(Integer.parseInt("1")) != null) {
                // Get the button view
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 30);
            }
        }
        catch (Exception e){ }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mStartLocation = latLng;
        mStoredUserLocation = mStartLocation;

        new GetAgenciesTask().execute();

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void showNoStartLocationAlert(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Warning")
                .setMessage("Could not determine your location. Make sure location services are enabled and try again.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    getActivity().onBackPressed();
                    // Permission denied, Disable the functionality that depends on this permission.
                    //Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }

    private void requestLocation(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case 1000: // Something was wrong with the location services.
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    {
                        // All required changes were successfully made
                        startLocationServices();

                        break;
                    }
                    case Activity.RESULT_CANCELED:
                    {
                        // The user was asked to change settings, but chose not to
                        getActivity().onBackPressed();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                break;
            case 1:
                if(resultCode == Activity.RESULT_OK) {
                    String profile = data.getStringExtra("Profile");
                    String timeType = data.getStringExtra("TimeType");

                    if (profile.equals("FewestTransfers"))
                        mProfile = Profile.FewestTransfers;
                    else
                        mProfile = Profile.ClosestToTime;

                    if (timeType.equals("ArriveBefore"))
                        mTimeType = TimeType.ArriveBefore;
                    else
                        mTimeType = TimeType.DepartAfter;

                    mTime = data.getStringExtra("Time");

                    mFilterImageView.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.filterapplied));
                }
                else{
                    mProfile = Profile.ClosestToTime;
                    mTimeType = TimeType.DepartAfter;
                    mTime = null;
                    mFilterImageView.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.filter));
                }
            case 2:
                if(resultCode == Activity.RESULT_OK) {
                    Boolean isLocation = data.getBooleanExtra("isLocation", false);
                    Boolean useUserLocation = data.getBooleanExtra("useUserLocation", false);
                    LatLng location = data.getParcelableExtra("location");

                    if (isLocation && !useUserLocation){
                        mStartLocation = location;

                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.where_to_locaiton_set)
                                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                    else if (isLocation && useUserLocation){
                        mStartLocation = mStoredUserLocation;
                    }
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationServices();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void startLocationServices(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        /*mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);*/

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

    protected TransportApiClient defaultClient = new TransportApiClient(new TransportApiClientSettings(ApplicationExtension.ClientId(), ApplicationExtension.ClientSecret()));

    private class GetAgenciesTask extends AsyncTask<Void, Void, TransportApiResult<List<Agency>>> {

        protected TransportApiResult<List<Agency>> doInBackground(Void... params){
            TransportApiResult<List<Agency>> agencies = defaultClient.getAgenciesNearby(AgencyQueryOptions.defaultQueryOptions(), mStartLocation.latitude, mStartLocation.longitude, 5000);

            return agencies;
        }

        protected void onPostExecute(TransportApiResult<List<Agency>> agencies) {

            if (agencies.isSuccess == false) {
                mRegionSupportButton.setVisibility(View.INVISIBLE);
                return;
            }
            else if (agencies.data == null) {
                mRegionSupportButton.setVisibility(View.VISIBLE);
                return;
            }
            else if (agencies.data.isEmpty()) {
                mRegionSupportButton.setVisibility(View.VISIBLE);
                return;
            }
            else{
                mRegionSupportButton.setVisibility(View.INVISIBLE);
            }
        }
    }
}