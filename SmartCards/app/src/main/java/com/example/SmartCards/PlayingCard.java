package com.example.SmartCards;

import android.net.Uri;

import java.io.Serializable;

public class PlayingCard implements Serializable {

    private String name;
    private String imageAddress;

    public PlayingCard(String name, Uri imageAddress){
        this.name = name;
        this.imageAddress = imageAddress.toString();
    }

    public String getName() {
        return name;
    }

    public Uri getImageAddress() {
        return Uri.parse(imageAddress);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageAddress(Uri imageAddress) {
        this.imageAddress = imageAddress.toString();
    }
}
