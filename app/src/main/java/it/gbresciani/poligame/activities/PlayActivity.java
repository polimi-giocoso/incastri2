package it.gbresciani.poligame.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.PageCompletedEvent;
import it.gbresciani.poligame.events.SyllableSelectedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.fragments.SyllablesFragment;
import it.gbresciani.poligame.fragments.WordsFragment;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.helper.Helper;
import it.gbresciani.poligame.model.Syllable;
import it.gbresciani.poligame.model.Word;


/**
 * This Activity contains the two fragments (words and syllables) and manages the game logic using bus messages
 */
public class PlayActivity extends ActionBarActivity {

    private int noPages;
    private int currentPageNum;
    private int noSyllables;
    private String syllableYetSelected = "";

    // Game Page state variables
    private int currentPageWordsToFindNum;
    private ArrayList<Word> currentPageWordsAvailable;

    private Bus BUS;

    private WordsFragment currentWordsFragment;
    private SyllablesFragment currentSyllablesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        BUS = BusProvider.getInstance();

        // Get match configuration
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        noPages = sp.getInt(getString(R.string.setting_no_pages_key), 1);
        noSyllables = sp.getInt(getString(R.string.setting_no_syllables_key), 4);

        startGame();
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
     * Start the game
     */
    private void startGame() {
        currentPageNum = 1;
        nextPage(currentPageNum);
    }

    /**
     * Initialize a page, adding the two fragments and passing them the calculated syllables and words
     */
    private void nextPage(int pageNum) {

        // Determine words and syllables for the page
        ArrayList<Syllable> syllables = Helper.chooseSyllables(noSyllables);
        currentPageWordsAvailable = Helper.permuteSyllablesInWords(syllables, 2);

        currentPageWordsToFindNum = currentPageWordsAvailable.size() <= 4 ? currentPageWordsAvailable.size() : 4;

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        currentWordsFragment = WordsFragment.newInstance(currentPageWordsAvailable);
        currentSyllablesFragment = SyllablesFragment.newInstance(syllables);

        ft.replace(R.id.words_frame_layout, currentWordsFragment);
        ft.replace(R.id.syllables_frame_layout, currentSyllablesFragment);

        ft.commit();
    }

    /**
     * React to a PageCompletedEvent, opening a new one or ending the game
     */
    @Subscribe public void pageCompleted(PageCompletedEvent pageCompletedEvent) {
        Log.d("pageCompleted", String.valueOf(pageCompletedEvent.getPageNumber()) + "/" + String.valueOf(noPages));

        if (pageCompletedEvent.getPageNumber() == noPages) {
            //TODO Partita finita
            Log.d("pageCompleted", "PARTITA TERMINATA!");
        } else {
            currentPageNum++;
            nextPage(currentPageNum);
        }
    }

    /**
     * React to a SyllableSelectedEvent
     */
    @Subscribe public void syllableSelected(SyllableSelectedEvent syllableSelectedEvent) {
        if ("".equals(syllableYetSelected)) {
            syllableYetSelected = syllableSelectedEvent.getSyllable();
        } else {
            String selectedWord = syllableYetSelected + syllableSelectedEvent.getSyllable();
            syllableYetSelected = "";
            //TODO ask confirmation with dialog
            Word word = wordByLemma(selectedWord);
            // If exists and it's new
            if (word != null) {
                Log.d("wordSelected", selectedWord + " exists!");
                BUS.post(new WordSelectedEvent(word, true, currentPageWordsAvailable.contains(word)));
            } else {
                Log.d("wordSelected", selectedWord + " does not exists!");
                BUS.post(new WordSelectedEvent(word, false, false));
            }
        }
    }

    /**
     * React to a wordSelected
     */
    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        Word selectedWord = wordSelectedEvent.getWord();
        if (wordSelectedEvent.isCorrect() && wordSelectedEvent.isNew()) {
            currentPageWordsToFindNum--;
            currentPageWordsAvailable.remove(selectedWord);
            if (currentPageWordsToFindNum == 0) {
                BUS.post(new PageCompletedEvent(currentPageNum));
            }
        }
    }



    /*  Helper Methods  */

    /**
     * Get a word given its lemma
     *
     * @param word The lemma of the word to find.
     * @return The Word if exists, null if it doesn't
     */
    private Word wordByLemma(String word) {
        List<Word> wordFound = Word.find(Word.class, "lemma = ?", word);
        if (wordFound.size() > 0) {
            return wordFound.get(0);
        } else {
            return null;
        }
    }

}
