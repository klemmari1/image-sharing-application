package com.example.chris.mcc_2017_g19;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class GroupStatusActivity extends AppCompatActivity {

    private List<String> members;
    private MemberAdapter memberAdapter;
    private FirebaseUser firebaseUser;
    private boolean userIsGroupCreator;
    private String group_id;
    private DatabaseReference databaseReference;
    private boolean isFinalized;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_status);

        members = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = Utils.getDatabase().getReference();

        ListView memberList = (ListView) findViewById(R.id.group_status_member_list);
        memberAdapter = new MemberAdapter(this, members);
        memberList.setAdapter(memberAdapter);

        if(Utils.isNetworkAvailable(getApplicationContext())){
            //Getting group info from firebase
            DatabaseReference userRef = databaseReference.child("users").child(firebaseUser.getUid());
            userRef.keepSynced(true);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    group_id = (String) dataSnapshot.child("group").getValue();
                    if(group_id != null){
                        DatabaseReference groupRef = databaseReference.child("groups").child(group_id);
                        groupRef.keepSynced(true);
                        groupRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GroupObject groupObj = dataSnapshot.getValue(GroupObject.class);
                                if(groupObj != null){
                                    //Update view values
                                    displayGroupName(groupObj.getName());
                                    displayGroupExpiration(groupObj.getExpiration());
                                    if (groupObj.isExpired()) {
                                        displayExpiredText();
                                        disableAddMemberButton();
                                    }
                                    checkIfUserIsGroupCreator(groupObj.getCreator());

                                    DataSnapshot membersSnapshot = dataSnapshot.child("members");
                                    members.clear();
                                    for (DataSnapshot member : membersSnapshot.getChildren()) {
                                        members.add((String) member.getValue());
                                    }
                                    memberAdapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
        }
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
            if(Utils.isNetworkAvailable(getApplicationContext()))
                leaveGroup();
            else
                Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
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

    public List<String> getMembers() {
        return members;
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
            api.deleteGroup(group_id, new BackendAPI.HttpCallback() {
                @Override
                public void onFailure(String response, Exception exception) {
                    Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String response) {
                    GroupStatusActivity.this.finish();
                }
            });
        }
        else{

            api.leaveGroup(group_id, new BackendAPI.HttpCallback() {
                @Override
                public void onFailure(String response, Exception exception) {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String response) {
                    GroupStatusActivity.this.finish();
                }
            });
        }
    }

    private void checkIfUserIsGroupCreator(String creator) {
        if(creator.equals(firebaseUser.getUid())){
            userIsGroupCreator = true;
        }
    }

    private void displayGroupName(String name) {
        TextView groupNameField = (TextView) findViewById(R.id.group_status_name_value);
        groupNameField.setText(name);
    }

    private void displayGroupExpiration(String expiration) {
        TextView groupExpirationField = (TextView) findViewById(R.id.group_status_expiration_value);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            cal.setTime(sdf.parse(expiration));
        } catch (ParseException pe)
        {
            pe.printStackTrace();
        }
        int tz = TimeZone.getDefault().getRawOffset();
        if (tz != 0)
            cal.add(Calendar.MILLISECOND, tz);
        groupExpirationField.setText(sdf.format(cal.getTime()));
    }

    private void displayExpiredText() {
        TextView expiredText = (TextView) findViewById(R.id.group_status_expired_text);
        expiredText.setText(R.string.group_status_expired_text);
    }

    private void disableAddMemberButton() {
        Button addMemberButton = (Button) findViewById(R.id.group_status_add);
        addMemberButton.setClickable(false);
    }
}
