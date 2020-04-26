package com.example.SmartCards;

import android.content.Context;
import android.graphics.drawable.Drawable;

public enum EditButtons {
    SHUFFLE_DECK,
    DISCARD_TO_TOP_OF_DECK,
    DISCARD_TO_DECK_RANDOM,
    SHUFFLE_ADD_TO_TOP,
    DECK_TO_DISCARD,
    ALL_DISCARD_TO_DECK;

    private static final int shuffleDeckIcon = R.drawable.shuffle;
    private static final String shuffleDeckText = "Shuffle Deck";

    private static final int discardToTopOfDeckIcon = R.drawable.discard_to_top_of_deck;
    private static final String discardToTopOfDeckText = "Draw Card Next";

    private static final int discardToDeckRandomIcon = R.drawable.discard_to_deck_random;
    private static final String discardToDeckRandomText = "Add To Deck";

    private static final int shuffleAddToTopIcon = R.drawable.shuffle_add_to_top;
    private static final String shuffleAddToTopText = "Add To Top";

    private static final int deckToDiscardIcon = R.drawable.deck_to_discard;
    private static final String deckToDiscardText = "Add To Discard";

    private static final int allDiscardToDeckIcon = R.drawable.all_discard_to_deck;
    private static final String allDiscardToDeckIconText = "Add All To Deck";


    public static Drawable getIcon(Context context, EditButtons button){
        switch (button){
            case SHUFFLE_DECK:
                return context.getResources().getDrawable(shuffleDeckIcon);
            case DISCARD_TO_TOP_OF_DECK:
                return context.getResources().getDrawable(discardToTopOfDeckIcon);
            case DISCARD_TO_DECK_RANDOM:
                return context.getResources().getDrawable(discardToDeckRandomIcon);
            case SHUFFLE_ADD_TO_TOP:
                return context.getResources().getDrawable(shuffleAddToTopIcon);
            case DECK_TO_DISCARD:
                return context.getResources().getDrawable(deckToDiscardIcon);
            case ALL_DISCARD_TO_DECK:
                return context.getResources().getDrawable(allDiscardToDeckIcon);
        }
        return null;
    }

    public static String getButtonText(EditButtons button){
        switch (button){
            case SHUFFLE_DECK:
                return shuffleDeckText;
            case DISCARD_TO_TOP_OF_DECK:
                return discardToTopOfDeckText;
            case DISCARD_TO_DECK_RANDOM:
                return discardToDeckRandomText;
            case SHUFFLE_ADD_TO_TOP:
                return shuffleAddToTopText;
            case DECK_TO_DISCARD:
                return deckToDiscardText;
            case ALL_DISCARD_TO_DECK:
                return allDiscardToDeckIconText;
        }
        return null;
    }


}



/*
 GameDeckManager extends AbstractDeckManager
    - GameDeckManager getInstance(Context context)
    - void setPrimaryDeck(subDeck sd)
    - void restartGame()
    - void shuffleDeck()
    - void shuffleInDiscard()
    - void deckToDiscard(int deckIndex) // to top of discard
    - void discardToTopOfDeck(int indexInDiscard)
    - void discardToDeckRandom(int indexInDiscard)
 */