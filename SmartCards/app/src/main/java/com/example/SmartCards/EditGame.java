package com.example.SmartCards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class EditGame extends AppCompatActivity {

    private List<PlayingCard> subdeck = new ArrayList<>();
    private PlayActivity.subDecks deckType;

    GridView cardGridView;
    TextView subDeckTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);

        cardGridView = findViewById(R.id.cardDisplayGridView);
        subDeckTitle = findViewById(R.id.editGameTitle);

        Intent intent = getIntent();

        subdeck = (List<PlayingCard>) intent.getSerializableExtra("subdeck");
        deckType = (PlayActivity.subDecks) intent.getSerializableExtra("deckType");

        setSubDeckTitle(deckType);

        CardGridAdapter adapter = new CardGridAdapter(this, subdeck);
        cardGridView.setAdapter(adapter);

        cardGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "You clicked the card named "+ subdeck.get(position).getName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSubDeckTitle(PlayActivity.subDecks deckType){
        switch(deckType) {
            case DECK:
                subDeckTitle.setText(getResources().getString(R.string.edit_game_deck_text));
                break;
            case INPLAY:
                subDeckTitle.setText(getResources().getString(R.string.edit_game_in_play_text));
                break;
            case DISCARD:
                subDeckTitle.setText(getResources().getString(R.string.edit_game_discard_text));
                break;
        }
    }

    public void completeEdit(View view){
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
