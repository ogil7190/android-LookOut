package com.mycampusdock.dock.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class LocalStore {
    private static RequestQueue queue;
    public static RequestQueue getRequestQueueInstance(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
            return queue;
        }
        return queue;
    }
}
