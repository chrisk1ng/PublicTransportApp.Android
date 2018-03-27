package com.chrisking.publictransportapp.activities.main;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.chrisking.publictransportapp.activities.itinerary.ItineraryViewActivity;
import com.chrisking.publictransportapp.activities.learnmore.LearnMoreActivity;
import com.chrisking.publictransportapp.activities.plancommute.PlanCommuteActivity;
import com.chrisking.publictransportapp.R;
import com.chrisking.publictransportapp.activities.queues.TaxiQueues;
import com.chrisking.publictransportapp.activities.settings.AdvancedOptionsActivity;
import com.chrisking.publictransportapp.activities.whereto.WhereToActivity;
import com.chrisking.publictransportapp.classes.QueueState;
import com.chrisking.publictransportapp.helpers.ApplicationExtension;
import com.flurry.android.FlurryAgent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button planJourneyButton;
    private Button learnMoreButton;
    private Button advancedButton;
    private Button mPlanCommuteButton;
    private Button mTaxiQueuesButton;
    private Button mFireBase;
    private Button mFireBaseFull;
    private ImageView mMainIcon;
    private DatabaseReference mDatabaseReference;

    private void init() {
        mMainIcon = (ImageView) findViewById(R.id.mainIcon);

        ApplicationExtension app = (ApplicationExtension) getApplicationContext();
        if (app.getItinerary() != null) {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            mMainIcon.startAnimation(pulse);
            mMainIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent push = new Intent(MainActivity.this, ItineraryViewActivity.class);
                    push.putExtra("isReload", true);

                    startActivity(push);
                }
            });
        }

        planJourneyButton = (Button) findViewById(R.id.planJourney);
        planJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("PlanJourney");

                Intent push = new Intent(MainActivity.this, WhereToActivity.class);

                startActivity(push);
            }
        });

        learnMoreButton = (Button) findViewById(R.id.learnMore);
        learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent push = new Intent(MainActivity.this, LearnMoreActivity.class);

                startActivity(push);
            }
        });

        advancedButton = (Button) findViewById(R.id.advanced);
        advancedButton.setPaintFlags(advancedButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent push = new Intent(MainActivity.this, AdvancedOptionsActivity.class);

                startActivity(push);
            }
        });

        mPlanCommuteButton = (Button) findViewById(R.id.planCommute);
        mPlanCommuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("PlanCommute");

                Intent push = new Intent(MainActivity.this, PlanCommuteActivity.class);

                startActivity(push);
            }
        });

        mTaxiQueuesButton = (Button) findViewById(R.id.taxiQueues);
        mTaxiQueuesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAgent.logEvent("TaxiQueues");

                Intent push = new Intent(MainActivity.this, TaxiQueues.class);

                startActivity(push);
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFireBase = (Button) findViewById(R.id.firebase);
        mFireBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QueueState queueState = new QueueState();
                queueState.setUid(mDatabaseReference.child("queues").push().getKey());
                queueState.setDate(new Date().getTime());
                queueState.setRankId("RankId1");
                queueState.setState("normal");
                mDatabaseReference.child("queues").child(queueState.getUid()).setValue(queueState);
            }
        });

        mFireBaseFull = (Button) findViewById(R.id.firebasefull);
        mFireBaseFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QueueState queueState = new QueueState();
                queueState.setUid(mDatabaseReference.child("queues").push().getKey());
                queueState.setDate(new Date().getTime());
                queueState.setRankId("RankId1");
                queueState.setState("long");
                mDatabaseReference.child("queues").child(queueState.getUid()).setValue(queueState);
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected  void onResume(){
        super.onResume();

        ApplicationExtension app = (ApplicationExtension) getApplicationContext();
        if (app.getItinerary() != null) {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            mMainIcon.startAnimation(pulse);
            mMainIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent push = new Intent(MainActivity.this, ItineraryViewActivity.class);
                    push.putExtra("isReload", true);

                    startActivity(push);
                }
            });
        }
    }
}
