package com.example.SmartCards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener{


    public enum subDecks {DECK, INPLAY, DISCARD}

    TextView deckNameText, deckCountText, inPlayCountText, discardCountText;
    Button exitButton;
    ConstraintLayout deckSubdeckButton, inPlaySubdeckButton, discardSubdeckButton;
    private String deckName;

    private List<PlayingCard> deckSubdeck = new ArrayList<>();
    private List<PlayingCard> inPlaySubdeck = new ArrayList<>();
    private List<PlayingCard> discardSubdeck = new ArrayList<>();

    private DeckManager deckManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        deckNameText = (TextView) findViewById(R.id.deckTitleText);
        deckCountText = (TextView) findViewById(R.id.deckCountText);
        inPlayCountText = (TextView) findViewById(R.id.inPlayCountText);
        discardCountText = (TextView) findViewById(R.id.discardCountText);
        exitButton = (Button) findViewById(R.id.exitGameButton);
        deckSubdeckButton = (ConstraintLayout) findViewById(R.id.deckControlsLayout);
        inPlaySubdeckButton = (ConstraintLayout) findViewById(R.id.inPlayControlsLayout);
        discardSubdeckButton = (ConstraintLayout) findViewById(R.id.discardControlsLayout);

        deckSubdeckButton.setOnClickListener(this);
        inPlaySubdeckButton.setOnClickListener(this);
        discardSubdeckButton.setOnClickListener(this);


        SharedPreferences sharedPreferences = getSharedPreferences(EditDeck.SHARED_PREFS, MODE_PRIVATE);
        deckName = sharedPreferences.getString(EditDeck.DECK_NAME,"DeckName");
        deckNameText.setText(deckName);
        updateSubdecks();

        deckManager = DeckManager.getInstance(this);
    }

    public void updateSubdecks(){
        //updates all 3 subdecks to match what was downloaded from the pi
    }

    public void restartGame(View view){
        //When game is restarted so all cards are back in Deck subdeck and shuffled
        //TODO:Send restart command to pi
    }

    public void blockDock(){
        //When modifying subdecks, send message to dock to prevent it from flashing cards
        //TODO:Send block command to pi
        LandingPageActivity.bluetooth_service.block();
    }

    public void unblockDock(){
        //When finished modifying subdecks, send message to dock to allow it to flash cards
        //TODO:Send unblock command to pi
        LandingPageActivity.bluetooth_service.unblock();
    }


    public void modifySubdeck(subDecks deckType){

        Intent intent = new Intent(this, EditGame.class);

        intent.putExtra("deckType", deckType);
        //TODO: intent.putExtra("subdeck", (Serializable) EditDeck.deck);
        /*
        switch(deckType){
            case DECK:
                intent.putExtra("subdeck", (Serializable) deckSubdeck);
                intent.putExtra("deckType", deckType);
                break;
            case INPLAY:
                intent.putExtra("subdeck", (Serializable) inPlaySubdeck);
                intent.putExtra("deckType", deckType);
                break;
            case DISCARD:
                intent.putExtra("subdeck", (Serializable) discardSubdeck);
                intent.putExtra("deckType", deckType);
                break;
            default:
                break;
        }
         */
        startActivity(intent);
    }


    public void exitGame(View view){
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v == deckSubdeckButton){
            modifySubdeck(subDecks.DECK);
        } else if (v == inPlaySubdeckButton){
            modifySubdeck(subDecks.INPLAY);
        } else if (v == discardSubdeckButton){
            modifySubdeck(subDecks.DISCARD);
        }

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
