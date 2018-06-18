package com.mycampusdock.dock.objects;

import io.realm.RealmObject;

public class RealmData extends RealmObject {
    private String data;
    private String extra;

    public RealmData(String data, String extra) {
        this.data = data;
        this.extra = extra;
    }

    public RealmData(String data) {
        this.data = data;
    }

    public RealmData() {
    }

    public String getData() {
        return data;
    }

    public String getExtra() {
        return extra;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
