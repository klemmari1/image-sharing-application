package com.example.chris.mcc_2017_g19;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
            }
        });
    }


    private List<ItemObject> getAllItemObject(){
        ItemObject itemObject = null;
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject("Image One", "one", "cloud"));
        items.add(new ItemObject("Image Two", "two", "cloud"));
        items.add(new ItemObject("Image Three", "three","cloud"));
        items.add(new ItemObject("Image Four", "four", "cloud"));
        items.add(new ItemObject("Image Five", "five", "cloud"));

        return items;
    }
}
