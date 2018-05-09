package com.chrisking.publictransportapp.activities.tripsharing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;
import transportapisdk.TransportApiClient;
import transportapisdk.TransportApiClientSettings;
import transportapisdk.TransportApiResult;
import transportapisdk.models.Itinerary;
import transportapisdk.models.Journey;
import transportapisdk.models.Leg;
import transportapisdk.models.LineString;

public class TripSharingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int mItineraryNumber = 0;
    private String mJourneyId;
    private DatabaseReference mDatabase;
    private String mLatitude;
    private String mLongitude;
    private float mZoomLevel = 14;
    private boolean mFinishedLoading = false;
    private String mTimeAgo = "Unknown";
    public Marker mMarker;

    // Define the api client.
    protected TransportApiClient defaultClient = new TransportApiClient(new TransportApiClientSettings(ApplicationExtension.ClientId(), ApplicationExtension.ClientSecret(),60, ApplicationExtension.UniqueContextId()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_sharing);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        String uid = intent.getStringExtra("uid");

        mJourneyId = uid.substring(0, uid.length() - 1);
        mItineraryNumber = Integer.parseInt(uid.substring(uid.length() - 1));

        FlurryAgent.logEvent("ViewSharedTrip");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("tripshares").child(uid);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        new GetJourneysTask().execute();
    }

    private void drawMarker(String key, String value){
        if(mMarker != null){
            if(mFinishedLoading) {
                mZoomLevel = mMap.getCameraPosition().zoom;
            }
            mMarker.remove();
        }
        if(key.equals("latitude")){
            mLatitude = value;
        }
        if(key.equals("longitude")){
            mLongitude = value;
        }
        if(key.equals("datetime")){
            Date time = new Date(Long.parseLong(value));
            mTimeAgo = Shortcuts.timeUntil(time);
        }

        if(mLatitude != null && mLongitude != null){
            LatLng latLng = new LatLng(Double.parseDouble(mLatitude) , Double.parseDouble(mLongitude));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mFinishedLoading = true;
                }

                @Override
                public void onCancel() {

                }
            });
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude)))
                    .title("Current Location")
                    .snippet(mTimeAgo)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMarker.showInfoWindow();
        }
    }

    private void addLines(Itinerary itinerary) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        int count = 0;
        for (Leg leg: itinerary.getLegs()){
            int color;
            if (leg.getLine() == null) {
                color = ContextCompat.getColor(this, R.color.colorWalking);
            } else {
                color = Color.parseColor(leg.getLine().getColour());
            }
            LineString line = leg.getGeometry();
            PolylineOptions polylineOptions = new PolylineOptions().width(10).color(color).geodesic(true);
            for (List<Double> coordinates : line.getCoordinates()) {
                LatLng coordinate = new LatLng(coordinates.get(1), coordinates.get(0));
                polylineOptions.add(coordinate);
            }
            mMap.addPolyline(polylineOptions);

            count++;
        }

        // move camera to zoom on map
        int padding = 50;
        /**create the bounds from latlngBuilder to set into map camera*/
        /**create the camera with bounds and padding to set into map*/
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue().toString();
                drawMarker(key, value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue().toString();
                drawMarker(key, value);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mMarker.remove();
                String key = dataSnapshot.getKey();
                if(key.equals("latitude")){
                    showTripSharingEnded();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showTripSharingEnded(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.trip_share_alert_title)
                .setMessage(R.string.trip_share_alert_sharing_ended)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private class GetJourneysTask extends AsyncTask<Void, Void, TransportApiResult<Journey>> {

        protected TransportApiResult<Journey> doInBackground(Void... params){
            TransportApiResult<Journey> journey = defaultClient.getJourney(mJourneyId , "directions");
            return journey;
        }

        protected void onPostExecute(TransportApiResult<Journey> journey) {
            if (journey.isSuccess == false) {
                System.out.println(getResources().getString(R.string.error_check_internet));
                return;
            }
            else if (journey.data == null) {
                System.out.println(getResources().getString(R.string.no_results));
                return;
            }
            else if (journey.data.getItineraries().isEmpty()) {
                System.out.println(getResources().getString(R.string.no_results));
                return;
            }
            addLines(journey.data.getItineraries().get(mItineraryNumber));
        }
    }
}