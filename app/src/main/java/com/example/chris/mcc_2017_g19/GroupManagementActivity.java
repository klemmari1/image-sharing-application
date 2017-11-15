package com.example.chris.mcc_2017_g19;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupManagementActivity extends AppCompatActivity {

    private static final String TAG = "GroupManagementActivity";
    private DatabaseReference mDatabase;
    private Button createGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
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

        //Testing
        //mDatabase
        createGroupButton = (Button)findViewById(R.id.buttonCreateGroup);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = "Test1"; //TODO Replace with value from name field
                try {
                    addGroup(groupName);
                } catch (IllegalArgumentException e){
                    Toast.makeText(GroupManagementActivity.this, "Give a proper group name", Toast.LENGTH_SHORT).show(); //TODO Placeholder toast, add meaningful notification
                    ; //TODO Toast?
                }
            }
        });

        //Check whether current user is in a group or not
    }

    private void addGroup(String groupName) throws IllegalArgumentException {
        EditText nameField = (EditText) findViewById(R.id.fieldGroupName); //TODO Handle name getting outside function to bypass empty check here?
        String nameValue = nameField.getText().toString();
        if (nameValue.isEmpty())
            throw new IllegalArgumentException("No group name provided");
        GroupObject group = new GroupObject(groupName);
        mDatabase.child("groups").child(groupName).setValue(group); //TODO Register by GroupID?
        Toast.makeText(GroupManagementActivity.this, "Group added to database", Toast.LENGTH_SHORT).show();
    }
}
