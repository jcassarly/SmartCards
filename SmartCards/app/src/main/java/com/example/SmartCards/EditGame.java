package com.example.SmartCards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EditGame extends AppCompatActivity implements CardListAdapter.OnCardListener, EditButtonAdapter.OnEditButtonListener{

    private List<PlayingCard> subdeck = new ArrayList<>();
    private DeckType deckType;

    private final EditButtons[] deckButtons = {EditButtons.SHUFFLE_DECK, EditButtons.SHUFFLE_ADD_TO_TOP, EditButtons.DECK_TO_DISCARD};
    private final EditButtons[] discardButtons = {EditButtons.DISCARD_TO_DECK_RANDOM, EditButtons.DISCARD_TO_TOP_OF_DECK};



    //GridView cardGridView;
    TextView subDeckTitle;

    // TODO: make game
    private EditDeckManager deckManager;

    private CardListAdapter cardListAdapter;
    private EditButtonAdapter editButtonAdapter;

    private RecyclerView cardRecyclerView, buttonRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);

        cardRecyclerView = findViewById(R.id.editGameDisplayListView);
        buttonRecyclerView = findViewById(R.id.editGameButtonListView);
        subDeckTitle = findViewById(R.id.editGameTitle);

        Intent intent = getIntent();

        deckType = (DeckType) intent.getSerializableExtra("deckType");

        setSubDeckTitle(deckType);

        // TODO: make game
        deckManager = EditDeckManager.getInstance(this);
        //deckManager.loadFromMemoryIfPossible(new TextView(this));

        cardListAdapter = new CardListAdapter(this, deckManager, this);
        cardRecyclerView.setAdapter(cardListAdapter);

        if(deckType == DeckType.DECK) {
            editButtonAdapter = new EditButtonAdapter(this, deckButtons, this);
        }
        if(deckType == DeckType.DISCARD){
            editButtonAdapter = new EditButtonAdapter(this, discardButtons, this);
        }
        buttonRecyclerView.setAdapter(editButtonAdapter);

        if(deckType != DeckType.INPLAY) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(cardRecyclerView);
        }

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        cardRecyclerView.addItemDecoration(divider);
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
            deckManager.swap(fromPosition, toPosition);

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

    public void onButtonClick(int position) {
        finish();
    }

    @Override
    public void onCardClick(int position){

    }
}
