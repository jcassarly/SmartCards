package com.example.SmartCards;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EditButtonAdapter extends RecyclerView.Adapter<EditButtonAdapter.ViewHolder> {

    private Activity context;
    private OnEditButtonListener listener;
    private EditButtons[] buttons;

    public EditButtonAdapter(Activity context, EditButtons[] buttons, OnEditButtonListener listener){
        this.context = context;
        this.buttons = buttons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EditButtonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View singleButton = context.getLayoutInflater().inflate(R.layout.edit_game_button, parent, false);
        ViewHolder viewHolder = new ViewHolder(singleButton, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EditButtonAdapter.ViewHolder holder, int position) {
        holder.buttonImage.setImageDrawable(EditButtons.getIcon(context,buttons[position]));
        holder.buttonText.setText(EditButtons.getButtonText(buttons[position]));
    }

    @Override
    public int getItemCount() {
        return  buttons.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView buttonText;
        ImageView buttonImage;
        OnEditButtonListener listener;

        ViewHolder(View v, OnEditButtonListener listener) {
            super(v);
            this.buttonText = (TextView) v.findViewById(R.id.editButtonNameText);
            this.buttonImage = (ImageView) v.findViewById(R.id.editButtonImage);
            this.listener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onButtonClick(getAdapterPosition());
        }
    }

    public interface OnEditButtonListener {
        void onButtonClick(int position);
    }

}
