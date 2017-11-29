package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chris.mcc_2017_g19.BackendAPI.BackendAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupStatusActivity extends AppCompatActivity {

    private List<String> members;
    private MemberAdapter memberAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private boolean userIsGroupCreator;

    private static final String TAG = "GroupStatusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_status);

        members = new ArrayList<String>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        ListView memberList = (ListView) findViewById(R.id.group_status_member_list);
        memberAdapter = new MemberAdapter(this, members);
        memberList.setAdapter(memberAdapter);

        String user_id = firebaseUser.getUid();
        BackendAPI api = new BackendAPI();
        api.getUserGroup(user_id, new BackendAPI.HttpCallback() {
            @Override
            public void onFailure(String response, Exception exception) {
            }
            @Override
            public void onSuccess(String response) {
                try{
                    JSONObject groupInfo = new JSONObject(response);
                    final String groupName = groupInfo.getString("name");
                    JSONObject membs = groupInfo.getJSONObject("members");
                    Iterator<?> keys = membs.keys();
                    while (keys.hasNext()) {
                        Object key = keys.next();
                        String name = membs.getString((String) key);
                        members.add(name);
                    }
                    GroupStatusActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try{
                                memberAdapter.notifyDataSetChanged();
                                displayGroupName(groupName);
                            }
                            catch (Exception e){
                            }
                        }
                    });
                }
                catch (Exception e){
                }
            }
        });
        checkIfUserIsGroupCreator();

        TextView expirationValue = (TextView) findViewById(R.id.group_status_expiration_value);
        expirationValue.setText("Tue 31 Oct - 10:00pm"); // Placeholder
    }

    public void addButton(View v) {
        Intent QRActivity = new Intent(this, GroupQRActivity.class);
        startActivity(QRActivity);
    }



    //TODO onClick (Actionbar: leave)
    //TODO Call leaveGroup()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leave_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public List<String> getMembers() {
        return members;
    }

    public void leaveGroup() {
        //TODO Add query: really wish to leave/delete group?

        //TODO okhttp: leave_group
    }

    public void checkIfUserIsGroupCreator() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserObject user = dataSnapshot.child("users").child(firebaseUser.getUid()).getValue(UserObject.class);
                String userGroup = user.getGroup();
                GroupObject group = dataSnapshot.child("groups").child(userGroup).getValue(GroupObject.class);
                if (group.getCreator().equals(firebaseUser.getUid()))
                    userIsGroupCreator = true;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    public void displayGroupName(String name) {
        TextView groupNameField = (TextView) findViewById(R.id.group_status_name_value);
        groupNameField.setText(name);
    }
}
