package com.example.chris.mcc_2017_g19;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.example.chris.mcc_2017_g19.BackgroundSync.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GroupStatusActivity extends AppCompatActivity {

    private MemberAdapter memberAdapter;
    private boolean userIsGroupCreator;
    private boolean isFinalized;
    List<String> members;

    private static final String TAG = "GroupStatusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_status);

        ListView memberList = (ListView) findViewById(R.id.group_status_member_list);
        members = GroupObject.getMembers();
        memberAdapter = new MemberAdapter(this, members);
        memberList.setAdapter(memberAdapter);

        final DatabaseReference databaseReference = Utils.getDatabase().getReference();
        DatabaseReference memberReference = databaseReference.child("groups").child(UserObject.getGroup()).child("members");
        memberReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                if(snapshot != null){
                    try{
                        members.clear();
                        for (DataSnapshot member : snapshot.getChildren()) {
                            members.add((String) member.getValue());
                        }

                        memberAdapter.notifyDataSetChanged();
                    }
                    catch (Exception e){
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });

        displayGroupName(GroupObject.getName());
        displayGroupExpiration(GroupObject.getExpiration());
        checkIfUserIsGroupCreator(GroupObject.getCreator());
    }

    public void addButton(View v) {
        Intent QRActivity = new Intent(this, GroupQRActivity.class);
        startActivity(QRActivity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leave_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_leave)
        {
            leaveGroup();
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (isFinalized) {
            menu.findItem(R.id.action_leave).setEnabled(false);
        }
        return true;
    }

    public void leaveGroup() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button pressed: Leave or delete the group
                        isFinalized = true;
                        findViewById(R.id.group_status_add).setEnabled(false);
                        deleteOrLeave();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button pressed: Do nothing
                        break;
                }
            }
        };
        if(userIsGroupCreator){
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupStatusActivity.this);
            builder.setMessage("Are you sure you want to delete your group?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupStatusActivity.this);
            builder.setMessage("Are you sure you want to leave your current group?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

    }

    private void deleteOrLeave(){
        BackendAPI api = new BackendAPI();

        if(userIsGroupCreator){
            api.deleteGroup(UserObject.getGroup(), new BackendAPI.HttpCallback() {
                @Override
                public void onFailure(String response, Exception exception) {
                    Log.i(TAG, "Error: " + response + " " + exception);
                }

                @Override
                public void onSuccess(String response) {
                    GroupStatusActivity.this.finish();
                    Log.i(TAG, "Error: " + response);
                }
            });
        }
        else{

            api.leaveGroup(UserObject.getId(), UserObject.getGroup(), new BackendAPI.HttpCallback() {
                @Override
                public void onFailure(String response, Exception exception) {
                    Log.d(TAG, "Error: " + response + " " + exception);
                }

                @Override
                public void onSuccess(String response) {GroupStatusActivity.this.finish();
                }
            });
        }
    }

    private void checkIfUserIsGroupCreator(String creator) {
        if(creator.equals(UserObject.getId())){
            userIsGroupCreator = true;
        }
    }

    private void displayGroupName(String name) {
        TextView groupNameField = (TextView) findViewById(R.id.group_status_name_value);
        groupNameField.setText(name);
    }

    private void displayGroupExpiration(String expiration) {
        TextView groupExpirationField = (TextView) findViewById(R.id.group_status_expiration_value);
        groupExpirationField.setText(expiration);
    }
}
