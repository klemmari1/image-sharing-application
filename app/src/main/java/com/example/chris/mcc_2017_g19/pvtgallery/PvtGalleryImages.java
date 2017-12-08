package com.example.chris.mcc_2017_g19.pvtgallery;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.GridImageItem;
import com.example.chris.mcc_2017_g19.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by alessiospallino on 18/11/2017.
 *
 * This class helps to get images from device storage from OrganizeApp folder.
 *
 */

public class PvtGalleryImages {

    //Define bucket name from which you want to take images Example '/DCIM/Camera' for camera images

    //method to get id of image bucket from path
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    //method to get images
    public static ArrayList<GridItem> getImages(Context context) {

        final String[] EXTENSIONS = new String[]{
                "jpg"
        };
        // filter to identify images based on their extensions
        final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        return (true);
                    }
                }
                return (false);
            }
        };
        ArrayList<GridItem> result = new ArrayList<GridItem>();
        File dir = new File(Utils.getAlbumsRoot(context) + File.separator + "Private");
        File[] filelist = dir.listFiles(IMAGE_FILTER );
        for (File f : filelist)
        {
            result.add(new GridItem(f.toString()));
        }
        return result;
    }
}
