package com.example.chris.mcc_2017_g19.AlbumsView;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.AlbumInfoActivity;
import com.example.chris.mcc_2017_g19.Utils;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.pvtgallery.PrivateGalleryActivity;
import com.example.chris.mcc_2017_g19.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/*
This activity is for the main gallery view.
 */
public class AlbumsActivity extends AppCompatActivity {

    private static final String TAG = "AlbumsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        GridView gridview = (GridView) findViewById(R.id.gridview);


        final List<ItemObject> allItems = getAllItemObject();

        CustomAdapter customAdapter = new CustomAdapter(AlbumsActivity.this, allItems);
        gridview.setAdapter(customAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(AlbumsActivity.this, "Position: " + position, Toast.LENGTH_SHORT);

                if(allItems.get(position).getWholeName().equals("Private")){

                    Intent intent = new Intent(AlbumsActivity.this, PrivateGalleryActivity.class);
                    startActivity(intent);

                }
                else{
                    String path = allItems.get(position).getWholeName();
                    Intent intent = new Intent(AlbumsActivity.this, AlbumInfoActivity.class);
                    intent.putExtra( "PATH", path);
                    startActivity(intent);
                }
            }
        });
    }

    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        File albumsFolder = new File(Utils.getAlbumsRoot(getApplicationContext()));

        List<ItemObject> albums = getAlbumData(albumsFolder);
        items.addAll(albums);

        return items;
    }


    private List<ItemObject> getAlbumData(File albumsFolder) {
        List<ItemObject> albumsList = new ArrayList<>();
        if(albumsFolder.listFiles() != null){
            Log.d(TAG, "Albums in path: " + albumsFolder.listFiles().length);

            File[] albums = albumsFolder.listFiles();
            for(File album: albums) {
                if (album.isDirectory()) {
                    Log.d(TAG, "Album: " + album.getName());

                    String albumName = album.getName();
                    int picturesInAlbum = 0;
                    String labelImage = null;

                    File[] files = album.listFiles();
                    for (int j=0; j < files.length; j++) {
                        File file = files[j];
                        if (fileIsValid(file)) {
                            picturesInAlbum++;
                            if (j == 0) {
                                labelImage = file.getName();
                            }
                        }

                    }
                    String cloud = "cloud";
                    if(albumName.equals("Private"))
                        cloud = "cloudoff";
                    albumsList.add(new ItemObject(albumName.split("_")[0], albumName, labelImage, cloud, String.valueOf(picturesInAlbum)));

                } else {
                    Log.d(TAG, "Unexpected error: Found file instead of a folder");
                }
            }
        }

        return albumsList;
    }


    private boolean fileIsValid(File image) {
        if (image.isFile()) {
            String filename = image.getName();
            Log.d(TAG, "File: " + filename);
            int extensionStart = filename.lastIndexOf('.');
            if (extensionStart > 0 && filename.substring(extensionStart + 1).equals("jpg"))
                return true;
        }

        return false;
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


    //fileformat: imageid_username_faces_.jpg
    private void createTestFiles(File album) {
        try {
            for (int x=0; x < 10; x++) {
                File f = new File(album + "/" + x + "_user_1_.jpg");
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
