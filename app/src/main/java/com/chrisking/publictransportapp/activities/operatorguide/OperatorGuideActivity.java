package com.chrisking.publictransportapp.activities.operatorguide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import com.chrisking.publictransportapp.helpers.Shortcuts;

import com.chrisking.publictransportapp.R;

public class OperatorGuideActivity extends AppCompatActivity {

    private TextView mNameTextView;
    private WebView mOperatorGuideWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_guide);


        mOperatorGuideWebView =  (WebView) findViewById(R.id.wv_operator_guide);
        mNameTextView = (TextView) findViewById(R.id.tv_operator_Name);


        Intent taxiNameInformationIntent = getIntent();
        if (taxiNameInformationIntent.hasExtra(Intent.EXTRA_TEXT)){

                String taxiName = taxiNameInformationIntent.getStringExtra(Intent.EXTRA_TEXT);
                mNameTextView.setText(taxiName);

                String resource = Shortcuts.mapOperatorNameToHtmlGuide(taxiName);
                WebSettings settings = mOperatorGuideWebView.getSettings();
                settings.setDefaultTextEncodingName("utf-8");
                mOperatorGuideWebView.loadUrl(resource);



        }
    }
}
