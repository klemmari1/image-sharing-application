package com.example.chris.mcc_2017_g19.pvtgallery;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.GridView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.MainActivity;
import com.example.chris.mcc_2017_g19.R;
import com.example.chris.mcc_2017_g19.pvtgallery.GridItem;
import com.example.chris.mcc_2017_g19.pvtgallery.GridViewAdapter;
import com.example.chris.mcc_2017_g19.pvtgallery.PvtGalleryImages;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.Manifest;


public class PrivateGallery extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private GridView mGridView;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private String FEED_URL = "http://api.themoviedb.org/3/movie/157336/images?api_key=8496be0b2149805afa458ab8ec27560c";
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/OrganizerApp";

    private static final int RC_READ_STORAGE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_gallery);

        mGridView = (GridView) findViewById(R.id.gridView);
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);



        //new AsyncHttpTask().execute(FEED_URL);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //Get images
            mGridData = PvtGalleryImages.getImages(this);
            //Set images inside the adapter and check when user click to one image
            mGridAdapter.setGridData(mGridData);

        } else {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_READ_STORAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Get images
                mGridData = PvtGalleryImages.getImages(this);
                //Set images inside the adapter and check when user click to one image
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(this, "Storage Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
