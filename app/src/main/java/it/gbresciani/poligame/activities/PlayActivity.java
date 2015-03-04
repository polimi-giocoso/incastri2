package it.gbresciani.poligame.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

    private int noPages;
    private int noSyllables;

    private WordsFragment currentWordsFragment;
    private SyllablesFragment currentSyllablesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        // Get match configuration
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        noPages = sp.getInt(getString(R.string.setting_no_pages_key), 1);
        noSyllables = sp.getInt(getString(R.string.setting_no_syllables_key), 4);

        initUI(noPages, noSyllables);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override public void run() {
                nextPage();
            }
        }, 2000);
    }

    /**
    * Initialize the UI, adding the two fragments.
    *
    * @param noPages The number of pages of the current match.
    * @param noSyllables The number of syllables to display for the current match.
    *
    */
    private void initUI(int noPages, int noSyllables) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        currentWordsFragment = WordsFragment.newInstance(noSyllables);
        currentSyllablesFragment = SyllablesFragment.newInstance(noSyllables);

        ft.replace(R.id.words_frame_layout, currentWordsFragment);
        ft.replace(R.id.syllables_frame_layout, currentSyllablesFragment);

        ft.commit();
    }

    /**
    * Go to the next page, removing the old fragments and adding new ones.
    *
    */
    private void nextPage() {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        currentWordsFragment = WordsFragment.newInstance(noSyllables);
        currentSyllablesFragment = SyllablesFragment.newInstance(noSyllables);

        //ft.add(R.id.words_frame_layout, currentWordsFragment);
        ft.replace(R.id.syllables_frame_layout, currentSyllablesFragment);

        ft.commit();
    }


}
