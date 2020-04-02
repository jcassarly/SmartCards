package com.example.herroworld;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class EditDeck extends AppCompatActivity {

    private static final int RESULT_ADD_CARD = 5;

    private static List<PlayingCard> deck = new ArrayList<>();

    EditText testOutput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        testOutput = (EditText) findViewById(R.id.TestOutput);
    }



    public void addCard(View view){
        Intent addNewCardIntent = new Intent(this, AddCard.class);
        startActivityForResult(addNewCardIntent, RESULT_ADD_CARD);
        setResult(RESULT_OK, addNewCardIntent);
    }

    private void updateDeck(){
        StringBuffer sb = new StringBuffer();
        for (PlayingCard card : deck) {
            sb.append(card.getName());
            sb.append("\n");
        }
        testOutput.setText(sb);
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
