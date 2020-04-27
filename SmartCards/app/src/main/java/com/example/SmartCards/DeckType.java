package com.example.SmartCards;

public enum DeckType {
    DECK("deckList"),
    INPLAY("inPlayList"),
    DISCARD("discardList");

    private String pyFieldName;


    DeckType(String pyFieldName) {
        this.pyFieldName = pyFieldName;
    }

    public String getPyFieldName() {
        return pyFieldName;
    }
}
