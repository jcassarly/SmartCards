package com.example.SmartCards;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class EditDeck extends AppCompatActivity {

    private static final int RESULT_ADD_CARD = 5;

    private static List<PlayingCard> deck = new ArrayList<>();

    ListView deckListView;

    CardListView cardListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        deckListView = (ListView) findViewById(R.id.EditDeckListView);
        cardListView = new CardListView(this,deck);
        deckListView.setAdapter(cardListView);
    }



    public void addCard(View view){
        Intent addNewCardIntent = new Intent(this, AddCard.class);
        startActivityForResult(addNewCardIntent, RESULT_ADD_CARD);
        setResult(RESULT_OK, addNewCardIntent);
    }

    private void updateDeck(){
        cardListView.notifyDataSetChanged();
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
