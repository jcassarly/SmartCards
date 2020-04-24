package com.example.SmartCards;

import android.content.Context;
import android.widget.TextView;

import java.util.List;

public class GameDeckManager extends AbstractDeckManager {
    protected GameDeckManager(Context context) {
        super(context);
    }

    @Override
    void saveDeck() {

    }

    @Override
    void loadDeck(TextView deckName) {

    }

    @Override
    List<PlayingCard> getPrimaryDeck() {
        return null;
    }

    @Override
    PlayingCard getCard(int index) {
        return null;
    }

    @Override
    void swap(int fromPosition, int toPosition) {

    }

    @Override
    int size() {
        return 0;
    }
}
