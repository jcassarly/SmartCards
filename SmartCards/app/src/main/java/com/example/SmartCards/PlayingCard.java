package com.example.SmartCards;

import android.net.Uri;

public class PlayingCard {

    private String name;
    private Uri imageAddress;

    public PlayingCard(String name, Uri imageAddress){
        this.name = name;
        this.imageAddress = imageAddress;
    }

    public String getName() {
        return name;
    }

    public Uri getImageAddress() {
        return imageAddress;
    }

    public void setName(String name) {
        name = name;
    }

    public void setImageAddress(Uri imageAddress) {
        this.imageAddress = imageAddress;
    }
}
