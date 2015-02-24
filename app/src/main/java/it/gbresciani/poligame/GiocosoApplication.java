package it.gbresciani.poligame;

import android.content.Intent;

import com.orm.SugarApp;

import it.gbresciani.poligame.services.InitDBService;

public class GiocosoApplication extends SugarApp {

    @Override
    public void onCreate() {
        /* Start the intentService that initialize the database */
        Intent initDBIntent = new Intent(this, InitDBService.class);
        startService(initDBIntent);
        super.onCreate();

    }
}
