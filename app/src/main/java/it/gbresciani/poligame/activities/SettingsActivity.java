package it.gbresciani.poligame.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.NoEmailEvent;
import it.gbresciani.poligame.fragments.SettingsFragment;
import it.gbresciani.poligame.helper.BusProvider;

public class SettingsActivity extends ActionBarActivity {

    private Bus BUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .add(R.id.settings_content, SettingsFragment.newInstance())
                .commit();
        BUS = BusProvider.getInstance();
    }

    @Override protected void onResume() {
        super.onResume();
        BUS.register(this);
    }

    @Override protected void onPause() {
        BUS.unregister(this);
        super.onPause();
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Subscribe public void noEmail(NoEmailEvent noEmailEvent){
        Snackbar.with(this).
                text(R.string.no_email_snackbar)
                .type(SnackbarType.SINGLE_LINE)
                .duration(1000l)
                .show(this);
    }
}
