package it.gbresciani.poligame.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import it.gbresciani.poligame.R;
import it.gbresciani.poligame.fragments.SettingsFragment;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .add(R.id.settings_content, SettingsFragment.newInstance())
                .commit();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
