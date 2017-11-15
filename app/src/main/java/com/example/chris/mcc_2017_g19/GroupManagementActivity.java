package com.example.chris.mcc_2017_g19;


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

public class GroupManagementActivity extends AppCompatActivity {

    private static final String TAG = "GroupManagementActivity";
    private DatabaseReference mDatabase;
    private Button createGroupButton;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() { //TODO singleValueEvent preferred?
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
        addUser(user); //TODO Temporary, move (see addUser() below)

        createGroupButton = (Button)findViewById(R.id.buttonCreateGroup);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addGroup();
                } catch (IllegalArgumentException e){
                    Toast.makeText(GroupManagementActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); //TODO Placeholder toast, add meaningful notification
                    ; //TODO Toast?
                }
            }
        });
    }

    private void addGroup() throws IllegalArgumentException {
        EditText nameField = (EditText) findViewById(R.id.fieldGroupName); //TODO Handle name getting outside function to bypass empty check here?
        String nameValue = nameField.getText().toString();
        if (nameValue.isEmpty())
            throw new IllegalArgumentException("No group name provided");
        GroupObject group = new GroupObject(nameValue);

        // Completion listeners?
        mDatabase.child("groups").child(nameValue).setValue(group); //TODO Register by GroupID?
        mDatabase.child("groups").child(nameValue).child("group_members").push().setValue(user.getUid());
        mDatabase.child("users").child(user.getUid()).child("group").setValue(nameValue);
        Toast.makeText(GroupManagementActivity.this, "Group added to database", Toast.LENGTH_SHORT).show();
    }

    private void addUser(FirebaseUser firebaseUser) { //TODO Note: here for testing purposes, this should probably happen when signing up new user with FireAuth
        String name = firebaseUser.getUid();
        UserObject user = new UserObject(name);
        mDatabase.child("users").child(name).setValue(user);
    }
}
