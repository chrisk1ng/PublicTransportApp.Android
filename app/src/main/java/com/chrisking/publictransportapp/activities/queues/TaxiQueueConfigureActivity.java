package com.chrisking.publictransportapp.activities.queues;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.chrisking.publictransportapp.services.geofencing.GeofencePersistence;
import com.chrisking.publictransportapp.R;

import java.util.ArrayList;

public class TaxiQueueConfigureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_queue_configure);

        setTitle(R.string.title_activity_taxi_queues_configure);

        init();
    }

    private void init(){
        // Init the city list.
        ListView stopListView = (ListView) findViewById(R.id.stopListView);
        stopListView.setAdapter(new TaxiQueueConfigureAdaptor(this, new ArrayList<>(new GeofencePersistence(this).getNamedGeofences())));

    }
}
