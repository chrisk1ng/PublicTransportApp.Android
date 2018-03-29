package com.chrisking.publictransportapp.activities.journeyoptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
import com.chrisking.publictransportapp.services.location.LocationMonitoringService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import java.util.ArrayList;
import java.util.List;
import transportapisdk.JourneyBodyOptions;
import transportapisdk.TransportApiClient;
import transportapisdk.TransportApiClientSettings;
import transportapisdk.TransportApiResult;
import transportapisdk.models.Itinerary;
import transportapisdk.models.Journey;
import transportapisdk.models.Profile;
import transportapisdk.models.TimeType;

public class JourneyOptionsActivity extends AppCompatActivity {
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private ProgressBar mLoader;
    private TextView mResultInfoTextView;
    private SharedPreferences mPrefs;
    private Profile mProfile = Profile.ClosestToTime;
    private TimeType mTimeType = TimeType.DepartAfter;
    private String mTime = null;
    private String mJourneyId;
    private LinearLayout mActiveTripLayout;
    private Button mTripShareStopButton;
    private String mUid;

    // Define the api client.
    protected TransportApiClient defaultClient = new TransportApiClient(new TransportApiClientSettings(ApplicationExtension.ClientId(), ApplicationExtension.ClientSecret()));

    JourneyBodyOptions options = new JourneyBodyOptions(
            null,
            null,
            null,
            null,
            5,
            null);

    protected ArrayList<Itinerary> arrayOfItineraries;
    protected JourneyAdapter adapter;
    protected LatLng startLocation;
    protected LatLng endLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_options);

        // Construct the data source
        arrayOfItineraries = new ArrayList<Itinerary>();

        // Create the adapter to convert the array to views
        adapter = new JourneyAdapter(this, arrayOfItineraries);

        Intent intent = getIntent();

        startLocation = intent.getParcelableExtra("startLocation");
        endLocation = intent.getParcelableExtra("endLocation");

        String profile = intent.getStringExtra("Profile");
        String timeType = intent.getStringExtra("TimeType");
        mTime = intent.getStringExtra("Time");

        if (profile.equals("FewestTransfers"))
            mProfile = Profile.FewestTransfers;
        else
            mProfile = Profile.ClosestToTime;

        if (timeType.equals("ArriveBefore"))
            mTimeType = TimeType.ArriveBefore;
        else
            mTimeType = TimeType.DepartAfter;

        mLoader = (ProgressBar) findViewById(R.id.loader);
        mLoader.setVisibility(View.VISIBLE);

        mResultInfoTextView = (TextView) findViewById(R.id.resultInfo);
        mResultInfoTextView.setVisibility(View.INVISIBLE);
        mActiveTripLayout = (LinearLayout) findViewById(R.id.activeTripLayout);
        mActiveTripLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShareLink(mUid);
            }
        });

        mTripShareStopButton = (Button) findViewById(R.id.tripShareStopButton);
        mTripShareStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocationMonitoringService.class);
                stopService(intent);
                showOnTrip(true);
            }
        });

        mPrefs = this.getSharedPreferences("settings", 0);

        loadJourneySettings();
        showOnTrip(false);

        new GetJourneysTask().execute();
    }

    public void startLocationService(int itineraryIndex) {

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        if (!((ApplicationExtension) getApplicationContext()).getIsBackgroundServiceRunning()) {

            if(Shortcuts.isMyServiceRunning(this, LocationMonitoringService.class))
            {
                Intent intent = new Intent(this, LocationMonitoringService.class);
                stopService(intent);
            }

            //Start location sharing service to app server.........
            Intent intent = new Intent(this, LocationMonitoringService.class);
            intent.putExtra("journeyId", mJourneyId);
            intent.putExtra("itineraryIndex", itineraryIndex);

            startService(intent);

            ((ApplicationExtension) getApplicationContext()).setIsBackgroundServiceRunning(true);
            mUid = mJourneyId + String.valueOf(itineraryIndex);
            sendShareLink(mUid);
            showOnTrip(false);
            //Ends................................................
        }
        else{
            new AlertDialog.Builder(this)
                    .setTitle(R.string.trip_share_alert_title)
                    .setMessage(R.string.trip_share_alert_already_sharing)
                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    public void sendShareLink(final String uid){

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://insta.trip?uid=" + uid))
                .setDynamicLinkDomain("enc6m.app.goo.gl")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
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

                        String shareText = getResources().getString(R.string.trip_share_share_text) + " " + dynamicLinkUri.toString();
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
        if (!((ApplicationExtension) getApplicationContext()).getIsBackgroundServiceRunning()) {
            mActiveTripLayout.setVisibility(View.GONE);
        }
        else{
            mActiveTripLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    public boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Start permissions requests.
     */
    public void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {

            /*showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });*/
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void loadJourneySettings(){

        options.profile = mProfile;
        options.timeType = mTimeType;

        if (mTime != null)
            options.time = mTime;

        List<String> omitModes = new ArrayList<String>();

        if (!mPrefs.getBoolean("bus", false))
            omitModes.add("bus");
        if (!mPrefs.getBoolean("lightrail", false))
            omitModes.add("lightrail");
        if (!mPrefs.getBoolean("subway", false))
            omitModes.add("subway");
        if (!mPrefs.getBoolean("rail", false))
            omitModes.add("rail");
        if (!mPrefs.getBoolean("ferry", false))
            omitModes.add("ferry");
        if (!mPrefs.getBoolean("coach", false))
            omitModes.add("coach");
        if (!mPrefs.getBoolean("sharetaxi", false))
            omitModes.add("sharetaxi");

        options.omitModes = omitModes;
    }

    private class GetJourneysTask extends AsyncTask<Void, Void, TransportApiResult<Journey>> {

        protected TransportApiResult<Journey> doInBackground(Void... params){
            TransportApiResult<Journey> journey = defaultClient.postJourney(options, startLocation.latitude, startLocation.longitude, endLocation.latitude, endLocation.longitude, "directions");

            return journey;
        }

        protected void onPostExecute(TransportApiResult<Journey> journey) {
            mLoader.setVisibility(View.INVISIBLE);

            if (journey.isSuccess == false) {
                mResultInfoTextView.setText(getResources().getString(R.string.error_check_internet));
                mResultInfoTextView.setVisibility(View.VISIBLE);
                return;
            }
            else if (journey.data == null) {
                mResultInfoTextView.setText(getResources().getString(R.string.no_results));
                mResultInfoTextView.setVisibility(View.VISIBLE);
                return;
            }
            else if (journey.data.getItineraries().isEmpty()) {
                mResultInfoTextView.setText(getResources().getString(R.string.no_results));
                mResultInfoTextView.setVisibility(View.VISIBLE);
                return;
            }

            mJourneyId = journey.data.getId();
            adapter.addAll(journey.data.getItineraries());

            // Attach the adapter to a ListView
            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
        }
    }
}
