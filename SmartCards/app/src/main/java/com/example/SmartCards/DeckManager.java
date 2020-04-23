package com.example.SmartCards;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.SmartCards.PlayActivity;
import com.example.SmartCards.PlayingCard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeckManager {

    public static String IMAGE_DIR;
    public static String ID_COUNT = "id_count";

    List<PlayingCard> deck = new ArrayList<>();
    List<PlayingCard> deckSubdeck = new ArrayList<>();
    List<PlayingCard> inPlaySubdeck = new ArrayList<>();
    List<PlayingCard> discardSubdeck = new ArrayList<>();

    private Context context;


    public DeckManager(Context context){
        this.context = context;
        File dir = context.getDir("deck", context.MODE_PRIVATE);
        IMAGE_DIR = dir.toString();
    }


    public void clearDeckFromMemory(){
        resetIDs(context);
        for(PlayingCard card : deck){
            card.delete(context);
        }
        deck.clear();
        setIsDeckInMemory(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveDeck(List<PlayingCard> deck){
        this.deck.clear();
        for(PlayingCard card : deck){
            try{
                card.save(context);
                this.deck.add(card);
            }
            catch(IOException e){
                setIsDeckInMemory(false);
                e.printStackTrace();
            }
        }
        setIsDeckInMemory(true);
    }

    private void setIsDeckInMemory(boolean bool){
        SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(EditDeck.IS_DECK_IN_MEMORY, bool);
        editor.apply();
    }

    public void loadDeckFromMemory() throws IOException {
        deck.clear();
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
        }
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







}
