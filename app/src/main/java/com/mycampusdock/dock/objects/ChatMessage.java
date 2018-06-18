package com.mycampusdock.dock.objects;

import com.mycampusdock.dock.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class ChatMessage extends RealmObject {
    @PrimaryKey
    long timestamp;
    private String sender;
    private String senderName;
    private String receiver;
    private String message;
    private int type;

    @Index
    String relatedTo;

    public ChatMessage(String senderName, String sender, String receiver, long timestamp, String message, String relatedTo, int type) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.message = message;
        this.type = type;
        this.relatedTo = relatedTo;
    }

    public ChatMessage() {

    }


    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public ChatMessage parseFromJSON(JSONObject obj, String id, String target) throws JSONException {
        String email = obj.getString("email");
        if (email.equals(target)) {
            setType(101);
        } else {
            setType(102);
        }
        setSender(email);
        try {
            setMessage(obj.getString("message"));
        } catch (Exception e) {
            setMessage(obj.getString("data"));
        }
        setSenderName(obj.getString("username"));
        setTimestamp(Utils.fromISO8601UTC(obj.getString("timestamp")).getTime());
        setRelatedTo(id);
        return this;
    }

    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    public String getRelatedTo() {

        return relatedTo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
