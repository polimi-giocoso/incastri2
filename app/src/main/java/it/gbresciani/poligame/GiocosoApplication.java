package it.gbresciani.poligame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.orm.SugarApp;

import it.gbresciani.poligame.services.InitDBService;

public class GiocosoApplication extends SugarApp {

    @Override
    public void onCreate() {
        // Start the intentService that initialize the database if is the first run
        Log.d("GiocosoApplication", "onCreate");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {
            Intent initDBIntent = new Intent(this, InitDBService.class);
            startService(initDBIntent);            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }
        super.onCreate();

    }
}
