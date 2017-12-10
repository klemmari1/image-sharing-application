package com.example.chris.mcc_2017_g19;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class GroupCreationActivity extends AppCompatActivity {

    private static final String TAG = "GroupCreationActivity";
    private Button createGroupButton;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

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
        int validDuration = validateDuration(groupDuration);
        String expirationTimestamp = generateTimestamp(validDuration);

        findViewById(R.id.buttonCreateGroup).setEnabled(false);

        //Request to the backend to create the group
        if(Utils.isNetworkAvailable(getApplicationContext())){
            BackendAPI api = new BackendAPI();
            api.createGroup(groupName, expirationTimestamp, new BackendAPI.HttpCallback() {
                @Override
                public void onFailure(String response, Exception exception) {
                    Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String response) {
                    try {
                        if(!response.toLowerCase().contains("error")){
                            //Start GroupStatusActivity with the new group
                            Intent groupStatus = new Intent(GroupCreationActivity.this, GroupStatusActivity.class);
                            startActivity(groupStatus);
                            GroupCreationActivity.this.finish();
                        }
                        else{
                            findViewById(R.id.buttonCreateGroup).setEnabled(true);
                            Toast.makeText(GroupCreationActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e){
                        Toast.makeText(GroupCreationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
        }
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

    private String generateTimestamp(int duration) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(timeZone);
        calendar.add(Calendar.MINUTE, duration);
        return dateFormat.format(calendar.getTime());
    }
}