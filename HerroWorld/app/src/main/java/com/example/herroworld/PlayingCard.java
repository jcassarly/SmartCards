package com.example.herroworld;

import java.net.URI;

public class PlayingCard {

    private String name;
    private URI imageAddress;

    public PlayingCard(String name, URI imageAddress){
        this.name = name;
        this.imageAddress = imageAddress;
    }

    public String getName() {
        return name;
    }

    public URI getImageAddress() {
        return imageAddress;
    }

    public void setName(String name) {
        name = name;
    }

    public void setImageAddress(URI imageAddress) {
        this.imageAddress = imageAddress;
    }
}
