package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.GalleryImages;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.GridImageItem;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.HeaderImageItem;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.ImageItem;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.Sorting;
import com.example.chris.mcc_2017_g19.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AlbumInfo extends AppCompatActivity {

    private static final int DEFAULT_SPAN_COUNT = 3;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private GridAlbumInfoViewAdapter mAdapter;
    private Sorting sorting = Sorting.PEOPLE;

    //List where we will put object to insert in the Grid
    private List<ImageItem> mImageItemList;
    private List<GridImageItem> mGridImageItemList;
    // In this list I will save all the images before adding them to the adapter
    List<String> list;
    private static final int RC_READ_STORAGE = 5;
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);
        path = getIntent().getStringExtra("PATH");

        mImageItemList = new ArrayList<>();
        mGridImageItemList = new ArrayList<>();

        configureViews();

        loadImagesActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_sort)
        {
            if(sorting == Sorting.PEOPLE)
                sorting = Sorting.PHOTOGRAPHER;
            else
                sorting = Sorting.PEOPLE;
            mAdapter.clear();
            loadImagesActivity();
        }
        return true;
    }


    private void configureViews(){
        mRecyclerView = (RecyclerView) this.findViewById(R.id.rec);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new GridAlbumInfoViewAdapter(this, mImageItemList, gridLayoutManager, DEFAULT_SPAN_COUNT);
        mRecyclerView.setAdapter(mAdapter);

        //addImageList();

        //new AsyncHttpTask().execute(FEED_URL);

    }


    private void loadImagesActivity(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadImages();
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
                loadImages();
            } else {
                Toast.makeText(this, "Storage Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadImages(){
        GalleryImages gi = new GalleryImages(path, getApplicationContext());
        mGridImageItemList = gi.getImages(this);
        if(sorting == Sorting.PEOPLE)
            loadByPeople();
        else
            loadByPhotographer();
    }


    private void loadByPeople(){
        //Get images
        //Set images inside the adapter and check when user click to one image
        mAdapter.addItem(new HeaderImageItem("People"));
        addPeopleImages("1");

        mAdapter.addItem(new HeaderImageItem("No People"));
        addPeopleImages("0");

        //mAdapter.addItem();
    }


    private void addPeopleImages(String flag){
        for(GridImageItem item: mGridImageItemList){
            String[] nameArray = item.getItemTitle().split("/");
            nameArray = nameArray[nameArray.length-1].split("_");
            if(nameArray.length >= 2){
                String people = nameArray[2];
                if(people.equals(flag)){
                    mAdapter.addItem(item);
                }
            }
        }
    }


    private void loadByPhotographer(){
        //Get images
        Map<String,ArrayList<GridImageItem>> photographerImages = new HashMap<>();
        for(GridImageItem item: mGridImageItemList){
            String[] nameArray = item.getItemTitle().split("/");
            nameArray = nameArray[nameArray.length-1].split("_");
            if(nameArray.length >= 2){
                String name = nameArray[1];
                if(photographerImages.get(name) == null)
                    photographerImages.put(name, new ArrayList<GridImageItem>());
                photographerImages.get(name).add(item);
            }
        }
        for(Map.Entry<String, ArrayList<GridImageItem>> entry : photographerImages.entrySet()){
            mAdapter.addItem(new HeaderImageItem(entry.getKey()));
            for(ImageItem photo: entry.getValue()){
                mAdapter.addItem(photo);
            }
        }
    }

/*
    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(httpResponse.getEntity().getContent());

                    //GO to parse the json and take URLs
                    parseResult(response);
                    result = 1; // Successful







                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d("Error", e.getLocalizedMessage());
            }
            return result;
        }
        String streamToString(InputStream stream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            // Close stream
            if (null != stream) {
                stream.close();
            }
            return result;
        }
        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            if (result == 1) {
                //mGridAdapter.setGridData(mGridData);

                mAdapter.addItem(new HeaderImageItem("Header 1"));


                for (int i=0;i < list.size();i++)
                {
                    mAdapter.addItem(new GridImageItem(list.get(i)));
                }



                mAdapter.addItem(new HeaderImageItem("Header 2"));


                System.out.println("I made it here");
            } else {
                Toast.makeText(AlbumInfo.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void parseResult(String result) {


        list = new ArrayList<String>();

        try {
            JSONObject response = new JSONObject(result);
            JSONArray array = response.getJSONArray("backdrops");

            for (int i = 0; i < array.length(); i++) {
                JSONObject imageobject = array.getJSONObject(i);
                String image_url = imageobject.getString("file_path");

                String uri = "http://image.tmdb.org/t/p/w500"+image_url;
                list.add(uri.toString());


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void addImageList() {
        mAdapter.addItem(new HeaderImageItem("Header 1"));

        mAdapter.addItem(new GridImageItem("http://www.terranuova.it/var/terranuova/storage/images/il-mensile/pane-al-pane/1294525-1-ita-IT/Pane-al-pane_articleimage.jpg"));
        mAdapter.addItem(new GridImageItem("http://www.terranuova.it/var/terranuova/storage/images/il-mensile/pane-al-pane/1294525-1-ita-IT/Pane-al-pane_articleimage.jpg"));
        mAdapter.addItem(new GridImageItem("http://www.terranuova.it/var/terranuova/storage/images/il-mensile/pane-al-pane/1294525-1-ita-IT/Pane-al-pane_articleimage.jpg"));
        mAdapter.addItem(new GridImageItem("http://www.terranuova.it/var/terranuova/storage/images/il-mensile/pane-al-pane/1294525-1-ita-IT/Pane-al-pane_articleimage.jpg"));
        mAdapter.addItem(new GridImageItem("http://www.terranuova.it/var/terranuova/storage/images/il-mensile/pane-al-pane/1294525-1-ita-IT/Pane-al-pane_articleimage.jpg"));
        mAdapter.addItem(new GridImageItem("http://www.terranuova.it/var/terranuova/storage/images/il-mensile/pane-al-pane/1294525-1-ita-IT/Pane-al-pane_articleimage.jpg"));

        mAdapter.addItem(new HeaderImageItem("Header 2"));

    }*/


}