package com.example.chris.mcc_2017_g19.AlbumsView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chris.mcc_2017_g19.R;
import com.example.chris.mcc_2017_g19.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/*
Adapter for showing the images in the gallery grid
 */
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
            listViewHolder.textNumberPerson = (TextView)convertView.findViewById(R.id.number);
            convertView.setTag(listViewHolder);
        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        /* populate the basic info of each album (image,title,cloud or not, number of people)*/
        listViewHolder.textInListView.setText(listStorage.get(position).getTitle());
        String albumAndFile = listStorage.get(position).getWholeName() + File.separator + listStorage.get(position).getImageResource();
        File image = new File(Utils.getAlbumsRoot(this.context) + File.separator + albumAndFile);
        //Scale the image before showing it
        Picasso.with(context).load(image).resize(500, 500).into(listViewHolder.imageInListView);

        int imageCloudId = this.context.getResources().getIdentifier(listStorage.get(position).getCloud(), "drawable", this.context.getPackageName());
        listViewHolder.imageCloudInListView.setImageResource(imageCloudId);
        listViewHolder.textNumberPerson.setText(listStorage.get(position).getNumber());

        return convertView;
    }

    static class ViewHolder {
        TextView textInListView;
        ImageView imageInListView;
        ImageView imageCloudInListView;
        TextView textNumberPerson;
    }

}
