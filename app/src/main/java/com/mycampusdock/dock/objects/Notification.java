package com.mycampusdock.dock.objects;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Notification extends RealmObject {
    @PrimaryKey
    String notificationId;
    private String creator;
    private String data;
    private long updatedOn;
    private boolean isDummy;
    private boolean isReached;

    public Notification(String notificationId, String creator, String data, long updatedOn) {
        this.creator = creator;
        this.data = data;
        this.notificationId = notificationId;
        this.updatedOn = updatedOn;
    }

    public Notification() {
    }

    public Notification parseFromJSON(JSONObject obj) throws JSONException{
        this.creator = obj.getString("creator");
        this.data = obj.getString("notification_description");
        this.notificationId = obj.getString("notification_id");
        this.updatedOn = obj.getLong("creation_time");
        return this;
    }

    public void setDummy(boolean dummy) {
        isDummy = dummy;
    }

    public boolean isDummy() {

        return isDummy;
    }

    public boolean isReached() {
        return isReached;
    }

    public void setReached(boolean reached) {
        isReached = reached;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotificationId() {

        return notificationId;
    }

    public String getCreator() {
        return creator;
    }

    public String getData() {
        return data;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setData(String data) {
        this.data = data;
    }


}
