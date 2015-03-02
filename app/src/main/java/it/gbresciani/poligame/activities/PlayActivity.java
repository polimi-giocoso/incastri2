package it.gbresciani.poligame.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import it.gbresciani.poligame.R;
import it.gbresciani.poligame.fragments.SyllablesFragment;
import it.gbresciani.poligame.fragments.WordsFragment;


/*
*
* This Activity contains the two fragments (words and syllables) and manages the game logic trough a finite-state machine
*
* */
public class PlayActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initUI();
    }

    /*
    * Initialize the UI, adding the two fragments
    */
    private void initUI() {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // TODO: read from preferences
        WordsFragment wordsFragment = WordsFragment.newInstance(4);
        SyllablesFragment syllablesFragment = SyllablesFragment.newInstance(4);

        ft.add(R.id.words_frame_layout, wordsFragment);
        ft.add(R.id.syllables_frame_layout, syllablesFragment);

        ft.commit();

    }
}
