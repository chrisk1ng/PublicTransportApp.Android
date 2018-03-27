package com.chrisking.publictransportapp.activities.journeyoptions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.google.android.gms.maps.model.LatLng;

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
    private ProgressBar mLoader;
    private TextView mResultInfoTextView;
    private SharedPreferences mPrefs;
    private Profile mProfile = Profile.ClosestToTime;
    private TimeType mTimeType = TimeType.DepartAfter;
    private String mTime = null;

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

        mPrefs = this.getSharedPreferences("settings", 0);

        loadJourneySettings();

        new GetJourneysTask().execute();
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

            adapter.addAll(journey.data.getItineraries());

            // Attach the adapter to a ListView
            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);
        }
    }
}
