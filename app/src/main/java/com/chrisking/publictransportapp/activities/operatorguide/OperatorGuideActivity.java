package com.chrisking.publictransportapp.activities.operatorguide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import com.chrisking.publictransportapp.helpers.Shortcuts;

import com.chrisking.publictransportapp.R;
import com.flurry.android.FlurryAgent;

public class OperatorGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_guide);

        WebView operatorGuideWebView =  (WebView) findViewById(R.id.wv_operator_guide);
        TextView nameTextView = (TextView) findViewById(R.id.tv_operator_Name);

        Intent taxiNameInformationIntent = getIntent();
        if (taxiNameInformationIntent.hasExtra(Intent.EXTRA_TEXT)){
            FlurryAgent.logEvent("ViewTaxiGuide");

            String taxiName = taxiNameInformationIntent.getStringExtra(Intent.EXTRA_TEXT);
            nameTextView.setText(taxiName);

            String resource = Shortcuts.mapOperatorNameToHtmlGuide(taxiName);
            WebSettings settings = operatorGuideWebView.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            operatorGuideWebView.loadUrl(resource);
        }
    }
}
