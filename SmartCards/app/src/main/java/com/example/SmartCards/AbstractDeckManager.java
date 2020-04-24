package com.example.SmartCards;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import org.w3c.dom.Text;

import java.util.List;

public abstract class AbstractDeckManager {

    private static Python py = null;
    private static PyObject pyDeckManagerModule = null;

    private Context context;

    protected AbstractDeckManager(Context context)
    {
        this.context = context;
    }

    protected static PyObject getPyDeckManagerModuleInstance()
    {
        if (pyDeckManagerModule == null)
        {
            py = Python.getInstance();
            pyDeckManagerModule = py.getModule("DeckManager");
            pyDeckManagerModule.put("IMAGE_DIR", LandingPageActivity.IMAGE_DIR);
            pyDeckManagerModule.put("DECK_LIST", LandingPageActivity.DECK_LIST);
        }

        return pyDeckManagerModule;
    }

    protected void toFile(PyObject pyDeckManager)
    {
        pyDeckManager.callAttr("to_file", LandingPageActivity.DECK_LIST);
    }

    public void loadDeckName(TextView deckName)
    {
        // TODO: might want to remove this null check.  I added it to test something
        if (deckName != null)
        {
            SharedPreferences sharedPreferences = this.context.getSharedPreferences(EditDeck.SHARED_PREFS, this.context.MODE_PRIVATE);
            deckName.setText(sharedPreferences.getString(EditDeck.DECK_NAME,""));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    abstract void saveDeck();
    abstract void loadDeck(TextView deckName);
    abstract List<PlayingCard> getPrimaryDeck();
    abstract PlayingCard getCard(int index); // gets the card from the primary deck
    abstract int size();
}
