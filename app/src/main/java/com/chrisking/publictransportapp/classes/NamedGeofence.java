package com.chrisking.publictransportapp.classes;

import android.support.annotation.NonNull;

/**
 * Created by ChrisKing on 2017/07/01.
 */

public class NamedGeofence implements Comparable {

    // region Properties

    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public String type;
    public boolean isSubscribed = false;
    public int numTimesAskedToSubscribe = 0;

    // end region

    // region Public

    // endregion

    // region Comparable

    @Override
    public int compareTo(@NonNull Object another) {
        NamedGeofence other = (NamedGeofence) another;
        return name.compareTo(other.name);
    }

    // endregion
}

