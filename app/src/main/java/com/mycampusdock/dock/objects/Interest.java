package com.mycampusdock.dock.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Interest extends RealmObject {
    @PrimaryKey
    String name;
    private String extra;
    private int type;

    public Interest(){

    }

    public Interest(String name, String exra, int type) {
        this.name = name;
        this.extra = exra;
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExra(String extra) {
        this.extra = extra;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {

        return name;
    }

    public String getExtra() {
        return extra;
    }

    public int getType() {
        return type;
    }
}
