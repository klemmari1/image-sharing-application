package com.example.chris.mcc_2017_g19;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Chris on 6.11.2017.
 */

public class MemberAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> members;

    public MemberAdapter(Context context, List<String> values) {
        super(context, R.layout.member_item, values);
        this.context = context;
        this.members = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View memberView = inflater.inflate(R.layout.member_item, parent, false);
        TextView textView = (TextView) memberView.findViewById(R.id.member_item);
        textView.setText(members.get(position));

        return memberView;
    }
}
