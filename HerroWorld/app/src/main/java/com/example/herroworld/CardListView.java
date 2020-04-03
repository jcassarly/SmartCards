package com.example.herroworld;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.herroworld.PlayingCard;
import com.example.herroworld.R;

import java.util.List;

public class CardListView extends ArrayAdapter<String> {

    private Activity context;
    private List<PlayingCard> deck;


    public CardListView(Activity context, List<PlayingCard> deck) {
        super(context, R.layout.card_list_view);

        this.context = context;
        this.deck = deck;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View singleListTile = convertView;
        ViewHolder viewHolder = null;

        if(singleListTile == null){
            LayoutInflater layoutInflater = context.getLayoutInflater();
            singleListTile = layoutInflater.inflate(R.layout.card_list_view,null);
            viewHolder = new ViewHolder(singleListTile);
            singleListTile.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) singleListTile.getTag();
        }

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
