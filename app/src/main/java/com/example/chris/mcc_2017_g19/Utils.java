package com.example.chris.mcc_2017_g19;


import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class Utils {
    private static FirebaseDatabase firebaseDatabase;

    public static FirebaseDatabase getDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }
        return firebaseDatabase;
    }

    //Returns the root of the group image albums
    public static String getAlbumsRoot(Context context) {
        Resources resources = context.getResources();
        String appName = resources.getString(R.string.app_name);
        String albumsFolder = resources.getString(R.string.albums_name);
        String path = Environment.getExternalStorageDirectory() + File.separator + appName + File.separator + albumsFolder;

        File albumsRoot = new File(path);
        boolean success = false;
        if (!albumsRoot.exists()){
            success = albumsRoot.mkdirs();
            if (!success)
                Log.d("Utils", "Unexpected error: could not create folder for albums");
        }
        return path;
    }

    //Checking if the application has network connectivity
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
