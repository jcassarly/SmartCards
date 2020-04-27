package com.example.SmartCards;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.chaquo.python.PyObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;


public class PlayingCard implements Serializable {

    private String cardName, tempImageAddress, savedImageAddress, idName, savedUrl;
    private boolean isSaved;

    public PlayingCard(Context context, String cardName, Uri tempImageAddress){
        this.cardName = cardName;
        this.tempImageAddress = tempImageAddress.toString();
        this.savedUrl = null;
        this.isSaved = false;
    }

    public PlayingCard(Context context, String imageAddress){
        savedImageAddress = imageAddress;
        this.idName = new File(savedImageAddress).getName();
        SharedPreferences sharedPreferences = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        this.cardName = sharedPreferences.getString(idName,"DefaultCardName");

        // TODO: consider changing the default image
        this.savedUrl = sharedPreferences.getString(idName + "_url", "https://www.complexsql.com/wp-content/uploads/2018/11/null.png");
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

    public String getSavedUrl()
    {
        return this.savedUrl;
    }

    public void setCardName(Context context, String cardName) {
        this.cardName = cardName;
        saveCardName(context, cardName);
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
    public boolean save(Context context) throws IOException {
        boolean needsToSave = !isSaved;
        if(needsToSave) {
            idName = String.valueOf(EditDeckManager.getNextID(context));

            Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.EFFECT_HEAVY_CLICK));
            }
            else
            {
                v.vibrate(750);
            }


            //Save Card Image
            if (!this.tempImageAddress.equals(this.savedImageAddress)) {
                File dir = context.getDir("deck", context.MODE_PRIVATE);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), getImageAddress());
                File imageFile = new File(dir, idName);

                Bitmap.CompressFormat format = (getImageAddress().toString().endsWith(".png")) ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
                try (FileOutputStream out = new FileOutputStream(imageFile)) {
                    bitmap.compress(format, 100, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                savedImageAddress = imageFile.getAbsolutePath();
            }

            PyObject pyImgurUploader = AbstractDeckManager.getPyImgurUploaderInstance();
            this.savedUrl = pyImgurUploader.callAttr("upload_image", savedImageAddress).toString();
            if (this.savedUrl != null)
            {
                saveUrl(context, this.savedUrl);
            }
            //Save Card Name
            saveCardName(context, cardName);
            isSaved = true;
        }
        // return if the image actually got saved
        return needsToSave;
    }

    private void saveUrl(Context context, String url)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(idName + "_url", url);
        editor.apply();
    }

    private void saveCardName(Context context, String name){
        SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(idName, name);
        editor.apply();
    }

    public void delete(Context context){
        if(isSaved){
            new File(savedImageAddress).delete();
            isSaved = false;
            savedImageAddress = null;
            SharedPreferences sharedPref = context.getSharedPreferences(EditDeck.SHARED_PREFS, context.MODE_PRIVATE);
            sharedPref.edit().remove(idName);
            sharedPref.edit().remove(idName + "_url");

            idName = null;
        }
    }


}
