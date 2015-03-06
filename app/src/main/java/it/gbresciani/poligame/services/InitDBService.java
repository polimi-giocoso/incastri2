package it.gbresciani.poligame.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.orm.SugarRecord;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import it.gbresciani.poligame.events.LoadingEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.model.Syllable;
import it.gbresciani.poligame.model.Word;

public class InitDBService extends IntentService {

    private final static String TAG = "InitDBService";

    private Gson gson;
    private Bus BUS;
    private Handler handler = new Handler(Looper.getMainLooper());

    public InitDBService() {
        super("InitDBService");
        gson = new Gson();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        BUS = BusProvider.getInstance();
        BUS.register(this);

        AssetManager assetManager = getAssets();
        BUS.post(new LoadingEvent(LoadingEvent.STATE_STARTED));

        try {
            InputStream JSONInputStreamWords = assetManager.open("words.json");
            InputStream JSONInputStreamSyllables = assetManager.open("syllables.json");
            flushDB();
            parseAndSaveWords(JSONInputStreamWords);
            parseAndSaveSyllables(JSONInputStreamSyllables);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BUS.post(new LoadingEvent(LoadingEvent.STATE_FINISHED));
    }

    @Override
    public void onDestroy() {
        BUS.unregister(this);
        super.onDestroy();
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
    /*
    * Parses the input stream and returns a List of Syllables objects
    */
    private void parseAndSaveSyllables(InputStream in) throws IOException {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        reader.beginArray();
        while (reader.hasNext()) {
            Syllable syllable = gson.fromJson(reader, Syllable.class);
            syllable.save();
        }
        reader.endArray();
        reader.close();
    }

    /*
    * Remove all words from the database
    * */
    private void flushDB() {
        SugarRecord.deleteAll(Word.class);
        SugarRecord.deleteAll(Syllable.class);
    }
}


