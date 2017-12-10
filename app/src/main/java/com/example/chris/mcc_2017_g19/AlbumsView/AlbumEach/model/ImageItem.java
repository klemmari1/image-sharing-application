package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model;


/*
Images in the group gallery
 */
public abstract class ImageItem {

    public static final int HEADER_ITEM_TYPE = 0;
    public static final int GRID_ITEM_TYPE = 1;
    private String mItemTitle;

    public ImageItem(String title){
        mItemTitle = title;
    }

    public String getItemTitle() {
        return mItemTitle;
    }

    public abstract int getItemType();

}
