package com.example.SmartCards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditGame extends AppCompatActivity implements CardListAdapter.OnCardListener{

    private List<PlayingCard> subdeck = new ArrayList<>();
    private DeckType deckType;



    //GridView cardGridView;
    TextView subDeckTitle;

    private GameDeckManager deckManager;

    private CardListAdapter cardListAdapter;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);

        recyclerView = findViewById(R.id.editGameDisplayListView);
        subDeckTitle = findViewById(R.id.editGameTitle);

        Intent intent = getIntent();

        //subdeck = (List<PlayingCard>) intent.getSerializableExtra("subdeck");
        deckType = (DeckType) intent.getSerializableExtra("deckType");

        setSubDeckTitle(deckType);

        deckManager = GameDeckManager.getInstance(this);
        deckManager.setPrimaryDeck(deckType);
        //deckManager.loadFromMemoryIfPossible(new TextView(this));

        cardListAdapter = new CardListAdapter(this, deckManager, this);
        recyclerView.setAdapter(cardListAdapter);

        //TODO: Add ability to drag cards, but not the ones in inplaysubdeck
        if(deckType != DeckType.INPLAY) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
            ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            // TODO: the card list adapter has the deck we need, we can expose this from there which makes the actual call on the deck manager
            // cast the recycler view to a CardListAdapter since it should always be that
            // TODO: remove swapping as it is no longer in game deck manager
            // deckManager.swap(fromPosition, toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            //Note, `this` was added to the payload to make the default fade animation not play
            recyclerView.getAdapter().notifyItemChanged(fromPosition, this);
            recyclerView.getAdapter().notifyItemChanged(toPosition, this);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    private void setSubDeckTitle(DeckType deckType) {
        switch (deckType) {
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

    @Override
    public void onCardClick(int position) {

    }
}
