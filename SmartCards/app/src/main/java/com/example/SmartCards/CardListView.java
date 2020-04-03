package com.example.SmartCards;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.SmartCards.R;

import java.util.List;

public class CardListView extends BaseAdapter {

    private Activity context;
    private List<PlayingCard> deck;



    public CardListView(Activity context, List<PlayingCard> deck) {
        this.context = context;
        this.deck = deck;

    }


    @Override
    public int getCount() {
        return deck.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View singleListTile = context.getLayoutInflater().inflate(R.layout.card_list_view,null);
        ViewHolder viewHolder = new ViewHolder(singleListTile);


        viewHolder.cardFace.setImageURI(deck.get(position).getImageAddress());
        viewHolder.cardName.setText(deck.get(position).getName());

        return singleListTile;
    }

    class ViewHolder {
        TextView cardName;
        ImageView cardFace;
        ViewHolder(View v) {
            cardName = (TextView) v.findViewById(R.id.cardPreviewText);
            cardFace = (ImageView) v.findViewById(R.id.cardPreviewImageView);
        }
    }

}
