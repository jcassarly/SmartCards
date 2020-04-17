package com.example.SmartCards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PlayActivity extends AppCompatActivity {

    TextView deckNameText, deckCountText, inPlayCountText, discardCountText;
    Button exitButton;
    private String deckName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        deckNameText = (TextView) findViewById(R.id.deckTitleText);
        deckCountText = (TextView) findViewById(R.id.deckCountText);
        inPlayCountText = (TextView) findViewById(R.id.inPlayCountText);
        discardCountText = (TextView) findViewById(R.id.discardCountText);
        exitButton = (Button) findViewById(R.id.exitGameButton);

        SharedPreferences sharedPreferences = getSharedPreferences(EditDeck.SHARED_PREFS, MODE_PRIVATE);
        deckName = sharedPreferences.getString(EditDeck.DECK_NAME,"DeckName");
        deckNameText.setText(deckName);
    }


    public void exitGame(View view){
        finish();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
