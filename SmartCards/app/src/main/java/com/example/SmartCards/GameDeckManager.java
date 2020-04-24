package com.example.SmartCards;

import android.content.Context;
import android.widget.TextView;

import com.chaquo.python.PyObject;

import java.util.ArrayList;
import java.util.List;

public class GameDeckManager extends AbstractDeckManager {

    private DeckType primaryDeck;
    private Context context;
    private PyObject pyDeckManagerModule;
    private PyObject pyDeckManager;

    private static GameDeckManager singletonManager = null;

    protected GameDeckManager(Context context) {
        super(context);
        this.setPrimaryDeck(DeckType.DECK);
        this.context = context;
        this.pyDeckManagerModule = AbstractDeckManager.getPyDeckManagerModuleInstance();
        this.pyDeckManager = this.pyDeckManagerModule.callAttr("empty_deck");
    }

    GameDeckManager getInstance(Context context)
    {
        if (singletonManager == null)
        {
            singletonManager = new GameDeckManager(context);
        }

        return singletonManager;
    }

    void setPrimaryDeck(DeckType deckType)
    {
        this.primaryDeck = deckType;
    }

    void restartGame()
    {
        this.pyDeckManager.callAttr("restart");
    }

    void shuffleDeck()
    {
        this.pyDeckManager.callAttr("shuffle");
    }

    void shuffleInDiscard()
    {
        this.pyDeckManager.callAttr("shuffle_in_discard");
    }

    void deckToDiscard(int deckIndex) // to top of discard
    {
        this.pyDeckManager.callAttr("move_card_to_discard", deckIndex);
    }

    void shuffleAddToTop(int deckIndex)
    {
        this.pyDeckManager.callAttr("shuffle_add_to_top", deckIndex);
    }

    void discardToTopOfDeck(int indexInDiscard)
    {
        this.pyDeckManager.callAttr("return_to_top", indexInDiscard);
    }

    void discardToDeckRandom(int indexInDiscard)
    {
        this.discardToTopOfDeck(indexInDiscard);
        this.shuffleDeck();
    }

    @Override
    void saveDeck() {
        this.pyDeckManager.callAttr("to_file", LandingPageActivity.DECK_LIST);
    }

    @Override
    void loadDeck(TextView deckName) {
        this.pyDeckManager.callAttr("from_file", LandingPageActivity.DECK_LIST);
        this.loadDeckName(deckName);
    }

    @Override
    List<PlayingCard> getPrimaryDeck() {

        PyObject pyCardList = this.pyCardList();
        List<PlayingCard> cardList = new ArrayList<>();

        for (PyObject cardPath : pyCardList.asList())
        {
            PlayingCard card = new PlayingCard(this.context, cardPath.toString());
            cardList.add(card);
        }

        return cardList;
    }

    @Override
    PlayingCard getCard(int index) {
        String cardPath = this.pyCardList().get(index).toString();

        return new PlayingCard(this.context, cardPath);
    }

    @Override
    int size() {
        return this.pyCardList().callAttr("__len__").toInt();
    }

    private PyObject pyCardList()
    {
        return this.pyDeckManager.get(this.primaryDeck.getPyFieldName());
    }
}
