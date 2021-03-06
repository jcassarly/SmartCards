package com.example.SmartCards;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class EditDeck extends AppCompatActivity implements CardListAdapter.OnCardListener {

    private static final int RESULT_ADD_CARD = 5;
    private static final int RESULT_EDIT_CARD = 4;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DECK_NAME = "deckName";
    public static final String IS_DECK_IN_MEMORY = "isDeckInMemory";

    RecyclerView deckListView;

    CardListAdapter cardListAdapter;

    TextView deckName;

    private EditDeckManager deckManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        deckListView = (RecyclerView) findViewById(R.id.EditDeckListView);
        deckName = (TextView) findViewById(R.id.editDeckNameInputText);

        deckManager = EditDeckManager.getInstance(this);
        deckManager.loadDeck(this.deckName);

        //Populate RecyclerView
        cardListAdapter = new CardListAdapter(this, deckManager, true, this);
        deckListView.setAdapter(cardListAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(deckListView);

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        deckListView.addItemDecoration(divider);
    }

    // starts the add card activity
    public void addCard(View view){
        Intent addNewCardIntent = new Intent(this, AddCard.class);
        startActivityForResult(addNewCardIntent, RESULT_ADD_CARD);
        setResult(RESULT_OK, addNewCardIntent);
    }

    // TODO: add the deckManager clear and eck clear to the deck manager, call that and then use updateDeckDisplay
    public void clearDeck(View view){
        deckManager.clearDeckFromMemory(this);
        updateDeckDisplay();
    }

    // TODO: leave this function, but move the save deck and name to be a call from deckmanager
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void completeEditDeck(View view){
        if (LandingPageActivity.bluetooth_service.isConnected()) {

            //Convert list deck to the deck manager
            deckManager.saveDeck(this);
            deckManager.saveDeckName(this.deckName);
            finish();
        } else {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    // refreshes the display seen by the user
    private void updateDeckDisplay(){
        cardListAdapter.notifyDataSetChanged();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_ADD_CARD || requestCode == RESULT_EDIT_CARD && resultCode == RESULT_OK){
            updateDeckDisplay();
        }
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
            ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            deckManager.swap(fromPosition, toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);
            //Note, `this` was added to the payload to make the default fade animation not play
            recyclerView.getAdapter().notifyItemChanged(fromPosition, this);
            recyclerView.getAdapter().notifyItemChanged(toPosition, this);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };




    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onCardClick(int position) {
        Intent editCardIntent = new Intent(this, EditCard.class);
        Bundle extras = new Bundle();

        extras.putInt("position", position);
        editCardIntent.putExtras(extras);
        startActivityForResult(editCardIntent, RESULT_EDIT_CARD);
        setResult(RESULT_OK, editCardIntent);
    }
}
