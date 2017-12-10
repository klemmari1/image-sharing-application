package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.ImageItem;
import com.example.chris.mcc_2017_g19.R;
import com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model.FullImageAcitivty;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by alessiospallino on 18/11/2017.
 */

public class GridAlbumInfoViewAdapter extends RecyclerView.Adapter<AlbumInfoHolder> {

    private Context mContext;
    private final int mDefaultSpanCount;
    private List<ImageItem> mItemList;

    public GridAlbumInfoViewAdapter(Context mContext, List<ImageItem> itemList, GridLayoutManager gridLayoutManager, int defaultSpanCount) {
        this.mContext = mContext;
        mItemList = itemList;
        mDefaultSpanCount = defaultSpanCount;
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isHeaderType(position) ? mDefaultSpanCount : 1;
            }
        });
    }

    private boolean isHeaderType(int position) {
        return mItemList.get(position).getItemType() == ImageItem.HEADER_ITEM_TYPE ? true : false;
    }


    @Override
    public AlbumInfoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if(viewType == 0){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_albuminfo_type_layout, viewGroup, false);
        }else{
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_albuminfo_type_layout, viewGroup, false);
        }
        return new AlbumInfoHolder(view);
    }


    @Override
    public void onBindViewHolder(AlbumInfoHolder holder, int position) {
        if(isHeaderType(position)){
            bindHeaderItem(holder, position);
        }else{
            bindGridItem(holder, position);
        }
    }


    private void bindGridItem(AlbumInfoHolder holder, int position) {
        //System.out.println(Uri.parse(mItemList.get(position).getItemTitle()));
        ImageView p = (ImageView) holder.itemView.findViewById(R.id.album_photo);
        final String uri = mItemList.get(position).getItemTitle();

        File imageFile = new File(uri);
        //ADD PICTURES IN GRIDVIEW with reasonable resolution
        Picasso.with(mContext).load(imageFile).resize(500, 500).error(R.drawable.cloudoff).into(p);

        //If clicked send to FullImageAcitivty view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sending image url to FullImageAcitivty activity
                Intent i = new Intent(mContext, FullImageAcitivty.class);

                System.out.println(uri);
                i.putExtra("image", uri );
                mContext.startActivity(i);
            }
        });
    }


    private void bindHeaderItem(AlbumInfoHolder holder, int position) {
        System.out.println(Uri.parse(mItemList.get(position).getItemTitle()));

        TextView title = (TextView) holder.itemView.findViewById(R.id.headerTitle);
        title.setText(mItemList.get(position).getItemTitle());
    }


    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position).getItemType() == ImageItem.HEADER_ITEM_TYPE ? 0 : 1;
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }


    public void addItem(ImageItem item){
        mItemList.add(item);
        notifyDataSetChanged();
    }

    /**
     * this method is used to remove items from the list
     *
     * @param item (@Link ImageItem)
     */
    public void removeItem(ImageItem item){
        mItemList.remove(item);
        notifyDataSetChanged();
    }

    public void clear(){
        mItemList.clear();
        notifyDataSetChanged();
    }
}
