package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.pvtgallery;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.FullImageAcitivty;
import com.example.chris.mcc_2017_g19.R;
import com.squareup.picasso.Picasso;


/*
Adapter for the private gallery grid
 */
public class GridViewAdapter extends ArrayAdapter<GridItem> {

    private Context mContext;
    private int layoutResourceId;
    private ArrayList<GridItem> mGridData = new ArrayList<GridItem>();

    public GridViewAdapter(Context mContext, int layoutResourceId, ArrayList<GridItem> mGridData) {
        super(mContext, layoutResourceId, mGridData);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.mGridData = mGridData;
    }

    /**
     * Updates grid data and refresh grid items.
     * @param mGridData
     */
    public void setGridData(ArrayList<GridItem> mGridData) {

        this.mGridData.addAll(mGridData);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.iv_Grid);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        GridItem item = mGridData.get(position);
        File file = new File(String.valueOf(Uri.parse(item.getImage())));

        //ADD PICTURES IN GRIDVIEW with reasonable resolution
        Picasso.with(mContext).load(file).resize(500, 500).error(R.drawable.cloudoff).into(holder.imageView);

        //If clicked send to FullImageAcitivty view
        holder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // Sending image url to FullImageAcitivty activity
                Intent i = new Intent(mContext, FullImageAcitivty.class);

                String url = String.valueOf(Uri.parse(mGridData.get(position).getImage()));

                System.out.println(url);
                i.putExtra("image", url );
                mContext.startActivity(i);

            }
        });
        return row;
    }



    static class ViewHolder {
        ImageView imageView;
    }

}
