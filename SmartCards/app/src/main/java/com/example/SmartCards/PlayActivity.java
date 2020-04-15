package com.example.SmartCards;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PlayActivity extends AppCompatActivity {

    TextView deckName, deckCount, inPlayCount, discardCount;
    Button exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        deckName = (TextView) findViewById(R.id.deckTitleText);
        deckCount = (TextView) findViewById(R.id.deckCountText);
        inPlayCount = (TextView) findViewById(R.id.inPlayCountText);
        discardCount = (TextView) findViewById(R.id.discardCountText);
        exitButton = (Button) findViewById(R.id.exitGameButton);
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
