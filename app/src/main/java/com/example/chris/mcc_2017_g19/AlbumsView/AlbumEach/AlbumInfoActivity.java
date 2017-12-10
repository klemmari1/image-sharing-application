package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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


/*
This activity shows the images inside group folders
 */
public class AlbumInfoActivity extends AppCompatActivity {

    private static final int DEFAULT_SPAN_COUNT = 3;
    private RecyclerView mRecyclerView;
    private GridAlbumInfoViewAdapter mAdapter;
    private Sorting sorting = Sorting.PEOPLE;

    //List where we will put object to insert in the Grid. Includes headers and images.
    private List<ImageItem> mImageItemList;
    //List of images
    private List<GridImageItem> mGridImageItemList;
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
        //Get images sorted by people/no people
        mAdapter.addItem(new HeaderImageItem("People"));
        addPeopleImages("1");

        mAdapter.addItem(new HeaderImageItem("No People"));
        addPeopleImages("0");
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
        //Get images sorted by the photographer
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
            //Add a header for each photographer
            mAdapter.addItem(new HeaderImageItem(entry.getKey()));
            for(ImageItem photo: entry.getValue()){
                mAdapter.addItem(photo);
            }
        }
    }

}