package it.gbresciani.legodigitalsonoro.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.events.ExitEvent;
import it.gbresciani.legodigitalsonoro.events.NextPageEvent;
import it.gbresciani.legodigitalsonoro.events.PageCompletedEvent;
import it.gbresciani.legodigitalsonoro.events.RepeatEvent;
import it.gbresciani.legodigitalsonoro.events.SyllableSelectedEvent;
import it.gbresciani.legodigitalsonoro.events.WordClickedEvent;
import it.gbresciani.legodigitalsonoro.events.WordConfirmedEvent;
import it.gbresciani.legodigitalsonoro.events.WordDismissedEvent;
import it.gbresciani.legodigitalsonoro.events.WordSelectedEvent;
import it.gbresciani.legodigitalsonoro.fragments.EndGameDialogFragment;
import it.gbresciani.legodigitalsonoro.fragments.PageCompletedFragment;
import it.gbresciani.legodigitalsonoro.fragments.SyllablesFragment;
import it.gbresciani.legodigitalsonoro.fragments.WordConfirmDialogFragment;
import it.gbresciani.legodigitalsonoro.fragments.WordsFragment;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;
import it.gbresciani.legodigitalsonoro.helper.Helper;
import it.gbresciani.legodigitalsonoro.model.GameStat;
import it.gbresciani.legodigitalsonoro.model.Syllable;
import it.gbresciani.legodigitalsonoro.model.Word;
import it.gbresciani.legodigitalsonoro.model.WordStat;
import it.gbresciani.legodigitalsonoro.services.GenericIntentService;


/**
 * This Activity contains the two fragments (words and syllables) and manages the game logic using bus messages
 */
public class PlayActivity extends FragmentActivity {

    // Pref
    private int noPages;
    private int noSyllables;

    // Game Page state variables
    private int currentPageWordsToFindNum;
    private ArrayList<Word> currentPageWordsAvailable;
    private int currentPageNum = 0;
    private String syllableYetSelected = "";
    private int backPressedCount = 0;

    // Helpers
    private Handler timeoutHandler;
    private Bus BUS;
    private SoundPool soundPool;

    // Sounds
    private int correctSound;
    private int wrongSound;
    private int sameSound;

    // TTS
    private TextToSpeech mTTS;
    private boolean ttsConfigured = false;
    private int TTS_CHECK_ITA = 0;

    // Stats
    private GameStat gameStat;
    private ArrayList<WordStat> wordStats = new ArrayList<>();


    /* ----------------------------- Activity Lifecycle Methods ----------------------------- */


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

