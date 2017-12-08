package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.chris.mcc_2017_g19.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by alessiospallino on 18/11/2017.
 *
 * This class helps to get images from device storage from OrganizeApp folder.
 *
 */

public class GalleryImages {

    //Define bucket name from which you want to take images Example '/DCIM/Camera' for camera images
    private static String CAMERA_IMAGE_BUCKET_NAME;

    public GalleryImages(String path, Context context){
        CAMERA_IMAGE_BUCKET_NAME = Utils.getAlbumsRoot(context) + File.separator + path;
    }

    //method to get id of image bucket from path
    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    //method to get images
    public ArrayList<GridImageItem> getImages(Context context) {
        final String[] projection = {MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = {GalleryImages.getBucketId(CAMERA_IMAGE_BUCKET_NAME)};
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<GridImageItem> result = new ArrayList<GridImageItem>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            do {
                GridImageItem galleryItem = new GridImageItem(cursor.getString(dataColumn));
                result.add(galleryItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
}
