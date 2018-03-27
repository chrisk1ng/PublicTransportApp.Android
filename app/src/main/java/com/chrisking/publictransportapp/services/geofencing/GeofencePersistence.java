package com.chrisking.publictransportapp.services.geofencing;

import android.content.Context;
import android.content.SharedPreferences;

import com.chrisking.publictransportapp.classes.NamedGeofence;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ChrisKing on 2017/07/01.
 */

public class GeofencePersistence {
    public static final String LOCATION_TYPE = "location";
    public static final String HOME_TYPE = "home";
    public static final String WORK_TYPE = "work";

    private Gson mGson;
    private SharedPreferences mPrefs;

    public GeofencePersistence(Context mContext){
        mGson = new Gson();
        mPrefs = mContext.getSharedPreferences("Geofences", Context.MODE_PRIVATE);
    }

    public List<NamedGeofence> getNamedGeofences() {
        List<NamedGeofence> namedGeofences = new ArrayList<>();
        // Loop over all geofence keys in prefs and add to namedGeofences
        Map<String, ?> keys = mPrefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String jsonString = mPrefs.getString(entry.getKey(), null);
            NamedGeofence namedGeofence = mGson.fromJson(jsonString, NamedGeofence.class);
            namedGeofences.add(namedGeofence);
        }

        return namedGeofences;
    }

    public List<String> getGeofenceIds() {
        List<String> geofenceIds = new ArrayList<>();
        // Loop over all geofence keys in prefs and add to namedGeofences
        Map<String, ?> keys = mPrefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String jsonString = mPrefs.getString(entry.getKey(), null);
            NamedGeofence namedGeofence = mGson.fromJson(jsonString, NamedGeofence.class);
            geofenceIds.add(namedGeofence.id);
        }

        return geofenceIds;
    }

    public void clearType(String type){
        List<NamedGeofence> namedGeofences = getNamedGeofences();
        SharedPreferences.Editor editor = mPrefs.edit();

        for(NamedGeofence namedGeofence : namedGeofences){
            if (namedGeofence.type.equals(type)){
                editor.remove(namedGeofence.id);
            }
        }

        editor.apply();
    }

    public void clearAll(){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();
        editor.apply();
    }

    public void remove(String id){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(id);
        editor.apply();
    }

    public void addNamedGeofence(NamedGeofence namedGeofence){
        String json = mGson.toJson(namedGeofence);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(namedGeofence.id, json);
        editor.apply();
    }

    public NamedGeofence find(String id){
        String jsonString = mPrefs.getString(id, null);
        return mGson.fromJson(jsonString, NamedGeofence.class);
    }
}
