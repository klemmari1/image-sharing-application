package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.pvtgallery;

import android.content.Context;

import com.example.chris.mcc_2017_g19.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by alessiospallino on 18/11/2017.
 *
 * This class helps to get images from device storage to the private folder.
 *
 */

public class PvtGalleryImages {


    //method to get images
    public static ArrayList<GridItem> getImages(Context context) {
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
