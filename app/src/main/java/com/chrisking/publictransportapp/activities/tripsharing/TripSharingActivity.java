package com.chrisking.publictransportapp.activities.tripsharing;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.journeyoptions.JourneyAdapter;
import com.chrisking.publictransportapp.activities.journeyoptions.JourneyOptionsActivity;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
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

import java.util.ArrayList;
import java.util.HashMap;
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
    private Itinerary mItinerary;
    private int mItineraryNumber = 0;
    private String mJourneyId = "mM79WePyqEGdGKixAQTJXA";
    private DatabaseReference mDatabase;
    private String mLatitude;
    private String mLongitude;
    public Marker mMarker;

    // Define the api client.
    protected TransportApiClient defaultClient = new TransportApiClient(new TransportApiClientSettings(ApplicationExtension.ClientId(), ApplicationExtension.ClientSecret()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_sharing);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("tripshares").child(mJourneyId + mItineraryNumber);
        new GetJourneysTask().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    private void drawMarker(String key, String value){
        if(mMarker != null){
            mMarker.remove();
        }
        if(key.equals("latitude")){
            mLatitude = value;
        }
        if(key.equals("longitude")){
            mLongitude = value;
        }

        if(mLatitude != null && mLongitude != null){
            LatLng latLng = new LatLng(Double.parseDouble(mLatitude) , Double.parseDouble(mLongitude));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude)))
                    .title("Current Location")
                    .snippet("33 seconds ago")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    private void addLines() {
        System.out.println("addLines() !!!!!");
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        int count = 0;
        for (Leg leg: mItinerary.getLegs()){
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
                if (mItineraryNumber == -1 || mItineraryNumber == count) {
                    builder.include(coordinate);
                }
            }
            mMap.addPolyline(polylineOptions);

            count++;
        }

        // move camera to zoom on map
        int padding = 50;
        /**create the bounds from latlngBuilder to set into map camera*/
        LatLngBounds bounds = builder.build();
        /**create the camera with bounds and padding to set into map*/
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                /**set animated zoom camera into map*/
                mMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        mDatabase.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String key = dataSnapshot.getKey();
                                String value = dataSnapshot.getValue(String.class);
                                drawMarker(key, value);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                String key = dataSnapshot.getKey();
                                String value = dataSnapshot.getValue(String.class);
                                drawMarker(key, value);
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                mMarker.remove();
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
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
            mItinerary = journey.data.getItineraries().get(mItineraryNumber);
            addLines();
        }
    }
}