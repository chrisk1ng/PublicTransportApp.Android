package com.chrisking.publictransportapp.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class UniqueContext {
    private SharedPreferences mPrefs;

    public UniqueContext(Context mContext){
        mPrefs = mContext.getSharedPreferences("UniqueContext", Context.MODE_PRIVATE);
    }

    public String getUniqueContext() {
        return mPrefs.getString("UniqueContext", null);
    }

    public void setUniqueContext(){
        if (getUniqueContext() != null)
            return;

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("UniqueContext", java.util.UUID.randomUUID().toString());
        editor.apply();
    }
}
