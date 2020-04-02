package com.example.herroworld;

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

public class AddCard extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;

    ImageView imageUpload;
    EditText uploadedCardName;
    Button uploadButton;

    boolean isImageUploaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        imageUpload = (ImageView) findViewById(R.id.imageUpload);
        uploadedCardName = (EditText) findViewById(R.id.cardNameField);
        uploadButton = (Button) findViewById(R.id.AddCardButton);

        imageUpload.setOnClickListener(this);
        uploadButton.setOnClickListener(this);

        isImageUploaded = false;
    }

    @Override
    public void onClick(View v) {
        if(v == imageUpload){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        }
        else {
            this.finish();
        }

    }

    //Called when an image is selected from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            imageUpload.setImageURI(selectedImage);
            isImageUploaded = true;
        }
    }


    @Override
    public void finish(){
        if(isImageUploaded && uploadedCardName != null){

        }
        super.finish();

    }
}
