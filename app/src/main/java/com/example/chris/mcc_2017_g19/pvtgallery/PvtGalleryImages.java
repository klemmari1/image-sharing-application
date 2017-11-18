package com.example.chris.mcc_2017_g19.pvtgallery;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiospallino on 18/11/2017.
 *
 * This class helps to get images from device storage from OrganizeApp folder.
 *
 */

public class PvtGalleryImages {

    //Define bucket name from which you want to take images Example '/DCIM/Camera' for camera images
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/OrganizerApp";

    //method to get id of image bucket from path
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    //method to get images
    public static List<PvtGalleryItem> getImages(Context context) {
        final String[] projection = {MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = {PvtGalleryImages.getBucketId(CAMERA_IMAGE_BUCKET_NAME)};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<PvtGalleryItem> result = new ArrayList<PvtGalleryItem>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            do {
                PvtGalleryItem galleryItem = new PvtGalleryItem(cursor.getString(dataColumn));
                result.add(galleryItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;

    }
}
