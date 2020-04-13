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
    private List<PlayingCard> deck;



    public CardListAdapter(Activity context, List<PlayingCard> deck) {
        this.context = context;
        this.deck = deck;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View singleListTile = context.getLayoutInflater().inflate(R.layout.card_list_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(singleListTile);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cardFace.setImageURI(deck.get(position).getImageAddress());
        holder.cardNumber.setText(Integer.toString(position+1));
        holder.cardName.setText(deck.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return deck.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardName, cardNumber;
        ImageView cardFace;

        ViewHolder(View v) {
            super(v);
            cardName = (TextView) v.findViewById(R.id.cardPreviewText);
            cardNumber = (TextView) v.findViewById(R.id.cardNumberText);
            cardFace = (ImageView) v.findViewById(R.id.cardPreviewImageView);
        }
    }

}
