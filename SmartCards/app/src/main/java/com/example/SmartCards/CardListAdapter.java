package com.example.SmartCards;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder> {

    private Activity context;
    private AbstractDeckManager deck;
    private OnCardListener mOnCardListener;


    // TODO: change to deck manager object
    public CardListAdapter(Activity context, AbstractDeckManager deck, OnCardListener onCardListener) {
        this.context = context;
        this.deck = deck;
        this.mOnCardListener = onCardListener;
    }

    // TODO: Still need to add swap function? Still need to have a DeckManager member?

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View singleListTile = context.getLayoutInflater().inflate(R.layout.card_list_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(singleListTile, mOnCardListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cardFace.setImageURI(deck.getCard(position).getImageAddress());
        holder.cardNumber.setText(Integer.toString(position+1));
        holder.cardName.setText(deck.getCard(position).getCardName());
    }

    @Override
    public int getItemCount() {
        return deck.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView cardName, cardNumber;
        ImageView cardFace;
        OnCardListener onCardListener;

        ViewHolder(View v, OnCardListener onCardListener) {
            super(v);
            cardName = (TextView) v.findViewById(R.id.cardPreviewText);
            cardNumber = (TextView) v.findViewById(R.id.cardNumberText);
            cardFace = (ImageView) v.findViewById(R.id.cardPreviewImageView);
            this.onCardListener = onCardListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCardListener.onCardClick(getAdapterPosition());
        }
    }

    public interface OnCardListener {
        void onCardClick(int position);
    }

}
