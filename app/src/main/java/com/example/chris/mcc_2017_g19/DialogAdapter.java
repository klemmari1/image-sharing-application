package com.example.chris.mcc_2017_g19;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DialogAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> members;

    public DialogAdapter(Context context, List<String> values) {
        super(context, R.layout.dialog_object, values);
        this.context = context;
        this.members = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_object, parent, false);
        TextView textView = (TextView) dialogView.findViewById(R.id.dialog_text);
        textView.setText(members.get(position));

        ImageView imageView = (ImageView) dialogView.findViewById(R.id.dialog_image);

        if(members.get(position).equals("JOIN A GROUP")){
            Drawable join_icon = this.context.getResources().getDrawable(R.drawable.join);
            imageView.setImageDrawable(join_icon);
        }
        else{
            Drawable create_icon = this.context.getResources().getDrawable(R.drawable.create);
            imageView.setImageDrawable(create_icon);
        }
        return dialogView;
    }
}
