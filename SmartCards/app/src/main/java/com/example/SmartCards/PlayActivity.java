package com.example.SmartCards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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


    public void restartGame(View view){
        //When game is restarted so all cards are back in Deck subdeck and shuffled
        deckManager.restartGame();
        deckManager.saveDeck(this);
        this.updateDeckCounts();
        if (LandingPageActivity.bluetooth_service.override() == BluetoothService.SEND_STATUS.ERROR) {
            Toast.makeText(this, "Sync not complete, try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void blockDock(){
        //When modifying subdecks, send message to dock to prevent it from flashing cards
        LandingPageActivity.bluetooth_service.block();
    }

    public void unblockDock(){
        //When finished modifying subdecks, send message to dock to allow it to flash cards
        LandingPageActivity.bluetooth_service.unblock();
    }


    public void modifySubdeck(DeckType deckType){
        if (LandingPageActivity.bluetooth_service.isConnected()) {
            if (LandingPageActivity.bluetooth_service.block() == BluetoothService.SEND_STATUS.SUCCESS) {

                Intent intent = new Intent(this, EditGame.class);
                intent.putExtra("deckType", deckType);
                LandingPageActivity.bluetooth_service.getDeckList();
                deckManager.loadDeck(null);

                startActivityForResult(intent, RESULT_EDIT_GAME);
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                setResult(RESULT_OK, intent);
            } else {
                Toast.makeText(this, "Unable to lock the deck", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_EDIT_GAME || resultCode == RESULT_OK){
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
        int counter = 0;
        while (counter++ < 5 && LandingPageActivity.bluetooth_service.getDeckList() == BluetoothService.SEND_STATUS.ERROR) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            Toast.makeText(this, "Unable to get Pi's deck", Toast.LENGTH_SHORT).show();
        }
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
