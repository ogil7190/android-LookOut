package com.mycampusdock.dock.utils;

import android.app.Application;
import android.os.Build;

import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.console;

import java.net.URISyntaxException;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatApplication extends Application {
    public static final String SESSION_ID = UUID.randomUUID().toString();
    private Socket socket;
    {
        try {
            socket = IO.socket(Config.URL_CHAT_SERVER);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        console.setContext(getApplicationContext());
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
       String  details =  "VERSION.RELEASE : "+ Build.VERSION.RELEASE
                +"\nVERSION.INCREMENTAL : "+Build.VERSION.INCREMENTAL
                +"\nVERSION.SDK.NUMBER : "+Build.VERSION.SDK_INT
                +"\nBRAND : "+Build.BRAND
                +"\nHARDWARE : "+Build.HARDWARE
                +"\nMANUFACTURER : "+Build.MANUFACTURER
                +"\nMODEL : "+Build.MODEL
                +"\nPRODUCT : "+Build.PRODUCT
                +"\nUSER : "+Build.USER;
        console.error("Device id : "+details);
    }

    @Override
    public void onTerminate() {
        console.log(" <<<<<< Session :"+SESSION_ID + " Closed >>>>>>");
        super.onTerminate();
    }

    public Socket getSocket() {
        return socket;
    }
}