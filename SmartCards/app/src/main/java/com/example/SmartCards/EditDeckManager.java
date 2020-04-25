package com.example.SmartCards;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditDeckManager extends AbstractDeckManager {

    public static String ID_COUNT = "id_count";

    private List<PlayingCard> primaryDeck;
    private Context context;
    private PyObject pyDeckManagerModule;
    private PyObject pyDeckManager;

    private static EditDeckManager singletonManager = null;

    private EditDeckManager(Context context) {
        super(context);
        this.primaryDeck = new ArrayList<>();
        this.context = context;
        this.pyDeckManagerModule = AbstractDeckManager.getPyDeckManagerModuleInstance();
        this.pyDeckManager = this.pyDeckManagerModule.callAttr("empty_deck");
    }

    static EditDeckManager getInstance(Context context)
    {
        if (singletonManager == null)
        {
            singletonManager = new EditDeckManager(context);
        }

        return singletonManager;
    }

    void clearDeckFromMemory(Context context)
    {
        resetIDs(context);
        for(PlayingCard card : this.primaryDeck){
            card.delete(context);
        }
        setIsDeckInMemory(false);
        this.primaryDeck.clear();
        this.pyDeckManager = this.pyDeckManagerModule.callAttr("empty_deck");
        this.toFile(this.pyDeckManager);
    }

    void setIsDeckInMemory(boolean bool)
    {
        SharedPreferences sharedPref = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(EditDeck.IS_DECK_IN_MEMORY, bool);
        editor.apply();
    }

    static int getNextID(Context context)
    {
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

    static void resetIDs(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ID_COUNT, 0);
        editor.apply();
    }

    void saveDeckName(TextView deckName)
    {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EditDeck.DECK_NAME, deckName.getText().toString());
        editor.apply();
    }

    void addCard(PlayingCard card)
    {
        this.primaryDeck.add(card);
    }

    void remove(PlayingCard card)
    {
        int cardIndex = this.primaryDeck.indexOf(card);

        // TODO: change to just remove the card at the index - saves time
        this.primaryDeck.remove(card);

        this.pyDeckManager.callAttr("remove_from_index", cardIndex);
    }

    void sendImagesToPi()
    {
        Python py = Python.getInstance();

        PyObject pyFileTransfer = py.getModule("file_transfer");
        PyObject dict = pyFileTransfer.callAttr("FileTransferDict");
        dict.callAttr("save_file_transfer", LandingPageActivity.FILE_TRANSFER_LIST);

        
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    void saveDeck()
    {
        this.pyDeckManager = this.pyDeckManagerModule.callAttr("empty_deck");

        for(PlayingCard card : this.primaryDeck){
            try{
                card.save(this.context);
                this.pyDeckManager.callAttr("add_to_top", card.getImageAddress().toString());
            }
            catch(IOException e){
                setIsDeckInMemory(false);
                e.printStackTrace();
            }
        }
        this.toFile(this.pyDeckManager);
        setIsDeckInMemory(true);
    }

    @Override
    void loadDeck(TextView deckName)
    {
        this.primaryDeck.clear();
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean(EditDeck.IS_DECK_IN_MEMORY, false)) {
            this.pyDeckManager = this.pyDeckManagerModule.callAttr("load_deck", LandingPageActivity.DECK_LIST);
            // if the file didnt exist, the load deck seems to do nothing
            // so write all the data back to the file so that we get a decklist.json if it didnt exist
            this.toFile(this.pyDeckManager);

            PyObject pyAllCards = this.pyDeckManager.callAttr("all_cards");
            //allCards = pyAllCards.toJava();

            for (PyObject cardPath : pyAllCards.asList())
            {
                PlayingCard card = new PlayingCard(context, cardPath.toString());
                primaryDeck.add(card);
            }
            this.loadDeckName(deckName);
        }
    }

    @Override
    List<PlayingCard> getPrimaryDeck()
    {
        return new ArrayList<>(this.primaryDeck);
    }

    @Override
    PlayingCard getCard(int index)
    {
        return this.primaryDeck.get(index);
    }

    void swap(int fromPosition, int toPosition)
    {
        Collections.swap(this.primaryDeck, fromPosition, toPosition);
    }

    @Override
    int size()
    {
        return this.primaryDeck.size();
    }
}
