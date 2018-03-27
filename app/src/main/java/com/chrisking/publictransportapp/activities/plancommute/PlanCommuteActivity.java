package com.chrisking.publictransportapp.activities.plancommute;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.search.AddressAdapter;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.chrisking.publictransportapp.helpers.Shortcuts;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import transportapisdk.AgencyQueryOptions;
import transportapisdk.TransportApiClient;
import transportapisdk.TransportApiClientSettings;
import transportapisdk.TransportApiResult;
import transportapisdk.models.Agency;

public class PlanCommuteActivity extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private SharedPreferences mPrefs;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private EditText mSearchEditText;
    private Button mDoneButton;
    private Button mClearSearchButton;
    private ImageView mHomeButton;
    private ImageView mWorkButton;
    private ListView mAddressListView;
    private ArrayList<Address> mAddressList;
    private AddressAdapter mAddressAdapter;
    private Marker mHomeMarker;
    private Marker mWorkMarker;
    private Button mRegionSupportButton;
    private LatLng mUserLocation;

    private void init(final View view, final Activity activity)
    {
        mRegionSupportButton = (Button) view.findViewById(R.id.regionSupport);
        mRegionSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shortcuts.displayRegionUnSupportedTip(getActivity());
            }
        });

        mPrefs = activity.getSharedPreferences("settings", 0);
        mAddressListView = (ListView) view.findViewById(R.id.addressList);
        mAddressList = new ArrayList<Address>();
        mAddressAdapter = new AddressAdapter(activity, mAddressList);
        mAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                Address address = (Address)adapter.getItemAtPosition(position);

                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                hideAddressList();
                hideKeyBoard();
            }
        });

        mDoneButton = (Button) view.findViewById(R.id.done);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDoneTip();
            }
        });

        mHomeButton = (ImageView) view.findViewById(R.id.home);
        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng homeLocation = mMap.getCameraPosition().target;

                if ((homeLocation.latitude + "," +homeLocation.longitude).equals(getWorkLocation())) {
                    showSameLocationNotAllowed();
                    return;
                }

                setHomeLocation();

                addOrReplaceHomeLocationOnMap(homeLocation);

                showHomeLocationSuccess();
            }
        });

        mWorkButton = (ImageView) view.findViewById(R.id.work);
        mWorkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng workLocation = mMap.getCameraPosition().target;

                if ((workLocation.latitude + "," +workLocation.longitude).equals(getHomeLocation())){
                    showSameLocationNotAllowed();
                    return;
                }

                setWorkLocation();

                addOrReplaceWorkLocationOnMap(workLocation);

                showWorkLocationSuccess();
            }
        });

        mSearchEditText = (EditText) view.findViewById(R.id.searchText);
        mSearchEditText.addTextChangedListener(new TextWatcher() {

            private String text;
            private long after;
            private Thread t;
            private Runnable runnable_EditTextWatcher = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if ((System.currentTimeMillis() - after) > 1000)
                        {
                            // Do your stuff
                            t = null;
                            search(text);
                            break;
                        }
                    }
                }
            };

            @Override
            public void onTextChanged(CharSequence chars, int start, int before, int count) {
                text = chars.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable ss) {
                after = System.currentTimeMillis();
                if (t == null)
                {
                    t = new Thread(runnable_EditTextWatcher);
                    t.start();
                }
            }
        });

        mClearSearchButton = (Button) view.findViewById(R.id.clearSearch);
        mClearSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditText.setText("");
                hideKeyBoard();
            }
        });
    }

    private String getHomeLocation(){
        return mPrefs.getString("homelocation", null);
    }

    private String getWorkLocation(){
        return mPrefs.getString("worklocation", null);
    }

    private void displayWelcomeTip(){
        if (getHomeLocation() != null && getWorkLocation() != null) { return ; }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.plan_commute_welcome)
                .setMessage(R.string.plan_commute_description)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void displayDoneTip(){
        if (getHomeLocation() == null || getWorkLocation() == null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.plan_commute_done_incomplete_header)
                    .setMessage(R.string.plan_commute_done_incomplete_description)
                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.plan_commute_complete)
                .setMessage(R.string.plan_commute_done_complete)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Minimize APp", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_HOME);
                        startActivity(i);
                    }})
                .show();
    }

    private void loadHomeAndWorkMarkers(){
        String homeLocation = mPrefs.getString("homelocation", null);
        String workLocation = mPrefs.getString("worklocation", null);

        if (homeLocation != null) {
            String[] homeLatlong =  homeLocation.split(",");
            double homeLatitude = Double.parseDouble(homeLatlong[0]);
            double homeLongitude = Double.parseDouble(homeLatlong[1]);

            addOrReplaceHomeLocationOnMap(new LatLng(homeLatitude, homeLongitude));
        }

        if (workLocation != null) {
            String[] workLatlong =  workLocation.split(",");
            double workLatitude = Double.parseDouble(workLatlong[0]);
            double workLongitude = Double.parseDouble(workLatlong[1]);

            addOrReplaceWorkLocationOnMap(new LatLng(workLatitude, workLongitude));
        }
    }

    private Bitmap getSizedHomeIcon(){
        int height = 48;
        int width = 48;

        BitmapDrawable bitMapDraw = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.home);
        Bitmap b = bitMapDraw.getBitmap();

        return Bitmap.createScaledBitmap(b, width, height, false);
    }

    private Bitmap getSizedWorkIcon(){
        int height = 48;
        int width = 48;

        BitmapDrawable bitMapDraw = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.work);
        Bitmap b = bitMapDraw.getBitmap();

        return Bitmap.createScaledBitmap(b, width, height, false);
    }

    private void addOrReplaceHomeLocationOnMap(LatLng homeLocation){
        if (mHomeMarker != null)
            mHomeMarker.remove();

        mHomeMarker = mMap.addMarker(new MarkerOptions()
                .position(homeLocation)
                .title("Home")
                .icon(BitmapDescriptorFactory.fromBitmap(getSizedHomeIcon())));
    }

    private void addOrReplaceWorkLocationOnMap(LatLng workLocation){
        if (mWorkMarker != null)
            mWorkMarker.remove();

        mWorkMarker = mMap.addMarker(new MarkerOptions()
                .position(workLocation)
                .title("Work/ School")
                .icon(BitmapDescriptorFactory.fromBitmap(getSizedWorkIcon())));
    }

    private void setHomeLocation(){
        LatLng homeLocation =  mMap.getCameraPosition().target;

        saveSetting("homelocation", String.valueOf(homeLocation.latitude) + "," + String.valueOf(homeLocation.longitude));
    }

    private void showSameLocationNotAllowed(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.plan_commute_error)
                .setMessage(R.string.plan_commute_same_location)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showHomeLocationSuccess(){
        if (getWorkLocation() == null)
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.plan_commute_success)
                    .setMessage(R.string.plan_commute_home_added_success_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        else
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.plan_commute_complete)
                    .setMessage(R.string.plan_commute_add_widget)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
    }

    private void setWorkLocation(){
        LatLng workLocation =  mMap.getCameraPosition().target;

        saveSetting("worklocation", String.valueOf(workLocation.latitude) + "," + String.valueOf(workLocation.longitude));
    }

    private void showWorkLocationSuccess(){
        if (getHomeLocation() == null)
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.plan_commute_success)
                    .setMessage(R.string.plan_commute_work_added_success_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        else
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.plan_commute_complete)
                    .setMessage(R.string.plan_commute_add_widget)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
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

        return inflater.inflate(R.layout.activity_plan_commute, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.title_activity_plan_commute);
        FlurryAgent.logEvent("PlanCommute");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init(getView(), getActivity());

        displayWelcomeTip();

        checkLocationPermission();
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

        loadHomeAndWorkMarkers();

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

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        mUserLocation = latLng;

        new GetAgenciesTask().execute();

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void saveSetting(String key, String value){
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putString(key, value);

        editor.commit();
    }

    private void search(String searchText){
        if (searchText != null && !searchText.equals("")) {
            Geocoder geocoder = new Geocoder(getActivity());
            try {
                mAddressList = (ArrayList<Address>) geocoder.getFromLocationName(searchText, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAddressAdapter.clear();
                                    mAddressAdapter.addAll(mAddressList);
                                    mAddressListView.setAdapter(mAddressAdapter);

                                    showAddressList();
                                }
                            });

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
            };
            thread.start();
        }
        else{
            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            wait(1);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideAddressList();
                                }
                            });

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
            };
            thread.start();
        }
    }

    private void showAddressList(){
        mAddressListView.setVisibility(View.VISIBLE);
        mDoneButton.setVisibility(View.INVISIBLE);
    }

    private void hideAddressList(){
        mAddressListView.setVisibility(View.INVISIBLE);
        mDoneButton.setVisibility(View.VISIBLE);
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
            TransportApiResult<List<Agency>> agencies = defaultClient.getAgenciesNearby(AgencyQueryOptions.defaultQueryOptions(), mUserLocation.latitude, mUserLocation.longitude, 5000);

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
