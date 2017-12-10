package com.example.chris.mcc_2017_g19.BackgroundServices;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.chris.mcc_2017_g19.Connectivity.Connectivity;
import com.example.chris.mcc_2017_g19.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SyncImagesService extends IntentService {

    private static final String TAG = "SyncImagesService";


    public SyncImagesService(){
        super("SyncImagesService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        String groupID = intent.getStringExtra("groupID");
        syncImageFolder(groupID);
    }

    //Syncing images to gallery with the quality specified in the settings
    public void syncImageFolder(final String groupID) {
        final DatabaseReference databaseReference = Utils.getDatabase().getReference();
        DatabaseReference imagesReference = databaseReference.child("groups").child(groupID);
        imagesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot groupSnapshot) {

                //get local imageIDs first:
                final String groupName = (String) groupSnapshot.child("name").getValue();
                List<String> localImageIdList = getLocalImageIDs(groupID, groupName);

                List<String> remoteImageIdList = new ArrayList<String>();
                for (DataSnapshot imageSnapshot: groupSnapshot.child("images").getChildren()) {
                    remoteImageIdList.add(imageSnapshot.getKey());
                    Log.d(TAG,"added to remote image list: " + imageSnapshot.getKey() + ":"+ groupID);
                }

                //loop through remote imageIDs and download if new found
                for (final String remoteImageID : remoteImageIdList) {
                    if (!localImageIdList.contains(remoteImageID)) {
                        Log.d(TAG,"Found new remote image to be synced! remoteImageID: wasnt found locally " + remoteImageID);

                        //get url based on quality min(localMaxQ,remoteMaxQ)
                        ////get max quality as int
                        String remoteMaxQ = (String) groupSnapshot.child("images").child(remoteImageID).child("maxQuality").getValue();
                        Log.d(TAG, "remoteMaxQ: " + remoteMaxQ);

                        String LTE;
                        String WIFI;
                        String localMaxQ = "errorQ";


                        if (Connectivity.isConnectedMobile(SyncImagesService.this)) {
                            LTE = PreferenceManager
                                    .getDefaultSharedPreferences(SyncImagesService.this)
                                    .getString("LTEpicturevalue","");
                            if (LTE.toLowerCase().contains("low"))
                                localMaxQ = "low";
                            if (LTE.toLowerCase().contains("high"))
                                localMaxQ = "high";
                            if (LTE.toLowerCase().contains("full"))
                                localMaxQ = "full";
                        }
                        else if (Connectivity.isConnectedWifi(SyncImagesService.this)) {
                            WIFI =PreferenceManager
                                    .getDefaultSharedPreferences(SyncImagesService.this)
                                    .getString("WIFIpicturevalue","");
                            if (WIFI.toLowerCase().contains("low"))
                                localMaxQ = "low";
                            if (WIFI.toLowerCase().contains("high"))
                                localMaxQ = "high";
                            if (WIFI.toLowerCase().contains("full"))
                                localMaxQ = "full";
                        }
                        else {
                            Log.d(TAG,"ERROR CONNECTION STATUS. WIFI/MOBILE?");
                        }

                        String finalQ;
                        if (qualityAsInt(localMaxQ) >= qualityAsInt(remoteMaxQ)) {
                            finalQ = remoteMaxQ;
                        }
                        else {
                            finalQ = localMaxQ;
                        }

                        if (!finalQ.equals("low") && !finalQ.equals("high") && !finalQ.equals("full")) {
                            finalQ = "low";
                            Log.d(TAG,"ERROR IN GETTING the final max quality: Setting it to low! finalQ was: "+finalQ);
                        }
                        //get url for the image
                        String url = (String) groupSnapshot.child("images").child(remoteImageID).child((String) finalQ + "URL").getValue();
                        Log.d(TAG, "URL for databaseref / Dl'ing image: " + url);
                        Log.d(TAG, "finalQ: " + finalQ);

                        //download from url as bitmap
                        //Bitmap newBitmap = getBitmapFromURL(url); this doesnt work, mainthread röplem

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference httpsReference = storage.getReferenceFromUrl(url);


                        final long MAX_SIZE = 50*1024*1024;

                        String photoOwnerID = (String) groupSnapshot.child("images").child(remoteImageID).child("userID").getValue();

                        DatabaseReference photographerUserRef = databaseReference.child("users").child(photoOwnerID);
                        photographerUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {

                                String photoOwner = (String) userSnapshot.child("name").getValue();
                                long hasFaces = (long) groupSnapshot.child("images").child(remoteImageID).child("hasFaces").getValue();
                                String fname = remoteImageID + "_" + photoOwner + "_" + hasFaces + "_.jpg";
                                String path =  Utils.getAlbumsRoot(SyncImagesService.this) + File.separator + groupName + "_" + groupID;
                                File directory = new File(path);

                                try {
                                    File newFile = new File(directory, fname);
                                    httpsReference.getFile(newFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Log.d(TAG,"Downloaded an image! /w ID: " + remoteImageID);
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG,"error in getting photographer name" + databaseError.getDetails() + databaseError.getMessage());
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"database error: " + databaseError.getDetails() + databaseError.getMessage());
            }
        });
    }

    public List<String> getLocalImageIDs(String groupID, String groupName) {

        String path = Utils.getAlbumsRoot(this) + File.separator + groupName + "_" + groupID;
        File yourDir = new File(path);

        Log.d(TAG,"yourDIr path is: "+ yourDir.getAbsolutePath());

        if (!yourDir.exists()) {
            yourDir.mkdirs();
            Log.d(TAG,"Now a directory should be created");
        }

        List<String> IDs = new ArrayList<String>();
        String filename;
        try {
            Log.d(TAG, "local image id list:");
            for (File f : yourDir.listFiles()) {
                if (f.isFile()) {
                    filename = f.getName();
                    //1234_a.b@c.com_0_.jpg
                    IDs.add(filename.split("_")[0]);
                    //IDs.add(filename);

                    Log.d(TAG, filename.split("_")[0]);
                }
            }

        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
        }

        return IDs;
    }

    public int qualityAsInt(String someQuality) {
        if (someQuality.equals("low")) {
            return 0;
        }
        else if (someQuality.equals("high")) {
            return 1;
        }
        else if (someQuality.equals("full")) {
            return 2;
        }
        else {
            Log.d(TAG,"ERROR IN qualityAsInt function: someQuality: " + someQuality);
            return 0;
        }
    }


}
