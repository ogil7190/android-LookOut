package com.mycampusdock.dock;

import android.content.Context;
import android.util.Log;

public class console {
    private static Context context;

    public static void setContext(Context cntxt){
        context = cntxt;
    }

    public static void log(String mssg) {
        Log.d(context.getPackageName(), mssg);
    }

    public static void error(String mssg) {
        Log.e(context.getPackageName(), mssg);
    }
}
