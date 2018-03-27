package com.chrisking.publictransportapp.activities.itinerary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.widget.WidgetListProvider;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import transportapisdk.models.Leg;

public class ItineraryViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_view);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        ApplicationExtension app = (ApplicationExtension) getApplicationContext();
        if (!intent.getBooleanExtra("isReload", false))
            if (intent.getAction() != null && intent.getAction().equals(WidgetListProvider.OPEN_ITINERARY)) {
                FlurryAgent.logEvent("Itinerary Viewed");
                int position = intent.getIntExtra(WidgetListProvider.OPEN_ITINERARY, 0);
                boolean home = intent.getBooleanExtra(WidgetListProvider.HOME, true);
                app.setItinerary(app.getItineraries().get(position));

                app.setIsCommute(true, home);
            }
            else {
                app.setIsCommute(false, false);
            }

        // Construct the data source
        ArrayList<Leg> arrayOfLegs = (ArrayList<Leg>) app.getItinerary().getLegs();
        // Create the adapter to convert the array to views
        ItineraryAdaptor adapter = new ItineraryAdaptor(arrayOfLegs);
        recyclerView.setAdapter(adapter);
    }
}
