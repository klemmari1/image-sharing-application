package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model;


/*
Items in the group gallery view
 */
public class GridImageItem extends ImageItem{

    private String mSubTitle;



    public GridImageItem(String title){
        super(title);

    }

    public String getmSubTitle() {
        return mSubTitle;
    }

    public void setmSubTitle(String mSubTitle) {
        this.mSubTitle = mSubTitle;
    }



    @Override
    public int getItemType() {
        return GRID_ITEM_TYPE;
    }
}
