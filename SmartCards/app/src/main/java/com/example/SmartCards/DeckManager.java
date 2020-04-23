package com.example.SmartCards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.SmartCards.PlayActivity;
import com.example.SmartCards.PlayingCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeckManager implements Serializable {

    public static String IMAGE_DIR;
    public static String DECK_LIST_DIR;
    public static String DECK_LIST;
    public static String ID_COUNT = "id_count";

    List<PlayingCard> deck = new ArrayList<>();

    // TODO: probably get rid of these since this is in the deck manager
    List<PlayingCard> deckSubdeck = new ArrayList<>();
    List<PlayingCard> inPlaySubdeck = new ArrayList<>();
    List<PlayingCard> discardSubdeck = new ArrayList<>();

    private Context context;
    private Python py;
    private PyObject deckManagerModule;
    private PyObject deckManager;

    private static DeckManager singletonDeck = null;

    public static DeckManager getInstance(Context context)
    {
        if (singletonDeck == null)
        {
            singletonDeck = new DeckManager(context);
        }

        return singletonDeck;
    }

    public DeckManager(Context context){
        this.context = context;
        File dir = context.getDir("deck", context.MODE_PRIVATE);
        File decklist = context.getDir("decklist", context.MODE_PRIVATE);
        IMAGE_DIR = dir.toString();
        DECK_LIST_DIR = decklist.toString();
        DECK_LIST = DECK_LIST_DIR + "/decklist.json";
        this.py = Python.getInstance();
        this.deckManagerModule = this.py.getModule("DeckManager");
        this.deckManagerModule.put("IMAGE_DIR", IMAGE_DIR);
        this.deckManagerModule.put("DECK_LIST", DECK_LIST_DIR + "/decklist.json");
        this.deckManager = this.deckManagerModule.callAttr("empty_deck");
    }

    private void toFile()
    {
        this.deckManager.callAttr("to_file", DECK_LIST);
    }


    public void clearDeckFromMemory(Context context){
        resetIDs(context);
        for(PlayingCard card : this.getDeck()){
            card.delete(context);
        }
        setIsDeckInMemory(false);
        this.deckManager = this.deckManagerModule.callAttr("empty_deck");
        this.toFile();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveDeck(){
        //this.deck.clear();
        for(PlayingCard card : this.getDeck()){
            try{
                card.save(this.context);
            }
            catch(IOException e){
                setIsDeckInMemory(false);
                e.printStackTrace();
            }
        }
        this.toFile();
        setIsDeckInMemory(true);
    }

    private void setIsDeckInMemory(boolean bool){
        SharedPreferences sharedPref = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(EditDeck.IS_DECK_IN_MEMORY, bool);
        editor.apply();
    }

    public void loadDeckFromMemory(Context context) throws IOException {
        this.deckManager = this.deckManagerModule.callAttr("load_deck", DECK_LIST_DIR + "/decklist.json");
        // if the file didnt exist, the load deck seems to do nothing
        // so write all the data back to the file so that we get a decklist.json if it didnt exist
        this.toFile();
    }


    public static int getNextID(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int nextID = sharedPreferences.getInt(ID_COUNT, 0) + 1;

        if(nextID == Integer.MAX_VALUE){
            nextID = 0;
        }

        editor.putInt(ID_COUNT, nextID);
        editor.apply();

        return nextID;
    }

    public static void resetIDs(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ID_COUNT, 0);
        editor.apply();
    }

    public List<PlayingCard> getDeck(){

        List<PlayingCard> deck = new ArrayList<>();
        PyObject pyAllCards = this.deckManager.callAttr("__all_cards");
        ArrayList<String> allCards = new ArrayList<>();
        allCards = pyAllCards.toJava(allCards.getClass());

        for (String cardPath : allCards)
        {
            PlayingCard card = new PlayingCard(context, cardPath);
            deck.add(card);
        }

        return deck;
    }

    private void clearSubdecks(){

        deckSubdeck = new ArrayList<>();
        inPlaySubdeck = new ArrayList<>();
        discardSubdeck = new ArrayList<>();
    }

    public void saveDeckName(TextView deckName)
    {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EditDeck.DECK_NAME, deckName.getText().toString());
        editor.apply();
    }

    public void loadDeckName(TextView deckName)
    {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);
        deckName.setText(sharedPreferences.getString(EditDeck.DECK_NAME,""));
    }

    public void addCard(PlayingCard card)
    {
        this.deckManager.callAttr("add_to_top", card.getImageAddress());
    }

    /**
     * Swaps positions in the full deck, not subdecks
     * @param fromPosition
     * @param toPosition
     */
    public void swapInFullDeck(int fromPosition, int toPosition)
    {
        this.deckManager.callAttr("move_card_in_deck", fromPosition,  toPosition);
    }

    public void loadFromMemoryIfPossible(TextView deckName)
    {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);

        try {
            if (sharedPreferences.getBoolean(EditDeck.IS_DECK_IN_MEMORY, false)) {
                this.loadDeckFromMemory(this.context);
                this.loadDeckName(deckName);
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public void clear()
    {
        this.deck.clear();
        this.clearSubdecks();
    }

    public void remove(PlayingCard card)
    {
        List<PlayingCard> deck = this.getDeck();
        int cardIndex = deck.indexOf(card);

        this.deckManager.callAttr("remove_from_index", cardIndex);
    }

}
