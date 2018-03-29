package com.chrisking.publictransportapp.activities.main;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.chrisking.publictransportapp.activities.learnmore.LearnMoreActivity;
import com.chrisking.publictransportapp.activities.operatorguide.OperatorGuideActivity;
import com.chrisking.publictransportapp.activities.plancommute.PlanCommuteActivity;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.city.CityPersistence;
import com.chrisking.publictransportapp.activities.city.CitySelectorActivity;
import com.chrisking.publictransportapp.activities.queues.TaxiQueues;
import com.chrisking.publictransportapp.activities.settings.AdvancedOptionsActivity;
import com.chrisking.publictransportapp.activities.tripsharing.TripSharingActivity;
import com.chrisking.publictransportapp.activities.whereto.WhereToActivity;
import com.chrisking.publictransportapp.classes.City;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class TripPlannerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {

    private String savedCityTaxiName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new WhereToActivity())
                .commit();

        City savedCity = new CityPersistence(this).getSavedCity();

        Menu menuNav = navigationView.getMenu();
        MenuItem taxiQueues = menuNav.findItem(R.id.taxiQueues);

        taxiQueues.setVisible(false);
        /*if (!savedCity.getHasInformal()){
            taxiQueues.setVisible(false);
        }
        else{
            taxiQueues.setVisible(true);
            taxiQueues.setTitle(savedCity.getTaxiName() + " " + getString(R.string.menu_taxi_queues));
        }*/



        MenuItem taxiGuide = menuNav.findItem(R.id.taxiGuide);
        taxiGuide.setVisible(false);
        if (!savedCity.getHasInformal()){
            taxiGuide.setVisible(false);
        }
        else{
            taxiGuide.setVisible(true);
            savedCityTaxiName = savedCity.getTaxiName();
            taxiGuide.setTitle(savedCityTaxiName + " " + getString(R.string.menu_taxi_guide));
        }



        TextView cityNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.cityTextView);
        cityNameTextView.setText(savedCity.getName());

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            return;
                        }

                        // Get the deep link
                        Uri deepLink = data.getLink();
                        String uid = deepLink.getQueryParameter("uid");

                        // Handle the deep link
                        // [START_EXCLUDE]

                        if (deepLink != null && uid != null) {
                            Intent intent = new Intent(getApplicationContext(), TripSharingActivity.class);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                        }
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public final void startOperatorGuideActivity() {
        Intent testIntent = new Intent(TripPlannerActivity.this, OperatorGuideActivity.class);
        testIntent.putExtra(Intent.EXTRA_TEXT, savedCityTaxiName);

        startActivity(testIntent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

            int lastBackStackEntryCount = getSupportFragmentManager().getBackStackEntryCount() - 1;
            if (lastBackStackEntryCount >= 0) {
                FragmentManager.BackStackEntry lastBackStackEntry =
                        getSupportFragmentManager().getBackStackEntryAt(lastBackStackEntryCount);

                setTitle(lastBackStackEntry.getName());
            }
            else{
                setTitle(getString(R.string.title_activity_where_to));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trip_planner, menu);
        return true;
    }

    @Override
    public void onBackStackChanged() {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.taxiQueues) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.title_activity_taxi_queues));
            if (myFragment == null || !myFragment.isVisible()) {
                fragmentManager.beginTransaction()
                        .add(R.id.content_frame, new TaxiQueues(), getString(R.string.title_activity_taxi_queues))
                        .addToBackStack(getString(R.string.title_activity_taxi_queues))
                        .commit();
            }
        } else if (id == R.id.tripPlanner) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.title_activity_where_to));
            if (myFragment == null || !myFragment.isVisible()) {
                fragmentManager.beginTransaction()
                        .add(R.id.content_frame, new WhereToActivity(), getString(R.string.title_activity_where_to))
                        .addToBackStack(getString(R.string.title_activity_where_to))
                        .commit();
            }
        } else if (id == R.id.myCommute) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.title_activity_plan_commute));
            if (myFragment == null || !myFragment.isVisible()) {
                fragmentManager.beginTransaction()
                        .add(R.id.content_frame, new PlanCommuteActivity(), getString(R.string.title_activity_plan_commute))
                        .addToBackStack(getString(R.string.title_activity_plan_commute))
                        .commit();
            }
        } else if (id == R.id.citySelect) {
            Intent push = new Intent(TripPlannerActivity.this, CitySelectorActivity.class);
            push.putExtra("menu", true);
            startActivity(push);
        }
        else if (id == R.id.surveyParticipate) {
            Intent push = new Intent(TripPlannerActivity.this, CitySelectorActivity.class);

            startActivity(push);
        } else if (id == R.id.settings) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.title_activity_advanced));
            if (myFragment == null || !myFragment.isVisible()) {
                fragmentManager.beginTransaction()
                        .add(R.id.content_frame, new AdvancedOptionsActivity(), getString(R.string.title_activity_advanced))
                        .addToBackStack(getString(R.string.title_activity_advanced))
                        .commit();
            }
        } else if (id == R.id.about) {
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.title_activity_learn_more));
            if (myFragment == null || !myFragment.isVisible()) {
                fragmentManager.beginTransaction()
                    .add(R.id.content_frame, new LearnMoreActivity(), getString(R.string.title_activity_learn_more))
                    .addToBackStack(getString(R.string.title_activity_learn_more))
                    .commit();
            }
        } else if (id == R.id.taxiGuide)
        {
            startOperatorGuideActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
