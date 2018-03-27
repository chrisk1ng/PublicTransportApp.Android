package com.chrisking.publictransportapp.activities.itinerary;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chrisking.publictransportapp.R;

public class SelectedItineraryListItem extends AppCompatActivity {

    private ConstraintLayout innerCardConstrainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_itinerary_list_item);

        innerCardConstrainLayout = (ConstraintLayout) findViewById(R.id.innerCard);
        innerCardConstrainLayout.getLayoutParams().width = 100;
        innerCardConstrainLayout.requestLayout();

    }
}
