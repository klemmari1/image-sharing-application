package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MemberActivity extends AppCompatActivity {

    private List<String> members;
    private MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        members = new ArrayList<String>();

        ListView memberList = (ListView) findViewById(R.id.group_info_member_list);
        memberAdapter = new MemberAdapter(this, members);
        memberList.setAdapter(memberAdapter);

        //To be removed
        members.add("Lisa");
        members.add("Mark");
        members.add("Joe");
        memberAdapter.notifyDataSetChanged();

        TextView nameValue = (TextView) findViewById(R.id.group_info_name_value);
        TextView expirationValue = (TextView) findViewById(R.id.group_info_expiration_value);

        nameValue.setText("Dummy Group");
        expirationValue.setText("Tue 31 Oct - 10:00pm");
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, MemberQRActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leave_button, menu);

        MenuItem item = menu.findItem(R.id.action_leave);

        return super.onCreateOptionsMenu(menu);
    }

    public List<String> getMembers() {
        return members;
    }
}
