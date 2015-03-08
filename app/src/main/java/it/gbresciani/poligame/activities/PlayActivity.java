package it.gbresciani.poligame.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.PageCompletedEvent;
import it.gbresciani.poligame.events.SyllableSelectedEvent;
import it.gbresciani.poligame.events.WordConfirmedEvent;
import it.gbresciani.poligame.events.WordDismissedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.fragments.SyllablesFragment;
import it.gbresciani.poligame.fragments.WordConfirmDialogFragment;
import it.gbresciani.poligame.fragments.WordsFragment;
import it.gbresciani.poligame.helper.BusProvider;
import it.gbresciani.poligame.helper.Helper;
import it.gbresciani.poligame.model.Syllable;
import it.gbresciani.poligame.model.Word;


/**
 * This Activity contains the two fragments (words and syllables) and manages the game logic using bus messages
 */
public class PlayActivity extends FragmentActivity {

    private int noPages;
    private int currentPageNum;
    private int noSyllables;
    private String syllableYetSelected = "";
    private int backPressedCount = 0;

    // Game Page state variables
    private int currentPageWordsToFindNum;
    private ArrayList<Word> currentPageWordsAvailable;

    private Handler timeoutHandler;
    private Bus BUS;

    private WordsFragment currentWordsFragment;
    private SyllablesFragment currentSyllablesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        BUS = BusProvider.getInstance();
        timeoutHandler = new Handler();
        ButterKnife.inject(this);

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
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        BUS.unregister(this);
        super.onPause();
    }

    @Override public void onBackPressed() {
        if (backPressedCount == 5) {
            super.onBackPressed();
        }
        backPressedCount++;
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
            showEndDialog();
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
            timeoutHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    //If no other syllable has been selected dissmiss
                    if (!"".equals(syllableYetSelected)) {
                        BUS.post(new WordDismissedEvent());
                        syllableYetSelected = "";
                    }
                }
            }, 3 * 1000);
        } else {
            String selectedWord = syllableYetSelected + syllableSelectedEvent.getSyllable();
            syllableYetSelected = "";
            showWordConfirmDialog(selectedWord);
        }
    }


    /**
     * React to a wordConfirmed
     */
    @Subscribe public void wordConfirmed(WordConfirmedEvent wordConfirmedEvent) {
        String confirmedWordString = wordConfirmedEvent.getWordConfirmed();
        Word word = wordByLemma(confirmedWordString);
        // If exists and it's new
        if (word != null) {
            Log.d("wordSelected", confirmedWordString + " exists!");
            BUS.post(new WordSelectedEvent(word, true, currentPageWordsAvailable.contains(word)));
        } else {
            Log.d("wordSelected", confirmedWordString + " does not exists!");
            BUS.post(new WordSelectedEvent(word, false, false));
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

    private void showWordConfirmDialog(String word) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Create and show the dialog.
        WordConfirmDialogFragment wd = WordConfirmDialogFragment.newInstance(word);

        wd.show(ft, "dialog");
    }

    private void showEndDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.message_congratulations)
                .setTitle(R.string.message_end);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
