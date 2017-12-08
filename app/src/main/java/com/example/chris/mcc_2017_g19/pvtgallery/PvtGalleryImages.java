package com.example.chris.mcc_2017_g19.pvtgallery;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.chris.mcc_2017_g19.Utils;

import java.io.File;
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
        final String[] projection = {MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = {PvtGalleryImages.getBucketId(Utils.getAlbumsRoot(context) + File.separator + "Private")};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<GridItem> result = new ArrayList<GridItem>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            do {
                GridItem galleryItem = new GridItem(cursor.getString(dataColumn));
                result.add(galleryItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;

    }
}
