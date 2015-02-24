package it.gbresciani.poligame.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import it.gbresciani.poligame.model.Word;

public class InitDBService extends IntentService {

    private final static String TAG = "InitDBService";

    private Gson gson;

    public InitDBService() {
        super("InitDBService");
        gson = new Gson();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AssetManager assetManager = getAssets();

        try {
            InputStream JSONInputStream = assetManager.open("words.json");
            parseAndSaveWords(JSONInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Parses the input stream and returns a List of Word objects
    */
    private void parseAndSaveWords(InputStream in) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        reader.beginArray();
        while (reader.hasNext()) {
            Word word = gson.fromJson(reader, Word.class);
            word.save();
        }
        reader.endArray();
        reader.close();
    }
}


