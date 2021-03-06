package com.example.SmartCards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EditGame extends AppCompatActivity implements CardListAdapter.OnCardListener, EditButtonAdapter.OnEditButtonListener{

    private DeckType deckType;

    private final EditButtons[] deckButtons = {EditButtons.SHUFFLE_DECK, EditButtons.SHUFFLE_ADD_TO_TOP, EditButtons.DECK_TO_DISCARD};
    private final EditButtons[] discardButtons = {EditButtons.DISCARD_TO_DECK_RANDOM, EditButtons.DISCARD_TO_TOP_OF_DECK, EditButtons.ALL_DISCARD_TO_DECK};


    TextView subDeckTitle;

    private GameDeckManager deckManager;

    private CardListAdapter cardListAdapter;
    private EditButtonAdapter editButtonAdapter;

    private ConstraintLayout constraintLayout;

    private RecyclerView cardRecyclerView, buttonRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);

        cardRecyclerView = findViewById(R.id.editGameDisplayListView);
        buttonRecyclerView = findViewById(R.id.editGameButtonListView);
        subDeckTitle = findViewById(R.id.editGameTitle);
        constraintLayout = findViewById(R.id.editGameLayout);

        Intent intent = getIntent();

        deckType = (DeckType) intent.getSerializableExtra("deckType");

        setSubDeckTitle(deckType);

        deckManager = GameDeckManager.getInstance(this);
        deckManager.setPrimaryDeck(deckType);

        cardListAdapter = new CardListAdapter(this, deckManager, false, this);
        cardRecyclerView.setAdapter(cardListAdapter);

        modifyLayoutForDeckType();


        RecyclerView.ItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        cardRecyclerView.addItemDecoration(divider);
    }

    private void modifyLayoutForDeckType(){
        if(deckType == DeckType.DECK) {
            editButtonAdapter = new EditButtonAdapter(this, deckButtons, this);
            buttonRecyclerView.setAdapter(editButtonAdapter);
        }
        if(deckType == DeckType.DISCARD){
            editButtonAdapter = new EditButtonAdapter(this, discardButtons, this);
            buttonRecyclerView.setAdapter(editButtonAdapter);
        }
        if(deckType == DeckType.INPLAY) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(cardRecyclerView.getId(),ConstraintSet.BOTTOM,constraintLayout.getId(),ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(constraintLayout);
        }
    }

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

    public void completeEdit(View view)
    {
        finish();
    }

    public void completeAndSaveEdit()
    {
        deckManager.saveDeck(this);
        LandingPageActivity.bluetooth_service.override();
        finish();
    }

    @Override
    public void finish() {
        LandingPageActivity.bluetooth_service.unblock();
        super.finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    }

    public void onButtonClick(EditButtons button) {
        if(isButtonClickValid(button)){
            switch (button){
                case SHUFFLE_DECK:
                    shuffleDeck();
                    break;
                case DISCARD_TO_TOP_OF_DECK:
                    discardToTopOfDeck();
                    break;
                case DISCARD_TO_DECK_RANDOM:
                    discardToDeckRandom();
                    break;
                case SHUFFLE_ADD_TO_TOP:
                    shuffleAddToTop();
                    break;
                case DECK_TO_DISCARD:
                    deckToDiscard();
                    break;
                case ALL_DISCARD_TO_DECK:
                    shuffleInDiscard();
                    break;
            }
            setResult(RESULT_OK);
            completeAndSaveEdit();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Please select a card first", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isButtonClickValid(EditButtons button){
        if(cardListAdapter.getSelectedCardPosition() != -1 || button == EditButtons.ALL_DISCARD_TO_DECK || button == EditButtons.SHUFFLE_DECK){
            return true;
        }
        return false;
    }


    @Override
    public void onCardClick(int position){
        //Do nothing
    }

    public void shuffleDeck(){
        deckManager.shuffleDeck();
    }

    public void shuffleInDiscard(){
        deckManager.shuffleInDiscard();
    }

    public void deckToDiscard(){
        deckManager.deckToDiscard(cardListAdapter.getSelectedCardPosition());
    }

    public void discardToTopOfDeck(){
        deckManager.discardToTopOfDeck(cardListAdapter.getSelectedCardPosition());
    }

    public void discardToDeckRandom(){
        deckManager.discardToDeckRandom(cardListAdapter.getSelectedCardPosition());
    }

    public void shuffleAddToTop(){
        deckManager.shuffleAddToTop(cardListAdapter.getSelectedCardPosition());
    }

}
