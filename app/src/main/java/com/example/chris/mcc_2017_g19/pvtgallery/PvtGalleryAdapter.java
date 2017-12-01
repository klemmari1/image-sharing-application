package com.example.chris.mcc_2017_g19.pvtgallery;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chris.mcc_2017_g19.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessiospallino on 18/11/2017.
 */

public class PvtGalleryAdapter extends RecyclerView.Adapter {
    //Declare GalleryItems List
    List<PvtGalleryItem> galleryItems;
    Context context;
    //Declare GalleryAdapterCallBacks
    GalleryAdapterCallBacks mAdapterCallBacks;

    public PvtGalleryAdapter(Context context) {
        this.context = context;
        //get GalleryAdapterCallBacks from contex
        this.mAdapterCallBacks = (GalleryAdapterCallBacks) context;
        //Initialize GalleryItem List
        this.galleryItems = new ArrayList<>();
    }

    //This method will take care of adding new Gallery items to RecyclerView
    public void addGalleryItems(List<PvtGalleryItem> galleryItems) {
        int previousSize = this.galleryItems.size();
        this.galleryItems.addAll(galleryItems);
        notifyItemRangeInserted(previousSize, galleryItems.size());

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.custom_row_pvtgallery_item, parent, false);
        return new GalleryItemHolder(row);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        //get current Gallery Item
        PvtGalleryItem currentItem = galleryItems.get(position);
        //Create file to load with Picasso lib
        File imageViewThoumb = new File(currentItem.imageUri);
        //cast holder with gallery holder
        GalleryItemHolder galleryItemHolder = (GalleryItemHolder) holder;
        //Load with Picasso
        Picasso.with(context)
                .load(imageViewThoumb)
                .centerCrop()
                .resize(ScreenUtils.getScreenWidth(context) / 3, ScreenUtils.getScreenHeight(context) / 4)//Resize image to width half of screen and height 1/3 of screen height
                .into(galleryItemHolder.imageViewThumbnail);

        //set on click listener on imageViewThumbnail
        galleryItemHolder.imageViewThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call onItemSelected method and pass the position and let activity decide what to do when item selected
                mAdapterCallBacks.onItemSelected(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    public class GalleryItemHolder extends RecyclerView.ViewHolder {
        ImageView imageViewThumbnail;

        public GalleryItemHolder(View itemView) {
            super(itemView);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);

        }
    }

    //Interface for communication of Adapter and MainActivity
    public interface GalleryAdapterCallBacks {
        void onItemSelected(int position);
    }


}

