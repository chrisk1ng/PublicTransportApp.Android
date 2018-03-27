package com.chrisking.publictransportapp.activities.map;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import transportapisdk.models.Itinerary;
import transportapisdk.models.Leg;
import transportapisdk.models.LineString;

public class ViewOnMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Itinerary mItinerary;
    private int mLeg = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        mLeg = intent.getIntExtra("leg", -1);

        ApplicationExtension app = (ApplicationExtension) getApplicationContext();
        mItinerary = app.getItinerary();
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

        addLines();
    }

    private void addLines() {
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
                if (mLeg == -1 || mLeg == count) {
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
                mMap.animateCamera(cu);
            }
        });

    }
}
