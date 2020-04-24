package com.example.SmartCards;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
        holder.cardNumber.setText(Integer.toString(position+1));
        if(deck.getCard(position) != null){
            holder.cardFace.setImageURI(deck.getCard(position).getImageAddress());
            holder.cardName.setText(deck.getCard(position).getCardName());
        }
        else {
            holder.cardName.setText("NO CARD");
            holder.cardName.setTextColor(Color.RED);
            holder.cardName.setTypeface(null, Typeface.BOLD_ITALIC);
            Drawable nullIcon = context.getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_24dp);
            holder.cardFace.setImageDrawable(nullIcon);
        }

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
