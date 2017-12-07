package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.model;

/**
 * Created by alessiospallino on 06/12/2017.
 */

public class HeaderImageItem extends ImageItem{

    public HeaderImageItem(String title){
        super(title);
    }

    @Override
    public int getItemType() {
        return HEADER_ITEM_TYPE;
    }
}
