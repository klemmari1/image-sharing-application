package com.example.chris.mcc_2017_g19.AlbumsView;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.AlbumInfo;
import com.example.chris.mcc_2017_g19.pvtgallery.PrivateGallery;
import com.example.chris.mcc_2017_g19.R;

import java.util.ArrayList;
import java.util.List;

public class AlbumsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        List<ItemObject> allItems = getAllItemObject();










        CustomAdapter customAdapter = new CustomAdapter(AlbumsActivity.this, allItems);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(AlbumsActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();

                if(position == 0){

                    Intent intent = new Intent(AlbumsActivity.this, PrivateGallery.class);
                    startActivity(intent);
                    finish();

                }

                if(position == 1){

                    Intent intent = new Intent(AlbumsActivity.this, AlbumInfo.class);
                    startActivity(intent);
                    finish();

                }
            }
        });
    }


    private List<ItemObject> getAllItemObject(){
        ItemObject itemObject = null;
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject("Private", "one", "cloudoff", ""));
        items.add(new ItemObject("Image Two", "two", "cloud", "1"));
        items.add(new ItemObject("Image Three", "three","cloud","1"));
        items.add(new ItemObject("Image Four", "four", "cloud", "1"));
        items.add(new ItemObject("Image Five", "five", "cloud", "1"));

        return items;
    }
}