    @Override protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (backPressedCount == 5) {
            super.onBackPressed();
        }
        backPressedCount++;
    }


    /* ----------------------------- Game Flow Methods ----------------------------- */


    /**
     * Start the game
     */
    private void startGame() {
        gameStat = new GameStat();
        gameStat.setStartDate(new Date());
        nextPage();
    }

    /**
     * Restart the game
     */
    private void restartGame() {
        gameStat = new GameStat();
        wordStats = new ArrayList<>();
        gameStat.setStartDate(new Date());
        currentPageNum = 0;
        syllableYetSelected = "";
        backPressedCount = 0;
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

        WordsFragment wordsFragment = WordsFragment.newInstance(currentPageWordsAvailable);
        SyllablesFragment syllablesFragment = SyllablesFragment.newInstance(syllables);

        ft.replace(R.id.words_frame_layout, wordsFragment);
        ft.replace(R.id.syllables_frame_layout, syllablesFragment);

        ft.commit();
    }

    /**
     * Replace the syllables fragment with a PageCompletedFragment
     */
    private void showPageCompleted() {

        Handler h = new Handler();

        // Waiting for the word dialog to disappear
        h.postDelayed(new Runnable() {
            @Override public void run() {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                PageCompletedFragment pageCompletedFragment = PageCompletedFragment.newInstance();

                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                ft.replace(R.id.syllables_frame_layout, pageCompletedFragment);

                ft.commit();
            }
        }, WordConfirmDialogFragment.WORD_DIALOG_TIMEOUT * 2);
    }

    /**
     * Show the dialog to confirm a word
     *
     * @param word the selected word
     */
    private void showWordConfirmDialog(String word) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        WordConfirmDialogFragment wd = WordConfirmDialogFragment.newInstance(word);

        wd.show(ft, "dialog");
    }

    /**
     * Show the end game dialog
     */
    private void showEndDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        EndGameDialogFragment ed = EndGameDialogFragment.newInstance();

        ed.show(ft, "endDialog");
    }


    /* ----------------------------- Bus Events Methods ----------------------------- */


    /**
     * React to a PageCompletedEvent, changing the layout
     */
    @Subscribe public void pageCompleted(PageCompletedEvent pageCompletedEvent) {
        // If last page store stats
        if (currentPageNum == noPages) {
            gameStat.setEndDate(new Date());
            storeSendStats();
        }
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
        saySyllable(syllableSelectedEvent.getSyllable());
        if ("".equals(syllableYetSelected)) {
            syllableYetSelected = syllableSelectedEvent.getSyllable().getVal();
            timeoutHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    //If no other syllable has been selected dismiss
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
     * React to a WordConfirmedEvent
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
     * React to a WordSelectedEvent
     */
    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        timeoutHandler.removeCallbacksAndMessages(null);
        Word selectedWord = wordSelectedEvent.getWord();
        if (wordSelectedEvent.isCorrect() && wordSelectedEvent.isNew()) {
            // Save Stats
            WordStat wordStat = new WordStat(new Date(), selectedWord.getLemma(), currentPageNum, null);
            wordStats.add(wordStat);
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

    /**
     * React to a ExitEvent
     */
    @Subscribe public void Exit(ExitEvent exitEvent) {
        finish();
    }

    /**
     * React to a RepeatEvent
     */
    @Subscribe public void Repeat(RepeatEvent repeatEvent) {
        restartGame();
    }

    /**
     * React to a WordClickedEvent
     */
    @Subscribe public void wordClicked(WordClickedEvent wordClickedEvent) {
        sayWord(wordClickedEvent.getWord(), wordClickedEvent.getLANG());
    }


    /* ----------------------------- Helper Methods ----------------------------- */


    /**
     * Loads the game sounds
     */
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

    /**
     * Loads the game preferences
     */
    private void loadPref() {
        // Get match configuration
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        noPages = sp.getInt(getString(R.string.setting_no_pages_key), 1);
        noSyllables = sp.getInt(getString(R.string.setting_no_syllables_key), 4);
    }

    /**
     * Store statistics in the db and send the through email
     */
    private void storeSendStats() {
        gameStat.save();
        for (WordStat ws : wordStats) {
            ws.setGameStat(gameStat);
            ws.save();
        }
        boolean send = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.setting_collect_key), false);
        if (send) {
            GenericIntentService.sendOneGameStat(this, gameStat.getId());
        }
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


    /* ----------------------------- TTS Methods ----------------------------- */


    /**
     * Pronounces a Word in the specified language
     *
     * @param word The word to pronounce
     * @param lang The Locale representing the language to use
     */
    private void sayWord(Word word, Locale lang) {
        if (ttsConfigured) {
            mTTS.setLanguage(lang);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTTS.speak(word.getLemma(), TextToSpeech.QUEUE_ADD, null, word.getLemma());

            } else {
                mTTS.speak(word.getEng(), TextToSpeech.QUEUE_ADD, null);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_tts_message), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Pronounces a Word in Italian
     *
     * @param syllable The syllable to pronounce
     */
    private void saySyllable(Syllable syllable) {
        if (ttsConfigured) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTTS.setLanguage(Locale.ITALIAN);
                mTTS.speak(syllable.getVal(), TextToSpeech.QUEUE_ADD, null, syllable.getVal());
            }
        } else {
            Toast.makeText(this, getString(R.string.no_tts_message), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * startActivityForResult to check the tts support (see {@link PlayActivity#onActivityResult} for response)
     */
    private void checkTTS() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, TTS_CHECK_ITA);
    }

    /**
     * Called on TTS check
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK_ITA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override public void onInit(int status) {
                        ttsConfigured = true;
                        mTTS.setLanguage(Locale.ITALIAN);
                    }
                });
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }
}