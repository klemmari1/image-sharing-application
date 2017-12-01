package com.example.chris.mcc_2017_g19;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


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
                } catch (Exception e){
                    Toast.makeText(GroupCreationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void addGroup() throws IllegalArgumentException {
        EditText nameField = (EditText) findViewById(R.id.fieldGroupName);
        String groupName = nameField.getText().toString();
        if (groupName.isEmpty())
            throw new IllegalArgumentException("No group name provided");

        EditText durationField = (EditText) findViewById(R.id.fieldGroupDuration);
        String groupDuration = durationField.getText().toString();
//        try {
//            validateDuration(groupDuration);
//        } catch (IllegalArgumentException ie) {
//            ie.printStackTrace();
//        }
        int validDuration = validateDuration(groupDuration);
        // TODO Handle timestamp creation here on in API class? atm in API

        findViewById(R.id.buttonCreateGroup).setEnabled(false);

        //okhttp request: create_group
        BackendAPI api = new BackendAPI();
        api.createGroup(groupName, validDuration, user.getUid(), new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
                Log.d(TAG, "Error: " + response + " " + exception);
            }

            @Override
            public void onSuccess(String response) {
                try {

                    Intent groupStatus = new Intent(GroupCreationActivity.this, GroupStatusActivity.class);
                    String group_id = response.split(":")[0];
                    groupStatus.putExtra("GROUP_ID", group_id);
                    startActivity(groupStatus);
                    GroupCreationActivity.this.finish();
                } catch (Exception e){
                    Toast.makeText(GroupCreationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int validateDuration(String inputDuration) throws IllegalArgumentException {
        try {
            if (inputDuration.isEmpty())
                throw new IllegalArgumentException("Give group duration");
            int duration = Integer.parseInt(inputDuration);
            if (duration <= 0)
                throw new IllegalArgumentException("Group duration must be greater than zero");
            return duration;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Give a valid duration that contains only numbers");
        }
    }
}
