package com.example.SmartCards;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.List;

public abstract class AbstractDeckManager {

    private static Python py = null;
    private static PyObject pyDeckManagerModule = null;

    private static PyObject pyImgurUploader;

    private Context context;

    protected AbstractDeckManager(Context context)
    {
        this.context = context;
    }

    private static Python getPyInstance()
    {
        if (py == null)
        {
            py = Python.getInstance();
        }

        return py;
    }

    protected static PyObject getPyDeckManagerModuleInstance()
    {
        if (pyDeckManagerModule == null)
        {
            Python pyInst = getPyInstance();
            pyDeckManagerModule = pyInst.getModule("DeckManager");
            pyDeckManagerModule.put("IMAGE_DIR", LandingPageActivity.IMAGE_DIR);
            pyDeckManagerModule.put("DECK_LIST", LandingPageActivity.DECK_LIST);
        }

        return pyDeckManagerModule;
    }

    protected static PyObject getPyImgurUploaderInstance()
    {
        if (pyImgurUploader == null)
        {
            Python pyInst = getPyInstance();
            pyImgurUploader = pyInst.getModule("imgur_uploader");
        }

        return pyImgurUploader;
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
    abstract void saveDeck(Context context);
    abstract void loadDeck(TextView deckName);
    abstract List<PlayingCard> getPrimaryDeck();
    abstract PlayingCard getCard(int index); // gets the card from the primary deck
    abstract int size();
}
