package com.mycampusdock.dock.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

public class GlideHelperUpscale extends CenterCrop {

    public GlideHelperUpscale(BitmapPool bitmapPool) {

    }

    public GlideHelperUpscale(Context context) {

    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getHeight() > outHeight || toTransform.getWidth() > outWidth) {
            return super.transform(pool, toTransform, outWidth, outHeight);
        } else {
            return toTransform;
        }
    }
}