package it.gbresciani.legodigitalsonoro.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.events.ConnectedDeviceNameEvent;
import it.gbresciani.legodigitalsonoro.events.ConnectionStateChangeEvent;
import it.gbresciani.legodigitalsonoro.events.ExitEvent;
import it.gbresciani.legodigitalsonoro.events.MessageReadEvent;
import it.gbresciani.legodigitalsonoro.events.MessageWriteEvent;
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
import it.gbresciani.legodigitalsonoro.helper.BluetoothMessageHeader;
import it.gbresciani.legodigitalsonoro.helper.BusProvider;
import it.gbresciani.legodigitalsonoro.helper.GameState;
import it.gbresciani.legodigitalsonoro.helper.Helper;
import it.gbresciani.legodigitalsonoro.model.GameStat;
import it.gbresciani.legodigitalsonoro.model.Syllable;
import it.gbresciani.legodigitalsonoro.model.Word;
import it.gbresciani.legodigitalsonoro.model.WordStat;
import it.gbresciani.legodigitalsonoro.services.BluetoothService;
import it.gbresciani.legodigitalsonoro.services.GenericIntentService;


/**
 * This Activity contains the two fragments (words and syllables) and manages the game logic using bus messages
 */
public class PlayActivity extends FragmentActivity {

    private static final String TAG = "PlayActivity";

    public static final String MODE_SINGLE_PLAYER = "mode_single_player";
    public static final String MODE_MULTI_PLAYER = "mode_multi_player";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_TTS_CHECK = 3;

    // Pref
    private int noPages;
    private int noSyllables;

    // Game Page state variables
    private String syllableYetSelected = "";
    private int backPressedCount = 0;
    private GameState currentGameState = null;

    // Multi
    private boolean multi;
    private boolean masterRole = false;
    private boolean myTurn = false;

    // Helpers
    private Handler timeoutHandler;
    private Handler timeoutTurnHandler;
    private Bus BUS;
    private SoundPool soundPool;
    private Gson gson;

    // Sounds
    private int correctSound;
    private int wrongSound;
    private int sameSound;

    // TTS
    private TextToSpeech mTTS;
    private boolean ttsConfigured = false;

    // Stats
    private GameStat gameStat;
    private ArrayList<WordStat> wordStats = new ArrayList<>();

    // Bluetooth
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
    private String mConnectedDeviceName;

    // UI
    @InjectView(R.id.connection_text_view) TextView connTextView;
    @InjectView(R.id.game_loading_progress_bar) ProgressBar progressBar;
    private PlayActivity mActivity;
    private AlertDialog newGameAlertDialog;
    private AlertDialog waitDialog;
    private EndGameDialogFragment ed;


    /* ----------------------------- Activity Lifecycle Methods ----------------------------- */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        BUS = BusProvider.getInstance();
        timeoutHandler = new Handler();
        ButterKnife.inject(this);
        gson = new Gson();

        mActivity = this;

        loadPref();
        loadSound();
        checkTTS();

