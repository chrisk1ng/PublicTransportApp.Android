package com.chrisking.publictransportapp.classes;

/**
 * Created by ChrisKing on 2018/03/28.
 */

public class TripShare {
    private String uid;
    private String latitude;
    private String longitude;
    private long datetime;

    public String getUid() {
        return uid;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long value) {
        this.datetime = value;
    }

    public void setUid(String value) {
        this.uid = value;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String value) {
        this.latitude = value;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String value) {
        this.longitude = value;
    }
}
