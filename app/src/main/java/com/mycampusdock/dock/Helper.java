package com.mycampusdock.dock;


import android.content.Context;

import com.mycampusdock.dock.utils.ColorGenerator;

public class Helper {
    public static int getColorFor(String key, Context context){
        ColorGenerator generator = ColorGenerator.MATERIAL;
        return context.getResources().getColor(generator.getColor(key));
    }

    public static int getColorResourceFor(String key){
        ColorGenerator generator = ColorGenerator.MATERIAL;
        return generator.getColor(key);
    }
}
