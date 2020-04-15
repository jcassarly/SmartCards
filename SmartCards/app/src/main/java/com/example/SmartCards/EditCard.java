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

public class EditCard extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;

    ImageView imageUpload;
    EditText uploadedCardName;
    Button editCardButton,deleteCardButton;
    Uri uploadedURI;
    PlayingCard card;

    boolean isImageUploaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);


        imageUpload = (ImageView) findViewById(R.id.imageUpload);
        uploadedCardName = (EditText) findViewById(R.id.editCardNameField);
        editCardButton = (Button) findViewById(R.id.editCardButton);
        deleteCardButton = (Button) findViewById(R.id.deleteCardButton);

        imageUpload.setOnClickListener(this);
        editCardButton.setOnClickListener(this);
        deleteCardButton.setOnClickListener(this);

        isImageUploaded = true;

        card = EditDeck.deck.get(getIntent().getExtras().getInt("position"));
        uploadedURI = card.getImageAddress();
        imageUpload.setImageURI(uploadedURI);
        uploadedCardName.setText(card.getName());



    }

    @Override
    public void onClick(View v) {
        if(v == imageUpload){
            //Would like to get back to ACTION_PICK because it only gives you the image selector which is nicer, yet the URI permissions expire
            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        }
        else if(v == editCardButton) {
            if(!isImageUploaded || uploadedCardName.getText().toString() == ""){
                Toast error = Toast.makeText(getApplicationContext(), "please add name and image", Toast.LENGTH_SHORT);
                error.show();
            }
            else {
                card.setName(uploadedCardName.getText().toString());
                card.setImageAddress(uploadedURI);
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
