package com.example.chris.mcc_2017_g19.BackgroundSync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.chris.mcc_2017_g19.GroupObject;
import com.example.chris.mcc_2017_g19.UserObject;
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "JAA");

        try{
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            UserObject.setId(firebaseUser.getUid());
            final DatabaseReference databaseReference = Utils.getDatabase().getReference();

            userReference = databaseReference.child("users").child(firebaseUser.getUid());
            userReference.keepSynced(true);

            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.i(TAG, "JAA");

                    UserObject.setName((String) snapshot.child("name").getValue());
                    UserObject.setGroup((String) snapshot.child("group").getValue());

                    if(UserObject.getGroup() != null){
                        String group_id = UserObject.getGroup();
                        DatabaseReference groupRef = databaseReference.child("groups").child(group_id);
                        groupRef.keepSynced(true);
                        groupRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot != null){
                                    GroupObject.setName((String) dataSnapshot.child("name").getValue());
                                    GroupObject.setCreator((String) dataSnapshot.child("creator").getValue());
                                    GroupObject.setExpiration((String) dataSnapshot.child("expiration").getValue());
                                    GroupObject.setToken((String) dataSnapshot.child("token").getValue());

                                    DataSnapshot membersSnapshot = dataSnapshot.child("members");
                                    ArrayList<String> members = new ArrayList<>();
                                    for (DataSnapshot member : membersSnapshot.getChildren()) {
                                        members.add((String) member.getValue());
                                    }
                                    GroupObject.setMembers(members);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        userReference.onDisconnect();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}