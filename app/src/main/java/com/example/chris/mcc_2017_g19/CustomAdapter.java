package com.example.chris.mcc_2017_g19;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private List<ItemObject> listStorage;
    private Context context;

    public CustomAdapter(Context context, List<ItemObject> customizedListView) {
        this.context = context;
        layoutinflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder listViewHolder;
        if(convertView == null){
            listViewHolder = new ViewHolder();
            convertView = layoutinflater.inflate(R.layout.activity_custom_adapter, parent, false);
            listViewHolder.textInListView = (TextView)convertView.findViewById(R.id.title);
            listViewHolder.imageInListView = (ImageView)convertView.findViewById(R.id.imageView);
            listViewHolder.imageCloudInListView = (ImageView)convertView.findViewById(R.id.imagecloud);
            convertView.setTag(listViewHolder);
        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        /* populate the basic info of each album (image,title,cloud or not)*/
        listViewHolder.textInListView.setText(listStorage.get(position).getTitle());
        int imageResourceId = this.context.getResources().getIdentifier(listStorage.get(position).getImageResource(), "drawable", this.context.getPackageName());
        listViewHolder.imageInListView.setImageResource(imageResourceId);
        int imageCloudId = this.context.getResources().getIdentifier(listStorage.get(position).getCloud(), "drawable", this.context.getPackageName());
        listViewHolder.imageCloudInListView.setImageResource(imageCloudId);

        return convertView;
    }

    static class ViewHolder{
        TextView textInListView;
        ImageView imageInListView;
        ImageView imageCloudInListView;
    }

}
