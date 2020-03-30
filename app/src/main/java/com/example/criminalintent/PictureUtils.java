package com.example.criminalintent;

import android.app.Activity;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PictureUtils {
    public static Bitmap getScaledBitmap(String path, int destWith, int destHeight){
        //Read in the dimensions of image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        //figure how much to scale down by
        int intSampleSize = 1;
        if (srcHeight > destHeight || destWith > destWith){
            if (srcWidth > srcHeight){
                intSampleSize = Math.round(srcHeight / destHeight);
            }else {
                intSampleSize = Math.round(srcWidth / destWith);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = intSampleSize;

        //Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity){
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }
}
