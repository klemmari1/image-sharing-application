package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
        setContentView(R.layout.activity_member);
//        invalidateOptionsMenu();

        members = new ArrayList<String>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        ListView memberList = (ListView) findViewById(R.id.group_info_member_list);
        memberAdapter = new MemberAdapter(this, members);
        memberList.setAdapter(memberAdapter);

        //To be removed
//        members.add("Lisa");
//        members.add("Mark");
//        members.add("Joe");
//        memberAdapter.notifyDataSetChanged();
        displayGroupName();

        TextView expirationValue = (TextView) findViewById(R.id.group_info_expiration_value);
        expirationValue.setText("Tue 31 Oct - 10:00pm");
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, MemberQRActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO Modify actionbar button title is current user is the creator
//        super.onCreateOptionsMenu(menu);
//        checkIfUserIsGroupCreator();
//        if (userIsGroupCreator)
//            menu.findItem(R.menu.leave_button).setTitle("Delete group");
//        getMenuInflater().inflate(R.menu.leave_button, menu);
//        return true;
        getMenuInflater().inflate(R.menu.leave_button, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public List<String> getMembers() {
        return members;
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

    public void displayGroupName() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserObject user = dataSnapshot.child("users").child(firebaseUser.getUid()).getValue(UserObject.class);
                TextView groupNameField = (TextView) findViewById(R.id.group_info_name_value);
                    groupNameField.setText(user.getGroup());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }
}
