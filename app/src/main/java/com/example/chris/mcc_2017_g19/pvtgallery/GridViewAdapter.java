package com.example.chris.mcc_2017_g19.pvtgallery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chris.mcc_2017_g19.R;
import com.squareup.picasso.Picasso;

/**
 * Created by alessiospallino on 18/11/2017.
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
        ImageView imageView;
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

        //ADD PICTURES IN GRIDVIEW
        Picasso.with(mContext).load(file).error(R.drawable.cloudoff).into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                // Sending image url to FullImage activity
                Intent i = new Intent(mContext, FullImage.class);

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
