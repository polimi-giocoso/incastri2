package it.gbresciani.poligame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.helper.BusProvider;


public class MainActivity extends FragmentActivity {

    private Bus BUS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BUS = BusProvider.getInstance();
        ButterKnife.inject(this);
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

}
