package com.example.SmartCards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Build;

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
        File dir = context.getDir("deck", context.MODE_PRIVATE);
        File decklist = context.getDir("decklist", context.MODE_PRIVATE);
        IMAGE_DIR = dir.toString();
        DECK_LIST_DIR = decklist.toString();
        DECK_LIST = DECK_LIST_DIR + "/decklist.json";
    }

    private void toFile()
    {
        this.deckManager.callAttr("to_file", DECK_LIST_DIR + "/decklist.json");
    }

    private void fromFile()
    {
        try {
            FileInputStream fis = new FileInputStream(new File(DECK_LIST));
            byte buffer[] = new byte[fis.available()];
            fis.read(buffer);
            String jstr = new String(buffer, "UTF-8");
            Pattern pattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL); // in case there's some unwanted characters outside the brackets.
            Matcher matcher = pattern.matcher(jstr);
            matcher.find();
            jstr = matcher.group();
            JSONObject jobj = (JSONObject) new JSONTokener(jstr).nextValue();
            JSONArray deck = jobj.getJSONArray("deckList");
            JSONArray inPlay = jobj.getJSONArray("inPlayList");
            JSONArray discard = jobj.getJSONArray("discardList");

            
        }
        catch (IOException | JSONException ioe)
        {
            // TODO: write default deck to the file and load in default deck
        }

    }


    public void clearDeckFromMemory(Context context){
        resetIDs(context);
        for(PlayingCard card : deck){
            card.delete(context);
        }
        deck.clear();
        setIsDeckInMemory(false, context);
        this.deckManager = this.deckManagerModule.callAttr("empty_deck");
        this.toFile();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveDeck(List<PlayingCard> deck, Context context){
        //this.deck.clear();
        for(PlayingCard card : deck){
            try{
                card.save(context);
                this.deck.add(card);
            }
            catch(IOException e){
                setIsDeckInMemory(false, context);
                e.printStackTrace();
            }
        }
        this.toFile();
        setIsDeckInMemory(true, context);
    }

    private void setIsDeckInMemory(boolean bool, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(EditDeck.IS_DECK_IN_MEMORY, bool);
        editor.apply();
    }

    public void loadDeckFromMemory(Context context) throws IOException {
        deck.clear();
        this.deckManager = this.deckManagerModule.callAttr("load_deck", DECK_LIST_DIR + "/decklist.json");
        // if the file didnt exist, the load deck seems to do nothing
        // so write all the data back to the file so that we get a decklist.json if it didnt exist
        this.toFile();

        PyObject pyAllCards = this.deckManager.callAttr("__all_cards");
        ArrayList<String> allCards = new ArrayList<>();
        allCards = pyAllCards.toJava(allCards.getClass());

        for (String cardPath : allCards)
        {
            PlayingCard card = new PlayingCard(context, cardPath);
            deck.add(card);
        }
        /* Old version
        File imageDirectory = new File(IMAGE_DIR);
        File[] directoryFiles = imageDirectory.listFiles();
        if (directoryFiles != null){
            for (File image: directoryFiles){
                PlayingCard card = new PlayingCard(context, image.getPath());
                deck.add(card);
            }
        }
        else {
            throw new IOException("Default directory is configured incorrectly or missing");
        }*/
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
        return deck;
    }

    private void translateDeckToSubdeck(){
        //TODO: copy deck and don't give the memory address
        deckSubdeck = deck;
    }

    private void clearSubdecks(){
        deckSubdeck = new ArrayList<>();
        inPlaySubdeck = new ArrayList<>();
        discardSubdeck = new ArrayList<>();
    }

    public void setupSubdecks(){
        clearSubdecks();
        translateDeckToSubdeck();
    }

    public void saveDeckName(Context context)
    {

    }

    public void loadDeckName(Context context)
    {

    }

    public void addCard(PlayingCard card)
    {

    }

    /**
     * Swaps position in the full
     * @param fromPosition
     * @param toPosition
     */
    public void swapInFullDeck(int fromPosition, int toPosition)
    {

    }

    public void loadFromMemoryIfPossible(Context context)
    {

    }

}
