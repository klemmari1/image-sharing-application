package com.example.chris.mcc_2017_g19.AlbumsView.AlbumEach.pvtgallery;


/*
Items in the private gallery
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
