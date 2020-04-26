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

    private static final int RESULT_EDIT_GAME = 1;

    TextView deckNameText, deckCountText, inPlayCountText, discardCountText;
    Button exitButton, restartButton;
    ConstraintLayout deckSubdeckButton, inPlaySubdeckButton, discardSubdeckButton;
    private String deckName;

    private List<PlayingCard> deckSubdeck = new ArrayList<>();
    private List<PlayingCard> inPlaySubdeck = new ArrayList<>();
    private List<PlayingCard> discardSubdeck = new ArrayList<>();

    private GameDeckManager deckManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        deckNameText = (TextView) findViewById(R.id.deckTitleText);
        deckCountText = (TextView) findViewById(R.id.deckCountText);
        inPlayCountText = (TextView) findViewById(R.id.inPlayCountText);
        discardCountText = (TextView) findViewById(R.id.discardCountText);
        exitButton = (Button) findViewById(R.id.exitGameButton);
        restartButton = (Button) findViewById(R.id.playRestartButton);
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

        deckManager = GameDeckManager.getInstance(this);

        this.updateDeckCounts();
    }

    public void updateDeckCounts()
    {
        deckManager.loadDeck(this.deckNameText);

        deckManager.setPrimaryDeck(DeckType.DECK);
        deckCountText.setText(String.valueOf(deckManager.size()));

        deckManager.setPrimaryDeck(DeckType.INPLAY);
        inPlayCountText.setText(String.valueOf(deckManager.sizeNonNull()));

        deckManager.setPrimaryDeck(DeckType.DISCARD);
        discardCountText.setText(String.valueOf(deckManager.size()));
    }

    public void updateSubdecks(){
        //updates all 3 subdecks to match what was downloaded from the pi
    }

    public void restartGame(View view){
        //When game is restarted so all cards are back in Deck subdeck and shuffled
        //TODO:Send restart command to pi
        deckManager.restartGame();
        deckManager.saveDeck(this);
        this.updateDeckCounts();
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


    public void modifySubdeck(DeckType deckType){

        Intent intent = new Intent(this, EditGame.class);

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

        startActivityForResult(intent, RESULT_EDIT_GAME);
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        setResult(RESULT_OK, intent);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_EDIT_GAME || resultCode == RESULT_OK){
            //TODO: Add whatever needs to happen everytime an 'editDeck' activity gets completed
            this.updateDeckCounts();
        }
    }


    public void exitGame(View view){
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v == deckSubdeckButton){
            modifySubdeck(DeckType.DECK);
        } else if (v == inPlaySubdeckButton){
            modifySubdeck(DeckType.INPLAY);
        } else if (v == discardSubdeckButton){
            modifySubdeck(DeckType.DISCARD);
        }

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
