package com.chrisking.publictransportapp.classes;

/**
 * Created by ChrisKing on 2017/06/25.
 */

public class QueueState {
    private String state;
    private String uid;
    private long date;
    private String rankId;

    public String getUid() {
        return uid;
    }

    public void setUid(String value) {
        this.uid = value;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long value) {
        this.date = value;
    }

    public String getRankId() {
        return rankId;
    }

    public void setRankId(String value) {
        this.rankId = value;
    }

    public String getState() {
        return state;
    }

    public void setState(String value) {
        this.state = value;
    }
}
