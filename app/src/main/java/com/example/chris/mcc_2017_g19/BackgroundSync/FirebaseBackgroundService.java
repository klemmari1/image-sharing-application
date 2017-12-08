package com.example.chris.mcc_2017_g19.BackgroundSync;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.example.chris.mcc_2017_g19.GroupObject;
import com.example.chris.mcc_2017_g19.UserObject;
import com.example.chris.mcc_2017_g19.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseBackgroundService extends Service {


    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;
    private static final String TAG = "BackgroundService";
    private static int NOTIFICATION_ID=1337;
    private UserObject userObj;
    private GroupObject groupObj;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID,
                getNotification());
        try{
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference databaseReference = Utils.getDatabase().getReference();

            userReference = databaseReference.child("users").child(firebaseUser.getUid());
            userReference.keepSynced(true);

            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    userObj = snapshot.getValue(UserObject.class);
                    userObj.setId(firebaseUser.getUid());

                    if(userObj.getGroup() != null){
                        String group_id = userObj.getGroup();
                        DatabaseReference groupRef = databaseReference.child("groups").child(group_id);
                        groupRef.keepSynced(true);
                        groupRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot != null){
                                    groupObj = dataSnapshot.getValue(GroupObject.class);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getMessage());
                }
            });
        }
        catch (Exception e){
        }
        return START_STICKY;
    }


    private Notification getNotification() {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);

        b.setOngoing(true)
                .setContentTitle("Syncing data")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        return(b.build());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}