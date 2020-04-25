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

    static GameDeckManager getInstance(Context context)
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

    List<PlayingCard> allNonNullCards()
    {
        PyObject pyAllCards = this.pyDeckManager.callAttr("all_cards");

        List<PlayingCard> cardList = new ArrayList<>();
        // TODO: refactor this for loop
        // TODO: remove the null check - unnecessary
        for (PyObject cardPath : pyAllCards.asList())
        {
            if (cardPath != null)
            {
                cardList.add(this.pyCardPathToPlayingCard(cardPath));
            }
        }

        return cardList;
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
            cardList.add(this.pyCardPathToPlayingCard(cardPath));
        }

        return cardList;
    }

    @Override
    PlayingCard getCard(int index) {
        PyObject pyCardPath = this.pyCardList().asList().get(index);

        return this.pyCardPathToPlayingCard(pyCardPath);
    }

    @Override
    int size() {
        return this.pyCardList().callAttr("__len__").toInt();
    }

    /**
     * @return the number of elements in the primary deck that are not null
     */
    int sizeNonNull()
    {
        int size = 0;
        for (PlayingCard card : this.getPrimaryDeck())
        {
            if (card != null)
            {
                size++;
            }
        }
        return size;
    }

    private PyObject pyCardList()
    {
        return this.pyDeckManager.get(this.primaryDeck.getPyFieldName());
    }

    private PlayingCard pyCardPathToPlayingCard(PyObject pyCardPath)
    {
        return (pyCardPath == null) ? null : new PlayingCard(this.context, pyCardPath.toString());
    }
}
