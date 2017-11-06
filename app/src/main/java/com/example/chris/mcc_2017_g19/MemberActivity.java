package com.example.chris.mcc_2017_g19;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

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
    }

    public void onClick(View v) {
        Intent intent = new Intent(this, MemberQRActivity.class);
        startActivity(intent);
    }

    public List<String> getMembers() {
        return members;
    }
}
