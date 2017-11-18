package com.example.chris.mcc_2017_g19.pvtgallery;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


/**
 * Created by alessiospallino on 18/11/2017.
 *
 * This class used to get width and height of screen.
 * Actually we did not need to create this class, we can get screen
 * width and height directly on the activity when needed. To keep things organized
 * we are creating this class.
 */

public class ScreenUtils {

    //static method to get screen width
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    //static method to get screen height
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
