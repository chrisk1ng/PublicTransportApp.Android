package com.chrisking.publictransportapp.activities.city;

import android.content.Context;
import android.content.SharedPreferences;

import com.chrisking.publictransportapp.classes.City;
import com.google.gson.Gson;

public class CityPersistence {
    private Gson mGson;
    private SharedPreferences mPrefs;

    public CityPersistence(Context mContext){
        mGson = new Gson();
        mPrefs = mContext.getSharedPreferences("City", Context.MODE_PRIVATE);
    }

    public City getSavedCity() {
        String jsonString = mPrefs.getString("SavedCity", null);

        return mGson.fromJson(jsonString, City.class);
    }

    void saveCity(City city){
        String json = mGson.toJson(city);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("SavedCity", json);
        editor.apply();
    }
}
