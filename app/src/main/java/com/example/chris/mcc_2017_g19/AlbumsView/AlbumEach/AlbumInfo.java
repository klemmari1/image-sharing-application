package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.GridImageItem;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.HeaderImageItem;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.ImageItem;
import com.example.chris.mcc_2017_g19.R;
import com.example.chris.mcc_2017_g19.pvtgallery.GridItem;

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

    //List where we will put object to insert in the Grid
    private List<ImageItem> mImageItemList = new ArrayList<>();
    // In this list I will save all the images before adding them to the adapter
    List<String> list;
    private String FEED_URL = "http://api.themoviedb.org/3/movie/157336/images?api_key=8496be0b2149805afa458ab8ec27560c";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        configureViews();


    }

    private void configureViews(){
        mRecyclerView = (RecyclerView) this.findViewById(R.id.rec);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT );

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new GridAlbumInfoViewAdapter(this, mImageItemList,gridLayoutManager, DEFAULT_SPAN_COUNT);
        mRecyclerView.setAdapter(mAdapter);

        //addImageList();

        new AsyncHttpTask().execute(FEED_URL);

    }





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

    /*private void addImageList() {
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