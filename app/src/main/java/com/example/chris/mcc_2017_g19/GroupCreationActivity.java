package com.example.chris.mcc_2017_g19;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;


public class GroupCreationActivity extends AppCompatActivity {

    private static final String TAG = "GroupCreationActivity";
    private DatabaseReference databaseReference;
    private Button createGroupButton;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Called any time data is added to database reference
                Log.d(TAG, "Value is: " + snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        createGroupButton = (Button)findViewById(R.id.buttonCreateGroup);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addGroup();
                    startActivity(new Intent(GroupCreationActivity.this, MainActivity.class));
                } catch (IllegalArgumentException e){
                    Toast.makeText(GroupCreationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onClick(android.view.View v){
        addGroup();
    }

    //TODO: MOVE handling to backend
    private void addGroup() throws IllegalArgumentException {
        EditText nameField = (EditText) findViewById(R.id.fieldGroupName);
        String groupName = nameField.getText().toString();
        if (groupName.isEmpty())
            throw new IllegalArgumentException("No group name provided");
        GroupObject group = new GroupObject(groupName, user.getUid());

       //okhttp request: create_group
        BackendAPI api = new BackendAPI();
        String token = api.createGroup(groupName, user.getUid());
    }
}