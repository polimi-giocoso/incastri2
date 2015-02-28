package it.gbresciani.poligame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.LoadingEvent;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.services.InitDBService;


public class MainActivity extends FragmentActivity {

    private Bus BUS;

    @InjectView(R.id.play_button) Button playButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BUS = BusProvider.getInstance();
        ButterKnife.inject(this);

    }

    @OnClick(R.id.play_button)
    public void startPlayActivity(){
        Intent playActivityIntent = new Intent(this, PlayActivity.class);
        startActivity(playActivityIntent);
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

}
