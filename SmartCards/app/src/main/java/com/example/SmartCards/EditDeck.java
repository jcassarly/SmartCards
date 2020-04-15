package com.example.SmartCards;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditDeck extends AppCompatActivity implements CardListAdapter.OnCardListener {

    private static final int RESULT_ADD_CARD = 5;
    private static final int RESULT_EDIT_CARD = 4;

    public static List<PlayingCard> deck = new ArrayList<>();

    RecyclerView deckListView;

    CardListAdapter cardListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        deckListView = (RecyclerView) findViewById(R.id.EditDeckListView);
        cardListAdapter = new CardListAdapter(this,deck, this);
        deckListView.setAdapter(cardListAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(deckListView);

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        deckListView.addItemDecoration(divider);
    }



    public void addCard(View view){
        Intent addNewCardIntent = new Intent(this, AddCard.class);
        startActivityForResult(addNewCardIntent, RESULT_ADD_CARD);
        setResult(RESULT_OK, addNewCardIntent);
    }

    public void clearDeck(View view){
        deck.clear();
        updateDeckDisplay();
    }

    public void completeEditDeck(View view){
        //Convert list deck to the deck manager
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
