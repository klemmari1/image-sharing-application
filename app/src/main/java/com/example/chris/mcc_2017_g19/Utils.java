package com.example.chris.mcc_2017_g19;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class Utils extends Activity {
    private static FirebaseDatabase firebaseDatabase;

    public static FirebaseDatabase getDatabase() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }
        return firebaseDatabase;
    }

    public static File getAlbumsRoot(Context context) {
        Resources resources = context.getResources();
        String appName = resources.getString(R.string.app_name);
        String albumsFolder = resources.getString(R.string.albums_name);

        File albumsRoot = new File(Environment.getExternalStorageDirectory() + File.separator + appName + File.separator + albumsFolder);
        boolean success = false;
        if (!albumsRoot.exists())
            success = albumsRoot.mkdirs();
        if (!success)
            Log.d("Utils", "Unexpected error: could not create folder for albums");
        return albumsRoot;
    }
}