package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.chris.mcc_2017_g19.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by alessiospallino on 18/11/2017.
 *
 * This class helps to get images from device storage to the group folders.
 *
 */

public class GalleryImages {

    //Define bucket name from which you want to take images Example '/DCIM/Camera' for camera images
    private static String CAMERA_IMAGE_BUCKET_NAME;

    public GalleryImages(String path, Context context){
        CAMERA_IMAGE_BUCKET_NAME = Utils.getAlbumsRoot(context) + File.separator + path;
    }

    //method to get images
    public ArrayList<GridImageItem> getImages(Context context) {
        final String[] EXT = new String[]{
                "jpg"
        };
        // filter to identify only .jpg images
        final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                for (final String ext : EXT) {
                    if (name.endsWith("." + ext)) {
                        return (true);
                    }
                }
                return (false);
            }
        };
        ArrayList<GridImageItem> result = new ArrayList<GridImageItem>();
        File dir = new File(CAMERA_IMAGE_BUCKET_NAME);
        File[] filelist = dir.listFiles(IMAGE_FILTER );
        for (File f : filelist)
        {
            result.add(new GridImageItem(f.toString()));
        }
        return result;
    }
}
