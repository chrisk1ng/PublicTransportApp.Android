package com.chrisking.publictransportapp.helpers;

import android.app.Application;
import android.content.Context;

import com.chrisking.publictransportapp.classes.UniqueContext;
import com.flurry.android.FlurryAgent;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import transportapisdk.models.Itinerary;


public class ApplicationExtension extends Application {
    private boolean mIsCommute = false;
    private boolean mIsCommuteHome = false;
    private boolean backgroundServiceRunning = false;
    private Itinerary mItinerary;
    private ArrayList<Itinerary> mItineraries;
    private static Context mContext;
    private String tripShareId;
    private static String uniqueContextId;

    public static String UniqueContextId() { return uniqueContextId; }
    public String getTripShareId() {return tripShareId;}
    public boolean getIsBackgroundServiceRunning() {return backgroundServiceRunning;}
    public boolean getIsCommute() {return mIsCommute;}
    public boolean getIsCommuteHome() {return mIsCommuteHome;}
    public Itinerary getItinerary() {return mItinerary;}
    public ArrayList<Itinerary> getItineraries() {return mItineraries;}
    public static String ClientId(){ return Credentials.ClientId; }
    public static String ClientSecret(){
        return Credentials.ClientSecret;
    }

    public void setIsBackgroundServiceRunning(boolean isRunning){ this.backgroundServiceRunning = isRunning; }
    public void setIsCommute(boolean isCommute, boolean isCommuteHome) {this.mIsCommute = isCommute; this.mIsCommuteHome = isCommuteHome;}
    public void setItinerary(Itinerary itinerary) {this.mItinerary = itinerary;}
    public void setItineraries(ArrayList<Itinerary> itineraries) {this.mItineraries = itineraries;}
    public void setTripShareId(String tripShareId) {this.tripShareId = tripShareId;}

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        if (!FirebaseApp.getApps(this).isEmpty())
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        UniqueContext uniqueContext = new UniqueContext(mContext);
        uniqueContext.setUniqueContext();
        uniqueContextId = uniqueContext.getUniqueContext();

        new FlurryAgent.Builder()
                .build(this, Credentials.FlurryKey);
    }

    public static Context getContext(){
        return mContext;
    }
}