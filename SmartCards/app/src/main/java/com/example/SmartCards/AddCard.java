package com.example.SmartCards;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddCard extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;

    ImageView imageUpload;
    EditText uploadedCardName;
    Button uploadButton;
    Uri uploadedURI;

    boolean isImageUploaded;

    private DeckManager deckManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        imageUpload = (ImageView) findViewById(R.id.imageUpload);
        uploadedCardName = (EditText) findViewById(R.id.editCardNameField);
        uploadButton = (Button) findViewById(R.id.editCardButton);

        // TODO: load in the deck manager into an instance variable
        deckManager = (DeckManager) getIntent().getSerializableExtra(LandingPageActivity.DECK_MANAGER);

        imageUpload.setOnClickListener(this);
        uploadButton.setOnClickListener(this);

        isImageUploaded = false;
    }

    @Override
    public void onClick(View v) {
        if(v == imageUpload){
            //Would like to get back to ACTION_PICK because it only gives you the image selector which is nicer, yet the URI permissions expire
            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        }
        else if(v == uploadButton) {
            if(!isImageUploaded || uploadedCardName.getText().toString() == null){
                Toast error = Toast.makeText(getApplicationContext(), "please add name and image", Toast.LENGTH_SHORT);
                error.show();
            }
            else {
                PlayingCard newCard = new PlayingCard(this, uploadedCardName.getText().toString(), uploadedURI);
                EditDeck.addCardToDeck(newCard); // TODO: use the instance variable here
                setResult(RESULT_OK);
                this.finish();
            }
        }
    }

    //Called when an image is selected from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            uploadedURI = data.getData();
            imageUpload.setImageURI(uploadedURI);
            isImageUploaded = true;
        }
    }


    @Override
    public void finish(){
        super.finish();

    }
}
