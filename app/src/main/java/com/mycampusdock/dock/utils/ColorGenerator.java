package com.mycampusdock.dock.utils;

import com.mycampusdock.dock.R;

import java.util.Arrays;
import java.util.List;

public class ColorGenerator {

    public static ColorGenerator MATERIAL;

    static {
        List<Integer> list = Arrays.asList(
                R.color.material_skin,
                R.color.material_blue,
                R.color.material_cyan,
                R.color.material_orange,
                R.color.material_light_blue,
                R.color.material_yellow,
                R.color.material_light_blue,
                R.color.material_lemon,
                R.color.material_orange,
                R.color.material_cyan,
                R.color.material_dark_green,
                R.color.material_red,
                R.color.material_purple,
                R.color.material_orange,
                R.color.material_baby_pink,
                R.color.material_light_green
        );
        MATERIAL = create(list);
    }

    private final List<Integer> mColors;

    public static ColorGenerator create(List<Integer> colorList) {
        return new ColorGenerator(colorList);
    }

    private ColorGenerator(List<Integer> colorList) {
        mColors = colorList;
    }

    public int getColor(Object key) {
        return mColors.get(Math.abs(key.hashCode()) % mColors.size());
    }
}
