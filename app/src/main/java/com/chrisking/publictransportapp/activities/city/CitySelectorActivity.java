package com.chrisking.publictransportapp.activities.city;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.main.TripPlannerActivity;
import com.chrisking.publictransportapp.activities.operatorguide.OperatorGuideActivity;
import com.chrisking.publictransportapp.classes.City;

public class CitySelectorActivity extends AppCompatActivity {

    private CityPersistence mCityPersistence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_selector);

        storageSetup();

        init();
    }

    private void init(){
        // Init city persistence.
        mCityPersistence = new CityPersistence(this);

        // Check for existing saved city.
        City savedCity = mCityPersistence.getSavedCity();

        boolean fromMenu = getIntent().getBooleanExtra("menu", false);

        if (savedCity != null && !fromMenu){
            startMainActivity();

            return;
        }

        // Init the city list.
        ListView cityListView = (ListView) findViewById(R.id.cityListView);
        cityListView.setAdapter(new CityListAdapter(this, City.getLocalCityStore()));
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                City cityItem = (City) adapter.getItemAtPosition(position);

                mCityPersistence.saveCity(cityItem);

                startMainActivity();
            }
        });
    }

    private void storageSetup(){
        SharedPreferences prefs = this.getSharedPreferences("settings", 0);
        if (prefs.getBoolean("setup", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("profile", "closest_to_time");
        editor.putBoolean("lightrail", true);
        editor.putBoolean("subway", true);
        editor.putBoolean("rail", true);
        editor.putBoolean("ferry", true);
        editor.putBoolean("coach", true);
        editor.putBoolean("sharetaxi", true);
        editor.putBoolean("bus", true);

        editor.putBoolean("setup", true);

        editor.apply();
    }

    public final void startMainActivity(){
        Intent push = new Intent(CitySelectorActivity.this, TripPlannerActivity.class);
        push.putExtra("calling-activity", "city-selector-activity");

        startActivity(push);
    }


}
