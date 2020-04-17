package com.example.SmartCards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CardGridAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<PlayingCard> subdeck = new ArrayList();

    public CardGridAdapter(Context context, List<PlayingCard> subdeck){
        this.context = context;
        this.subdeck = subdeck;
    }

    @Override
    public int getCount() {
        return subdeck.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null){
            convertView = inflater.inflate(R.layout.card_grid_item, null);
        }

        ImageView cardGridImageView = convertView.findViewById(R.id.cardGridImageView);
        TextView cardGridText = convertView.findViewById(R.id.cardGridNameText);

        cardGridImageView.setImageURI(subdeck.get(position).getImageAddress());
        cardGridText.setText(subdeck.get(position).getName());

        return convertView;
    }
}
