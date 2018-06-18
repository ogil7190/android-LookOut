package com.mycampusdock.dock.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Bulletin extends RealmObject {
    @PrimaryKey
    @Index
    String bulletinId;
    private String title;
    private String description;
    private String creator;
    private RealmList<RealmData> files = new RealmList<>();
    private RealmList<ChatMessage> chats = new RealmList<>();
    private boolean isDummy;
    private long updatedOn;
    private boolean haveNewChanges;
    private String session_synchronised;
    private boolean isImportant;
    private boolean isReached;
    private boolean isViewed;
    public Bulletin() {
    }

    public Bulletin(String bulletinId, String title, String description, String creator, RealmList<RealmData> files, RealmList<ChatMessage> chats, long updatedOn) {
        this.bulletinId = bulletinId;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.files = files;
        this.chats = chats;
        this.updatedOn = updatedOn;
    }

    public Bulletin parseFromJSON(JSONObject obj) throws JSONException {
        setBulletinId(obj.getString("bulletin_id"));
        setCreator(obj.getString("creator"));
        setTitle(obj.getString("bulletin_title"));
        setDescription(obj.getString("bulletin_description"));
        JSONArray files = obj.getJSONArray("bulletin_media");
        RealmList<RealmData> list = new RealmList<>();
        for (int i = 0; i < files.length(); i++) {
            list.add(new RealmData(files.getString(i)));
        }
        setFiles(list);
        setUpdatedOn(obj.getLong("creation_time"));
        try {
            if (obj.getBoolean("important")) {
                setImportant(true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isReached() {
        return isReached;
    }

    public void setReached(boolean reached) {
        isReached = reached;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getSession_synchronised() {
        return session_synchronised;
    }

    public void setSession_synchronised(String session_synchronised) {
        this.session_synchronised = session_synchronised;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void insertChat(ChatMessage message) {
        if (chats != null) {
            chats.add(message);
        } else {
            chats = new RealmList<>();
            chats.add(message);
        }
    }

    public void clearChats() {
        if (chats == null) {
            chats = new RealmList<>();
        } else {
            chats.clear();
        }
    }

    public boolean isHaveNewChanges() {
        return haveNewChanges;
    }

    public void setHaveNewChanges(boolean haveNewChanges) {
        this.haveNewChanges = haveNewChanges;
    }

    public boolean isDummy() {
        return isDummy;
    }

    public String getBulletinId() {
        return bulletinId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator() {
        return creator;
    }

    public List<RealmData> getFiles() {
        return files;
    }

    public RealmList<ChatMessage> getChats() {
        return chats;
    }

    public void setBulletinId(String bulletinId) {
        this.bulletinId = bulletinId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setFiles(RealmList<RealmData> files) {
        this.files = files;
    }

    public void setChats(RealmList<ChatMessage> chats) {
        this.chats = chats;
    }

    public void setDummy(boolean dummy) {
        isDummy = dummy;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }
}