        // Single or Multi player (default single)
        Intent playIntent = getIntent();
        String action = playIntent.getAction();
        switch (action) {
            case MODE_SINGLE_PLAYER:
                multi = false;
                startGame();
                break;
            case MODE_MULTI_PLAYER:
                multi = true;
                // If Bluetooth is supported and enabled show dialog
                if (initBluetooth() && enableBluetooth()) {
                    setupMultiPlayer();
                }
                break;
            default:
                multi = false;
                startGame();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BUS.register(this);

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothService.start();
            }
        }
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
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (backPressedCount == 5) {
            super.onBackPressed();
        }
        backPressedCount++;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, show dialog and start the service
                    setupMultiPlayer();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "Bluetooth not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_TTS_CHECK:
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


    /* ----------------------------- Game Flow Methods ----------------------------- */


    /**
     * Start the game
     */
    private void startGame() {
        gameStat = new GameStat();
        gameStat.setStartDate(new Date());
        nextPage(constructPage());
    }

    /**
     * Restart the game
     */
    private void restartGame() {
        gameStat = new GameStat();
        wordStats = new ArrayList<>();
        gameStat.setStartDate(new Date());
        currentGameState = null;
        syllableYetSelected = "";
        backPressedCount = 0;
        nextPage(constructPage());
    }

    /**
     * Initialize a page, adding the two fragments and passing them the calculated syllables and words
     */
    private void nextPage(GameState gameState) {

        progressBar.setVisibility(View.INVISIBLE);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        WordsFragment wordsFragment = WordsFragment.newInstance(gameState.getWordsAvailable());
        SyllablesFragment syllablesFragment = SyllablesFragment.newInstance(gameState.getSyllables());

        ft.replace(R.id.words_frame_layout, wordsFragment);
        ft.replace(R.id.syllables_frame_layout, syllablesFragment);

        currentGameState = gameState;

        ft.commit();


        if (masterRole) {
            showWaitDialog(true);
        }
    }

    /**
     * Construct all the parameters need by a page new page
     */
    private GameState constructPage() {

        GameState newGameState = new GameState();

        //First page
        if (currentGameState == null) {
            newGameState.setPageNumber(0);
        } else {
            newGameState.nextPageNumber();
        }

        // Determine words and syllables for the page
        newGameState.setSyllables(Helper.chooseSyllables(noSyllables));
        newGameState.setWordsAvailable(Helper.permuteSyllablesInWords(newGameState.getSyllables(), 2));
        newGameState.setPageWordsToFindNum(newGameState.getWordsAvailable().size() <= 4 ? newGameState.getWordsAvailable().size() : 4);
        if (multi && masterRole) {
            sendMessage(BluetoothMessageHeader.PAGE_INFO + gson.toJson(newGameState, GameState.class));
        }
        return newGameState;
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
     * Make connection text view visible and start all Multi Player process before starting the game
     */
    private void setupMultiPlayer() {
        connTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        startBluetoothService();
        showMultiPlayerDialog();
    }

    private void myTurn(final boolean turn, boolean delayed) {
        myTurn = turn;
        if (timeoutTurnHandler != null) {
            timeoutTurnHandler.removeCallbacksAndMessages(null);
        }
        if (delayed) {
            timeoutTurnHandler = new Handler();
            timeoutTurnHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    showWaitDialog(!turn);
                }
            }, WordConfirmDialogFragment.WORD_DIALOG_TIMEOUT);
        } else {
            showWaitDialog(!turn);
        }
    }


    /* ----------------------------- Helper Methods ----------------------------- */


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
        if(!masterRole) {
            ed = EndGameDialogFragment.newInstance(true);
        }else{
            ed = EndGameDialogFragment.newInstance(false);
        }
        ed.show(ft, "endDialog");
    }

    private void showMultiPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        newGameAlertDialog = builder.setTitle(getString(R.string.new_game_multi_dialog_title))
                .setMessage(getString(R.string.new_game_multi_message))
                .setPositiveButton(getString(R.string.new_game_multi), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        showDeviceListActivity();
                        masterRole = true;
                    }
                })
                .setNeutralButton(getString(R.string.button_discoverable), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        mActivity.finish();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();

        newGameAlertDialog.show();
        // Prevent the dialog to close on click
        newGameAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ensureDiscoverable();
            }
        });
    }

    private void showWaitDialog(boolean show) {
        if (waitDialog != null) {
            waitDialog.dismiss();
        }
        if (show) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            waitDialog = builder.setMessage(getString(R.string.wait_turn)).setCancelable(false).create();
            waitDialog.show();
        }
    }


    /* ----------------------------- Bus Events Methods ----------------------------- */


    /**
     * React to a PageCompletedEvent, changing the layout
     */
    @Subscribe public void pageCompleted(PageCompletedEvent pageCompletedEvent) {
        if (multi) {
            if (masterRole) {
                if (currentGameState.lastPage()) {
                    gameStat.setEndDate(new Date());
                    storeSendStats();
                }
                myTurn(true, false);
                showPageCompleted();
            } else {
                myTurn(false, false);
            }
        } else {
            if (currentGameState.lastPage()) {
                gameStat.setEndDate(new Date());
                storeSendStats();
            }
            showPageCompleted();
        }
    }

    /**
     * React to a NextPageEvent, opening a new one or ending the game
     */
    @Subscribe public void nextPageEvent(NextPageEvent nextPageEvent) {
        if (currentGameState.lastPage()) {
            showEndDialog();
            sendMessage(BluetoothMessageHeader.GAME_END);
        } else {
            nextPage(constructPage());
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
            timeoutHandler.removeCallbacksAndMessages(null);
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
            BUS.post(new WordSelectedEvent(word, true, currentGameState.getWordsAvailable().contains(word)));
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
            WordStat wordStat = new WordStat(new Date(), selectedWord.getLemma(), currentGameState.getPageNumber(), null);
            wordStats.add(wordStat);
            // Play correct sound
            soundPool.play(correctSound, 1f, 1f, 0, 0, 1f);
            // Update number of words to found
            currentGameState.wordFound(selectedWord);
            if (multi) {
                // If was my turn I found the word and now is the other turn
                if (myTurn) {
                    sendMessage(BluetoothMessageHeader.WORD_FOUND + gson.toJson(selectedWord, Word.class));
                    myTurn(false, true);
                } else { // If was not my turn the other found it and now is my turn
                    myTurn(true, false);
                }

            }
            // Check if page is completed
            if (currentGameState.allWordsFound()) {
                BUS.post(new PageCompletedEvent(currentGameState.getPageNumber()));
            }

        } else if (wordSelectedEvent.isCorrect() && !wordSelectedEvent.isNew()) {
            soundPool.play(sameSound, 1f, 1f, 0, 0, 1f);

            if (multi && myTurn) {
                sendMessage(BluetoothMessageHeader.SIMPLE_TURN_PASS);
                // Pass the turn
                myTurn(false, false);
            }
        } else {
            soundPool.play(wrongSound, 1f, 1f, 0, 0, 1f);

            if (multi && myTurn) {
                sendMessage(BluetoothMessageHeader.SIMPLE_TURN_PASS);
                // Pass the turn
                myTurn(false, true);
            }
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

    /**
     * React to a ConnectionStateChangeEvent
     */
    @Subscribe public void connectionStateChangeEvent(ConnectionStateChangeEvent connectionStateChangeEvent) {
        switch (connectionStateChangeEvent.getNewState()) {
            case BluetoothService.STATE_CONNECTED:
                if (null != mConnectedDeviceName) {
                    connTextView.setText("(" + String.valueOf(masterRole) + ") " + getString(R.string.bluetooth_connected) + " a " + mConnectedDeviceName);
                } else {
                    connTextView.setText(R.string.bluetooth_connected);
                }

                // If it is master initialize the game, if it is slave do nothing and wait
                if (masterRole) {
                    startGame();
                }
                break;
            case BluetoothService.STATE_CONNECTING:
                connTextView.setText(R.string.bluetooth_connecting);
                break;
            case BluetoothService.STATE_LOST:
                connTextView.setText(R.string.bluetooth_listening);
                Toast.makeText(this, "Connessione persa", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothService.STATE_FAILED:
                connTextView.setText(R.string.bluetooth_listening);
                Toast.makeText(this, "Connessione fallita", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothService.STATE_LISTEN:
                connTextView.setText(R.string.bluetooth_listening);
                break;
            case BluetoothService.STATE_NONE:
                connTextView.setText(R.string.bluetooth_conn_def);
                break;
        }
    }

    /**
     * React to a MessageWriteEvent
     */
    @Subscribe public void messageWriteEvent(MessageWriteEvent messageWriteEvent) {
    }

    /**
     * React to a MessageWriteEvent
     */
    @Subscribe public void messageReadEvent(MessageReadEvent messageReadEvent) {
        byte[] readBuf = (byte[]) messageReadEvent.getBuffer();
        // construct a string from the valid bytes in the buffer
        String readMessage = new String(readBuf, 0, messageReadEvent.getBytes());

        // New page info from the master
        if (readMessage.startsWith(BluetoothMessageHeader.PAGE_INFO) && !masterRole) {
            if(null != ed) {
                ed.dismiss();
            }
            myTurn(true, false);
            String pageInfoJson = readMessage.replace(BluetoothMessageHeader.PAGE_INFO, "");
            GameState gameState = gson.fromJson(pageInfoJson, GameState.class);
            nextPage(gameState);
        }

        // Simple turn pass
        if (readMessage.startsWith(BluetoothMessageHeader.SIMPLE_TURN_PASS)) {
            myTurn(true, false);
        }

        // Word found by the other player (the turn is not changed , it will be in wordSelectedEvent)
        if (readMessage.startsWith(BluetoothMessageHeader.WORD_FOUND)) {
            String wordFoundJson = readMessage.replace(BluetoothMessageHeader.WORD_FOUND, "");
            Word wordFound = gson.fromJson(wordFoundJson, Word.class);
            BUS.post(new WordConfirmedEvent(wordFound.getLemma()));
        }

        // Game finishd
        if (readMessage.startsWith(BluetoothMessageHeader.GAME_END)) {
            myTurn(true,false);
            showEndDialog();
        }
    }

    /**
     * React to a ConnectedDeviceNameEvent
     */
    @Subscribe public void connectedDeviceNameEvent(ConnectedDeviceNameEvent connectedDeviceNameEvent) {
        mConnectedDeviceName = connectedDeviceNameEvent.getName();
        connTextView.setText("Connesso a " + mConnectedDeviceName);
        Toast.makeText(this, "Connesso a " + connectedDeviceNameEvent.getName(), Toast.LENGTH_SHORT).show();
        newGameAlertDialog.dismiss();
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
        startActivityForResult(checkTTSIntent, REQUEST_TTS_CHECK);
    }


    /* ----------------------------- Bluetooth Methods ----------------------------- */


    /**
     * Initialize the bluetooth connection and return if the Bluetooth is supported
     */
    private boolean initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * If Bluetooth is not enabled, requests to enable it (the bluetooth enable request is async, look for {@link MainActivity#onActivityResult}
     *
     * @return whether it was enabled
     */
    private boolean enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Starts the service that handles bluetooth connections
     */
    private void startBluetoothService() {
        if (mBluetoothService == null) {
            mBluetoothService = new BluetoothService(this, BUS);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Log.e(TAG, String.valueOf(mBluetoothService.getState()));
            Toast.makeText(this, R.string.error_connection_msg, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device);
    }

    /**
     * Start device connection activity
     */
    private void showDeviceListActivity() {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }
}