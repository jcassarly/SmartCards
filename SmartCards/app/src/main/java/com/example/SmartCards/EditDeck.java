package com.example.SmartCards;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class EditDeck extends AppCompatActivity {

    private static final int RESULT_ADD_CARD = 5;

    private static List<PlayingCard> deck = new ArrayList<>();

    //ListView deckListView;
    RecyclerView deckListView;

    CardListAdapter cardListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

       //deckListView = (ListView) findViewById(R.id.EditDeckListView);
        deckListView = (RecyclerView) findViewById(R.id.EditDeckListView);
        cardListAdapter = new CardListAdapter(this,deck);
        deckListView.setAdapter(cardListAdapter);
    }



    public void addCard(View view){
        Intent addNewCardIntent = new Intent(this, AddCard.class);
        startActivityForResult(addNewCardIntent, RESULT_ADD_CARD);
        setResult(RESULT_OK, addNewCardIntent);
    }

    public void clearDeck(View view){
        deck.clear();
        updateDeck();
    }

    public void completeEditDeck(View view){
        //Convert list deck to the deck manager
        finish();
    }

    private void updateDeck(){
        cardListAdapter.notifyDataSetChanged();
    }


    public static void addCardToDeck(PlayingCard card){
        deck.add(card);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_ADD_CARD && resultCode == RESULT_OK){
            updateDeck();
        }
    }




    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
