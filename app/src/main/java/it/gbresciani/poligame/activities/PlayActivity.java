package it.gbresciani.poligame.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.NextPageEvent;
import it.gbresciani.poligame.events.PageCompletedEvent;
import it.gbresciani.poligame.events.SyllableSelectedEvent;
import it.gbresciani.poligame.events.WordConfirmedEvent;
import it.gbresciani.poligame.events.WordDismissedEvent;
import it.gbresciani.poligame.events.WordSelectedEvent;
import it.gbresciani.poligame.fragments.PageCompletedFragment;
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
    private int currentPageNum = 0;
    private int noSyllables;
    private String syllableYetSelected = "";
    private int backPressedCount = 0;

    // Game Page state variables
    private int currentPageWordsToFindNum;
    private ArrayList<Word> currentPageWordsAvailable;

    private Handler timeoutHandler;
    private Bus BUS;
    private SoundPool soundPool;

    private int correctSound;
    private int wrongSound;
    private int sameSound;

    private TextToSpeech mTTS;
    private boolean ttsConfigured = false;
    private int TTS_CHECK = 0;

    private WordsFragment currentWordsFragment;
    private SyllablesFragment currentSyllablesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        BUS = BusProvider.getInstance();
        timeoutHandler = new Handler();
        ButterKnife.inject(this);

        loadPref();
        loadSound();
        checkTTS();

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
        nextPage();
    }

    /**
     * Initialize a page, adding the two fragments and passing them the calculated syllables and words
     */
    private void nextPage() {

        currentPageNum++;

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

    private void showPageCompleted() {

        Handler h = new Handler();

        // Waiting for the owrd dialog to disappear
        h.postDelayed(new Runnable() {
            @Override public void run() {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                PageCompletedFragment pageCompletedFragment = PageCompletedFragment.newInstance();

                ft.setCustomAnimations(R.animator.test, R.animator.test);

                ft.replace(R.id.syllables_frame_layout, pageCompletedFragment);

                ft.commit();
            }
        }, WordConfirmDialogFragment.WORD_DIALOG_TIMEOUT * 2);
    }


    /**
     * React to a PageCompletedEvent, changing the layout
     */
    @Subscribe public void pageCompleted(PageCompletedEvent pageCompletedEvent) {
            showPageCompleted();
    }

    /**
     * React to a NextPageEvent, opening a new one or ending the game
     */
    @Subscribe public void nextPage(NextPageEvent nextPageEvent) {

        if (currentPageNum == noPages) {
            showEndDialog();
        } else {
            nextPage();
        }
    }

    /**
     * React to a SyllableSelectedEvent
     */
    @Subscribe public void syllableSelected(SyllableSelectedEvent syllableSelectedEvent) {
        say(syllableSelectedEvent.getSyllable().getVal());
        if ("".equals(syllableYetSelected)) {
            syllableYetSelected = syllableSelectedEvent.getSyllable().getVal();
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
            String selectedWord = syllableYetSelected + syllableSelectedEvent.getSyllable().getVal();
            syllableYetSelected = "";
            showWordConfirmDialog(selectedWord);
        }
    }


    /**
     * React to a wordConfirmed
     */
    @Subscribe public void wordConfirmed(WordConfirmedEvent wordConfirmedEvent) {
        timeoutHandler.removeCallbacksAndMessages(null);
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
        timeoutHandler.removeCallbacksAndMessages(null);
        Word selectedWord = wordSelectedEvent.getWord();
        if (wordSelectedEvent.isCorrect() && wordSelectedEvent.isNew()) {
            // Play correct sound
            soundPool.play(correctSound, 1f, 1f, 0, 0, 1f);
            // Update number of words to found
            currentPageWordsToFindNum--;
            currentPageWordsAvailable.remove(selectedWord);
            // Pronounce the word
            // Check if page is completed
            if (currentPageWordsToFindNum == 0) {
                BUS.post(new PageCompletedEvent(currentPageNum));
            }
        } else if (wordSelectedEvent.isCorrect() && !wordSelectedEvent.isNew()) {
            soundPool.play(sameSound, 1f, 1f, 0, 0, 1f);
        } else {
            soundPool.play(wrongSound, 1f, 1f, 0, 0, 1f);
        }
    }

    /*  Helper Methods  */


    private void say(String string) {
        if (ttsConfigured) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTTS.speak(string, TextToSpeech.QUEUE_FLUSH, null, string);
            } else {
                mTTS.speak(string, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_tts_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        correctSound = soundPool.load(this, R.raw.correct, 1);
        wrongSound = soundPool.load(this, R.raw.wrong, 1);
        sameSound = soundPool.load(this, R.raw.same, 1);
    }

    private void loadPref() {
        // Get match configuration
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        noPages = sp.getInt(getString(R.string.setting_no_pages_key), 1);
        noSyllables = sp.getInt(getString(R.string.setting_no_syllables_key), 4);
    }

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

    private void showEndDialog() {
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

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    private void checkTTS() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, TTS_CHECK);
    }

    /**
     * Called on TTS check
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override public void onInit(int status) {
                        ttsConfigured = true;
                        setTTSLang(Locale.ITALIAN);
                    }
                });
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void setTTSLang(Locale locale){
        if(ttsConfigured){
            mTTS.setLanguage(locale);
        }else{
            Toast.makeText(this, getString(R.string.no_tts_message), Toast.LENGTH_SHORT).show();
        }
    }

}
