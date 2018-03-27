package com.chrisking.publictransportapp.activities.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;

import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
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

/**
 * Created by ChrisKing on 2017/05/06.
 */

public class WidgetRemoteFetchService extends Service {

    private SharedPreferences mPrefs;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    protected LatLng mStartLocation;
    protected LatLng mEndLocation;

    JourneyBodyOptions mOptions = new JourneyBodyOptions(
            null,
            null,
            null,
            null,
            5,
            null);

    // Define the api client.
    protected TransportApiClient defaultClient = new TransportApiClient(new TransportApiClientSettings(ApplicationExtension.ClientId(), ApplicationExtension.ClientSecret()));

    public static ArrayList<Itinerary> mListItemList;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Retrieve appwidget id from intent it is needed to update widget later
     * initialize our AQuery class
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        mPrefs = this.getSharedPreferences("settings", 0);

        loadJourneySettings();

        fetchDataFromSharedPref();

        return Service.START_NOT_STICKY;
    }

    private void loadJourneySettings(){

        if (!mPrefs.getString("profile", "closest_to_time").equals("closest_to_time"))
            mOptions.profile = Profile.FewestTransfers;

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

        mOptions.omitModes = omitModes;
    }

    /**
     * method which fetches data(json) from web aquery takes params
     * remoteJsonUrl = from where data to be fetched String.class = return
     * format of data once fetched i.e. in which format the fetched data be
     * returned AjaxCallback = class to notify with data once it is fetched
     */
    private void fetchDataFromSharedPref() {
        String homeLocation = mPrefs.getString("homelocation", null);
        String workLocation = mPrefs.getString("worklocation", null);

        if (homeLocation == null || workLocation == null) {
            //populateWidgetAdditionalSetup();
            return;
        }

        String[] homeLatlong =  homeLocation.split(",");
        double homeLatitude = Double.parseDouble(homeLatlong[0]);
        double homeLongitude = Double.parseDouble(homeLatlong[1]);

        String[] workLatlong =  workLocation.split(",");
        double workLatitude = Double.parseDouble(workLatlong[0]);
        double workLongitude = Double.parseDouble(workLatlong[1]);

        if (homeLatitude == workLatitude &&
                homeLongitude == workLongitude){
            //populateWidgetAdditionalSetup();
            return;
        }

        if (Shortcuts.userIsHome()){
            mStartLocation = new LatLng(homeLatitude, homeLongitude);
            mEndLocation = new LatLng(workLatitude, workLongitude);
        }
        else{
            mStartLocation = new LatLng(workLatitude, workLongitude);
            mEndLocation = new LatLng(homeLatitude, homeLongitude);
        }

        new GetJourneysTask().execute();
    }

    /**
     * Method which sends broadcast to WidgetProvider
     * so that widget is notified to do necessary action
     * and here action == WidgetProvider.DATA_FETCHED
     */
    private void populateWidgetWithData() {

        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(WidgetListProvider.DATA_FETCHED);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }

    private void populateWidgetAdditionalSetup() {
        this.stopSelf();
        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(WidgetListProvider.ADDITIONAL_SETUP);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(widgetUpdateIntent);


    }

    private void populateWidgetError() {

        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(WidgetListProvider.ERROR_OR_NO_CONNECTION);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }

    private class GetJourneysTask extends AsyncTask<Void, Void, TransportApiResult<Journey>> {

        protected TransportApiResult<Journey> doInBackground(Void... params){
            TransportApiResult<Journey> journey = defaultClient.postJourney(mOptions, mStartLocation.latitude, mStartLocation.longitude, mEndLocation.latitude, mEndLocation.longitude, "directions");

            return journey;
        }

        protected void onPostExecute(TransportApiResult<Journey> journey) {

            mListItemList = new ArrayList<Itinerary>();

            if (journey.isSuccess == false) {
                populateWidgetError();
                return;
            }
            else if (journey.data != null && !journey.data.getItineraries().isEmpty()) {
                mListItemList.addAll(journey.data.getItineraries());
            }

            populateWidgetWithData();
        }
    }

}

