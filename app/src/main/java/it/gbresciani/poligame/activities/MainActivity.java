package it.gbresciani.poligame.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.ProgressChangeEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.services.InitDBService;


public class MainActivity extends FragmentActivity {

    private Bus BUS;

    @InjectView(R.id.play_layout) LinearLayout playLayout;
    @InjectView(R.id.db_progress_bar) ProgressBar dbProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BUS = BusProvider.getInstance();
        ButterKnife.inject(this);
        checkDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BUS.register(this);
    }

    @Override
    protected void onPause() {
        BUS.unregister(this);
        super.onPause();
    }

    /**
     * React to a progress change
     */
    @Subscribe public void progressChange(ProgressChangeEvent progressChangeEvent) {
        if (progressChangeEvent.getProgress() == 0) {
            playLayout.setVisibility(View.INVISIBLE);
            dbProgressBar.setVisibility(View.VISIBLE);
        }
        if (progressChangeEvent.getProgress() == 100) {
            playLayout.setVisibility(View.VISIBLE);
            dbProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.play_button)
    public void startPlayActivity() {
        Intent playActivityIntent = new Intent(this, PlayActivity.class);
        startActivity(playActivityIntent);
    }

    @OnClick(R.id.settings_button)
    public void startSettingsActivity() {
        Intent playActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(playActivityIntent);
    }

    private void checkDB(){
        BUS.post(new ProgressChangeEvent(0));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {
            Intent initDBIntent = new Intent(this, InitDBService.class);
            startService(initDBIntent);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }else{
            BUS.post(new ProgressChangeEvent(100));
        }
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

}
