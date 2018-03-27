package com.chrisking.publictransportapp.activities.about;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrisking.publictransportapp.R;

public class AboutActivity extends AppCompatActivity {

    private void init()
    {
        ImageView twitterButton = (ImageView) findViewById(R.id.twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.twitter.com/crkingza";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });

        ImageView webButton = (ImageView) findViewById(R.id.web);
        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://about.me/crking";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });

        ImageView linkedInButton = (ImageView) findViewById(R.id.linkedin);
        linkedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.linkedin.com/in/crkingza";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });

        TextView transportApiSdkTextView = (TextView) findViewById(R.id.transportApiSdk);
        transportApiSdkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/WhereIsMyTransport/TransportApiSdk.Java";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                startActivity(i);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        init();
    }
}
