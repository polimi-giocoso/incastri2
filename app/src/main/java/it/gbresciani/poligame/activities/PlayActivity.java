package it.gbresciani.poligame.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        // Get match configuration
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        noPages = sp.getInt(getString(R.string.setting_no_pages_key), 1);
        noSyllables = sp.getInt(getString(R.string.setting_no_syllables_key), R.id.four_syllables_radio_button);

        initUI(noPages, noSyllables);
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

        WordsFragment wordsFragment = WordsFragment.newInstance(noSyllables);
        SyllablesFragment syllablesFragment = SyllablesFragment.newInstance(noSyllables);

        ft.add(R.id.words_frame_layout, wordsFragment);
        ft.add(R.id.syllables_frame_layout, syllablesFragment);

        ft.commit();





    }

}
