package it.gbresciani.poligame;

import android.app.Application;
import android.content.Intent;

import it.gbresciani.poligame.services.InitDBService;

public class GiocosoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Intent initDBIntent = new Intent(this, InitDBService.class);
        startService(initDBIntent);

        /* Start the intentService that fill the database (check if exists) */
    }
}
