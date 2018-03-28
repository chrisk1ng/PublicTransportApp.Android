package com.chrisking.publictransportapp.activities.operatorguide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;

public class OperatorGuideActivity extends AppCompatActivity {

    private void init()
    {
        //TODO: init based on the operator Id
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_guide);

        Intent taxiNameInformationIntent = getIntent();
        if (taxiNameInformationIntent.hasExtra(Intent.EXTRA_TEXT)){
            String taxiName = taxiNameInformationIntent.getStringExtra(Intent.EXTRA_TEXT);

            //TODO: build up a resource list, match this taxiName against the list, and see if we can populate the fields with non-placeholder info!

        }


        init();
    }
}
