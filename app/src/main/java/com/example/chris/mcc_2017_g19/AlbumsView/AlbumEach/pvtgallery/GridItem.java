package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.pvtgallery;

/**
 * Created by alessiospallino on 18/11/2017.
 */

public class GridItem {

    public String image;
    public boolean isSelected = false;

    public GridItem(String image) {
        this.image = image;

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
