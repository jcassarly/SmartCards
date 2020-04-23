package com.example.SmartCards;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class PlayingCard implements Serializable {

    //private Context context;
    private String cardName, tempImageAddress, savedImageAddress, idName;
    private boolean isSaved;

    public PlayingCard(Context context, String cardName, Uri tempImageAddress){
        //this.context = context;
        this.cardName = cardName;
        this.tempImageAddress = tempImageAddress.toString();
        this.isSaved = false;
    }

    public PlayingCard(Context context, String imageAddress){
        //TODO: Error handling if the image address is bad
        savedImageAddress = imageAddress;
        this.idName = new File(savedImageAddress).getName();
        SharedPreferences sharedPreferences = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        this.cardName = sharedPreferences.getString(idName,"DefaultCardName");
        isSaved = true;
    }

    public String getCardName() {
        return cardName;
    }

    public Uri getImageAddress() {
        if(isSaved){
            return Uri.parse(savedImageAddress);
        }
        else{
            return Uri.parse(tempImageAddress);
        }
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
        isSaved = false;
    }

    public String getIdName(){
        return idName;
    }

    public void setTempImageAddress(Uri tempImageAddress) {
        this.tempImageAddress = tempImageAddress.toString();
        isSaved = false;
    }

    public boolean isSaved(){
        return isSaved;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void save(Context context) throws IOException {
        if(!isSaved) {
            idName = String.valueOf(DeckManager.getNextID(context));

            //Save Card Image
            File dir = context.getDir("deck", context.MODE_PRIVATE);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), getImageAddress());
            File imageFile = new File(dir, idName);

            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            savedImageAddress = imageFile.getAbsolutePath();

            //Save Card Name
            SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(idName, cardName);
            editor.apply();

            isSaved = true;
        }
    }

    public void delete(Context context){
        if(isSaved){
            new File(savedImageAddress).delete();
            isSaved = false;
            savedImageAddress = null;
            SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
            sharedPref.edit().remove(idName);
            idName = null;
        }
    }


}
