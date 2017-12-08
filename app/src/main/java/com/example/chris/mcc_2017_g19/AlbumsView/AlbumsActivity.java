package com.example.chris.mcc_2017_g19.AlbumsView;


import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.AlbumInfo;
import com.example.chris.mcc_2017_g19.pvtgallery.PrivateGallery;
import com.example.chris.mcc_2017_g19.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumsActivity extends AppCompatActivity {

    private static final String TAG = "AlbumsActivity";
    private final String testFolderRoot = "Test";

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
                Toast.makeText(AlbumsActivity.this, "Position: " + position, Toast.LENGTH_SHORT);

                if(position == 0){

                    Intent intent = new Intent(AlbumsActivity.this, PrivateGallery.class);
                    startActivity(intent);

                }
                else{
                    Intent intent = new Intent(AlbumsActivity.this, AlbumInfo.class);
                    intent.putExtra( "PATH", "/Test");
                    startActivity(intent);
                }
            }
        });
    }


    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        File albumsFolder = new File(Environment.getExternalStorageDirectory() + File.separator + testFolderRoot);

        boolean success = false;
        if (!albumsFolder.exists())
            success = albumsFolder.mkdirs();
        if (!success)
            Log.d(TAG, "Unexpected error: could not create folder for albums");

        createTestFolders(albumsFolder);
        createTestFiles(new File(albumsFolder + File.separator + "testalbum1"));
        createTestFiles(new File(albumsFolder + File.separator + "testalbum2"));
        createTestFiles(new File(albumsFolder + File.separator + "testalbum3"));

        List<File> albums = getAlbumData(albumsFolder);

        items.add(new ItemObject("Private", "one", "cloudoff", ""));
        items.add(new ItemObject("Image Two", "two", "cloud", "1"));
        items.add(new ItemObject("Image Three", "three","cloud","1"));
        items.add(new ItemObject("Image Four", "four", "cloud", "1"));
        items.add(new ItemObject("Image Five", "five", "cloud", "1"));

        return items;
    }


    private List<File> getAlbumData(File albumsFolder) {
        List<File> albumsList = new ArrayList<>();
        Log.d(TAG, "Albums in path: " + albumsFolder.listFiles().length);

        File[] albums = albumsFolder.listFiles();
        for (int i=0; i < albums.length; i++) {
            File album = albums[i];
            if (album.isDirectory()) {
                Log.d(TAG, "Album: " + album.getName());
                File[] files = album.listFiles();
                for (int j=0; j < files.length; j++) {
                    File file = files[j];
                    if (file.isFile()) {
                        Log.d(TAG, "File: " + file.getName());
                    } else {
                        Log.d(TAG, "Unexpected error: Found folder instead of a file");
                    }
                }
            } else {
                Log.d(TAG, "Unexpected error: Found file instead of a folder");
            }
        }

        return albumsList;
    }


    private void createTestFolders(File albumRoot) {
        File album1 = new File(albumRoot + File.separator + "testalbum1");
        File album2 = new File(albumRoot + File.separator + "testalbum2");
        File album3 = new File(albumRoot + File.separator + "testalbum3");

        boolean success = false;
        if (!album1.exists())
            success = album1.mkdirs();
        if (!success)
            Log.d(TAG, "Unexpected error: could not create folder for albums");

        if (!album2.exists())
            success = album2.mkdirs();
        if (!success)
            Log.d(TAG, "Unexpected error: could not create folder for albums");

        if (!album3.exists())
            success = album3.mkdirs();
        if (!success)
            Log.d(TAG, "Unexpected error: could not create folder for albums");

    }

    private void createTestFiles(File album) {
        try {
            for (int x=0; x < 10; x++) {
                File f = new File(album + "/test" + x + ".txt");
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();

                FileOutputStream out = new FileOutputStream(f);

                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
