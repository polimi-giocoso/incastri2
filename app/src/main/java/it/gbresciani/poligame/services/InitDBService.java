package it.gbresciani.poligame.services;

import android.app.IntentService;
import android.content.Intent;

import it.gbresciani.poligame.model.Word;

public class InitDBService extends IntentService {

    private final static String TAG = "InitDBService";

    public InitDBService() {
        super("InitDBService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Word word = new Word("casa", "ca", "sa", "", "");
        word.save();
    }
}


