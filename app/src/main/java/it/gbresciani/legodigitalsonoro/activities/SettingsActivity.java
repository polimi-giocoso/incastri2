package it.gbresciani.legodigitalsonoro.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.fragments.SettingsFragment;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .add(R.id.settings_content, SettingsFragment.newInstance())
                .commit();
    }

}
