package com.example.SmartCards;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EditDeck extends AppCompatActivity implements CardListAdapter.OnCardListener {

    private static final int RESULT_ADD_CARD = 5;
    private static final int RESULT_EDIT_CARD = 4;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DECK_NAME = "deckName";
    public static final String IS_DECK_IN_MEMORY = "isDeckInMemory";

    public static List<PlayingCard> deck = new ArrayList<>();

    RecyclerView deckListView;

    CardListAdapter cardListAdapter;

    TextView deckName;

    public static DeckManager deckManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        deckListView = (RecyclerView) findViewById(R.id.EditDeckListView);
        deckName = (TextView) findViewById(R.id.editDeckNameInputText);

        loadFromMemoryIfPossible();

        //Populate RecyclerView
        cardListAdapter = new CardListAdapter(this,deck, this);
        deckListView.setAdapter(cardListAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(deckListView);

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        deckListView.addItemDecoration(divider);
    }

    private void loadFromMemoryIfPossible(){
        if(deckManager == null){
            deckManager = new DeckManager(this);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        try {
            if (sharedPreferences.getBoolean(IS_DECK_IN_MEMORY, false)) {
                deckManager.loadDeckFromMemory();
                deck = new ArrayList(deckManager.getDeck());
                loadDeckName();
            } else {
              deck = new ArrayList<>();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    public void addCard(View view){
        Intent addNewCardIntent = new Intent(this, AddCard.class);
        startActivityForResult(addNewCardIntent, RESULT_ADD_CARD);
        setResult(RESULT_OK, addNewCardIntent);
    }

    public void clearDeck(View view){
        deckManager.clearDeckFromMemory();
        deck.clear();
        updateDeckDisplay();
    }

    public void saveDeckName(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DECK_NAME, deckName.getText().toString());
        editor.apply();
    }

    public void loadDeckName(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        deckName.setText(sharedPreferences.getString(DECK_NAME,""));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void completeEditDeck(View view){
        //Convert list deck to the deck manager
        deckManager.saveDeck(deck);
        saveDeckName();
        finish();
    }

    private void updateDeckDisplay(){
        cardListAdapter.notifyDataSetChanged();
    }


    public static void addCardToDeck(PlayingCard card){
        deck.add(card);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_ADD_CARD || requestCode == RESULT_EDIT_CARD && resultCode == RESULT_OK){
            updateDeckDisplay();
        }
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
            ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(deck, fromPosition, toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);
            //Note, `this` was added to the payload to make the default fade animation not play
            recyclerView.getAdapter().notifyItemChanged(fromPosition, this);
            recyclerView.getAdapter().notifyItemChanged(toPosition, this);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };




    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onCardClick(int position) {
        Intent editCardIntent = new Intent(this, EditCard.class);
        Bundle extras = new Bundle();
        extras.putInt("position", position);
        editCardIntent.putExtras(extras);
        startActivityForResult(editCardIntent, RESULT_EDIT_CARD);
        setResult(RESULT_OK, editCardIntent);
    }
}
