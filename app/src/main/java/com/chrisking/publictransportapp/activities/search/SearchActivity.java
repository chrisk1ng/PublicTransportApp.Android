package com.chrisking.publictransportapp.activities.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.chrisking.publictransportapp.R;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

import transportapisdk.models.Profile;
import transportapisdk.models.TimeType;

public class SearchActivity extends AppCompatActivity {

    private ArrayList<Address> mAddressList;
    private ListView mAddressListView;
    private AddressAdapter mAddressAdapter;
    private boolean isLocation = false;
    private LinearLayout mUseLocationLayout;
    private LinearLayout mHorizontalSeparatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();
    }

    private void init(){
        mAddressListView = (ListView) findViewById(R.id.addressList);
        mAddressList = new ArrayList<>();
        mAddressAdapter = new AddressAdapter(this, mAddressList);
        mAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
            {
                Address address = (Address)adapter.getItemAtPosition(position);

                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                Intent intent = new Intent();
                intent.putExtra("isLocation", isLocation);
                intent.putExtra("location", latLng);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mHorizontalSeparatorLayout = (LinearLayout) findViewById(R.id.horizontalSeparatorLayout);
        mHorizontalSeparatorLayout.setVisibility(View.GONE);

        mUseLocationLayout = (LinearLayout) findViewById(R.id.useMyLocationLayout);
        mUseLocationLayout.setVisibility(View.GONE);
        mUseLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("isLocation", isLocation);
                intent.putExtra("useUserLocation", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        LinearLayout chooseOnMapLayout = (LinearLayout) findViewById(R.id.chooseOnMapLayout);
        chooseOnMapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (isLocation){
                   Intent push = new Intent(SearchActivity.this, ChooseLocationActivity.class);

                   FlurryAgent.logEvent("ChooseLocationOnMap");
                   startActivityForResult(push, 1);
               }
               else{
                   finish();
               }
            }
        });

        EditText locationEditText = (EditText) findViewById(R.id.locationTextView);
        locationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    isLocation = true;
                    mUseLocationLayout.setVisibility(View.VISIBLE);
                    mHorizontalSeparatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        locationEditText.addTextChangedListener(new TextWatcher() {

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
                            isLocation = true;
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

        EditText destinationEditText = (EditText) findViewById(R.id.destinationTextView);
        destinationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    isLocation = false;
                    mUseLocationLayout.setVisibility(View.GONE);
                    mHorizontalSeparatorLayout.setVisibility(View.GONE);
                }
            }
        });
        destinationEditText.requestFocus();
        destinationEditText.addTextChangedListener(new TextWatcher() {

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
                            isLocation = false;
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case 1:
                if(resultCode == Activity.RESULT_OK) {
                    LatLng location = data.getParcelableExtra("location");

                    Intent intent = new Intent();
                    intent.putExtra("isLocation", isLocation);
                    intent.putExtra("location", location);
                    setResult(RESULT_OK, intent);
                    finish();
                }
        }
    }

    private void search(String searchText){
        if (searchText != null && !searchText.equals("")) {
            Geocoder geocoder = new Geocoder(this);
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

                            runOnUiThread(new Runnable() {
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
                }
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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideAddressList();
                                }
                            });

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    private void showAddressList(){
        mAddressListView.setVisibility(View.VISIBLE);
    }

    private void hideAddressList(){
        mAddressListView.setVisibility(View.INVISIBLE);
    }
}
