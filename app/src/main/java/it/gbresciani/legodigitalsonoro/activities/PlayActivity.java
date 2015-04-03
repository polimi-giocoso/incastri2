package it.gbresciani.legodigitalsonoro.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.events.ConnectedDeviceNameEvent;
import it.gbresciani.legodigitalsonoro.events.ConnectionStateChangeEvent;
import it.gbresciani.legodigitalsonoro.events.DeviceSelectedEvent;
import it.gbresciani.legodigitalsonoro.events.ExitEvent;
import it.gbresciani.legodigitalsonoro.events.MessageReadEvent;
import it.gbresciani.legodigitalsonoro.events.MessageWriteEvent;
import it.gbresciani.legodigitalsonoro.events.NextPageEvent;
import it.gbresciani.legodigitalsonoro.events.PageCompletedEvent;
import it.gbresciani.legodigitalsonoro.events.RepeatEvent;
import it.gbresciani.legodigitalsonoro.events.StateUpdatedEvent;
import it.gbresciani.legodigitalsonoro.events.SyllableSelectedEvent;
import it.gbresciani.legodigitalsonoro.events.WordClickedEvent;
import it.gbresciani.legodigitalsonoro.events.WordDismissedEvent;
import it.gbresciani.legodigitalsonoro.events.WordSelectedEvent;
import it.gbresciani.legodigitalsonoro.fragments.EndGameDialogFragment;
import it.gbresciani.legodigitalsonoro.fragments.MultiSetupDialogFragment;
import it.gbresciani.legodigitalsonoro.fragments.PageCompletedFragment;
import it.gbresciani.legodigitalsonoro.fragments.SyllablesFragment;
import it.gbresciani.legodigitalsonoro.fragments.WaitDialogFragment;
import it.gbresciani.legodigitalsonoro.fragments.WordConfirmDialogFragment;
import it.gbresciani.legodigitalsonoro.fragments.WordsFragment;
import it.gbresciani.legodigitalsonoro.helper.Constants;
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

    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

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
    private String role = SLAVE;

    // Helpers
    private Handler timeoutHandler;
    private Bus BUS;
    private SoundPool soundPool;
    private Gson gson;
    private String mDeviceId;
    private String otherDeviceId;

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

    // UI
    @InjectView(R.id.game_loading_progress_bar) ProgressBar progressBar;
    private MultiSetupDialogFragment newGameAlertDialog;
    private WaitDialogFragment waitDialog;
    private EndGameDialogFragment ed;


    /* ----------------------------- Activity Lifecycle Methods ----------------------------- */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ButterKnife.inject(this);
        BUS = BusProvider.getInstance();
        gson = new Gson();

        mDeviceId = BluetoothAdapter.getDefaultAdapter().getAddress();

        loadPref();
        loadSound();
        checkTTS();

        // Single or Multi player (default single)
        Intent playIntent = getIntent();
        String action = playIntent.getAction();
        switch (action) {
            case MODE_SINGLE_PLAYER:
                multi = false;
                timeoutHandler = new Handler();
                startGame();
                break;
            case MODE_MULTI_PLAYER:
                multi = true;
                timeoutHandler = new Handler();
                // If Bluetooth is supported and enabled show multi layout
                if (initBluetooth() && enableBluetooth()) {
                    setupMultiPlayer();
                }
                break;
            default:
                multi = false;
                timeoutHandler = new Handler();
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

        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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
     * Start the game creating the statistics object
     */
    private void startGame() {
        gameStat = new GameStat();
        gameStat.setStartDate(new Date());
        gameStat.setDeviceId1(mDeviceId);
        if (multi) {
            gameStat.setDeviceId2(otherDeviceId);
        }
        startPage(constructPage());
    }

    /**
     * Restart the game resetting the statistics objects
     */
    private void restartGame() {
        gameStat = new GameStat();
        wordStats = new ArrayList<>();
        gameStat.setStartDate(new Date());
        gameStat.setDeviceId1(mDeviceId);
        if (multi) {
            gameStat.setDeviceId2(otherDeviceId);
        }
        currentGameState = null;
        syllableYetSelected = "";
        backPressedCount = 0;
        startPage(constructPage());
    }

    /**
     * Construct all the parameters needed by a new page
     * In multiplayer it should only called by MASTER
     */
    private GameState constructPage() {

        GameState newGameState;

        // If first page in the game -> create new game state and set page number to 1
        if (currentGameState == null) {
            newGameState = new GameState();
            newGameState.setPageNumber(1);
        } else {
            newGameState = currentGameState;
            newGameState.nextPage();
        }

        newGameState.setPages(noPages);

        // Determine words and syllables for the page
        newGameState.setSyllables(Helper.chooseSyllables(noSyllables));
        newGameState.setWordsAvailable(Helper.permuteSyllablesInWords(newGameState.getSyllables(), 2));
        newGameState.setPageWordsToFindNum(newGameState.getWordsAvailable().size() <= 4 ? newGameState.getWordsAvailable().size() : 4);

        // If in multi the current player is also set to SLAVE
        if (multi) {
            newGameState.setCurrentPlayer(SLAVE);
            newGameState.setCurrentPlayerDeviceId(isMaster() ? otherDeviceId : mDeviceId);
        }
        return newGameState;
    }

    /**
     * Initialize a page, adding the two fragments and passing them the calculated syllables and words in gameState
     */
    private void startPage(GameState gameState) {

        currentGameState = gameState;

        // If in multi send the state to the SLAVE
        if (multi && isMaster()) {
            sendAndUpdateState(currentGameState);
        } else {
            updateState(currentGameState);
        }

        progressBar.setVisibility(View.INVISIBLE);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        WordsFragment wordsFragment = WordsFragment.newInstance(gameState.getWordsAvailable());
        SyllablesFragment syllablesFragment = SyllablesFragment.newInstance(gameState.getSyllables());

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
     * Make connection text view visible and start all Multi Player process before starting the game
     */
    private void setupMultiPlayer() {
        progressBar.setVisibility(View.VISIBLE);
        startBluetoothService();
        showMultiPlayerDialog();
    }

    private void sendSimpleTurnPass() {
        sendMessage(Constants.SIMPLE_TURN_PASS);
    }

    private void sendNewWordFound(Word word) {
        sendMessage(Constants.WORD_FOUND + gson.toJson(word, Word.class));
    }

    private void sendAndUpdateState(GameState gameState) {
        sendMessage(Constants.GAME_STATE + gson.toJson(gameState, GameState.class));
        updateState(gameState);
    }

    public GameState getGameState() {
        return currentGameState;
    }

    private void updateState(GameState gameState) {

        // If there is no currentGameState or the page number in the new state is different from the current start a new page with the new state
        if (currentGameState == null || currentGameState.getPageNumber() != gameState.getPageNumber()) {
            startPage(gameState);
        }
        // Check if page is completed
        if (gameState.allWordsFound()) {
            if (multi) {
                // If MASTER found last word keep the control
                gameState.setCurrentPlayer(MASTER);
                gameState.setCurrentPlayerDeviceId(isMaster() ? mDeviceId : otherDeviceId);
                if (isMaster()) {
                    BUS.post(new PageCompletedEvent(gameState.getPageNumber()));
                }
            } else {
                BUS.post(new PageCompletedEvent(gameState.getPageNumber()));
            }
        }
        // Show dialog if it is not my turn
        if (multi) {
            showWaitDialog(!role.equals(gameState.getCurrentPlayer()));
        }
        // Check if there are new words and eventually store stats
        for (Word word : Helper.getNewWordInState(gameState, currentGameState)) {
            WordStat wordStat = new WordStat(new Date(), word.getLemma(), currentGameState.getPageNumber(), null, currentGameState.getCurrentPlayerDeviceId());
            wordStats.add(wordStat);
        }

        BUS.post(new StateUpdatedEvent(gameState, currentGameState));
        // Set the new gameState as current
        currentGameState = new GameState(gameState);
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
        if (multi && !isMaster()) {
            ed = EndGameDialogFragment.newInstance(true);
        } else {
            ed = EndGameDialogFragment.newInstance(false);
        }
        ed.show(ft, "endDialog");
    }

    private void showMultiPlayerDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        newGameAlertDialog = MultiSetupDialogFragment.newInstance();
        newGameAlertDialog.show(ft, "multi");

    }

    private void showWaitDialog(boolean show) {
        if (waitDialog != null) {
            waitDialog.dismiss();
        }
        if (show) {
            (new Handler()).postDelayed(new Runnable() {
                @Override public void run() {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    waitDialog = WaitDialogFragment.newInstance();
                    waitDialog.show(ft, "waitDialog");
                }
            }, WordConfirmDialogFragment.WORD_DIALOG_TIMEOUT);
        }
    }

    /* ----------------------------- Bus Events Methods ----------------------------- */


    /**
     * React to a NextPageEvent, opening a new one or ending the game
     */
    @Subscribe public void nextPageEvent(NextPageEvent nextPageEvent) {
        if (currentGameState.lastPage()) {
            showEndDialog();
        } else {
            startPage(constructPage());
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
     * React to a WordSelectedEvent
     */
    @Subscribe public void wordSelected(WordSelectedEvent wordSelectedEvent) {
        timeoutHandler.removeCallbacksAndMessages(null);
        Word selectedWord = wordSelectedEvent.getWord();
        if (wordSelectedEvent.isCorrect() && wordSelectedEvent.isNew()) {
            // Play correct sound
            soundPool.play(correctSound, 1f, 1f, 0, 0, 1f);
            // New Game State
            GameState newGameState = new GameState(currentGameState);
            // Update number of words to found
            if (multi) {
                // If MASTER update the status and send to the SLAVE
                if (isMaster()) {
                    newGameState.setCurrentPlayer(SLAVE);
                    newGameState.setCurrentPlayerDeviceId(isMaster() ? otherDeviceId : mDeviceId);
                    newGameState.wordFound(selectedWord);
                    sendAndUpdateState(newGameState);
                } else {
                    sendNewWordFound(selectedWord);
                }
            } else {
                newGameState.wordFound(selectedWord);
                updateState(newGameState);
            }
        } else if (wordSelectedEvent.isCorrect() && !wordSelectedEvent.isNew()) {
            soundPool.play(sameSound, 1f, 1f, 0, 0, 1f);
            if (multi) {
                // If MASTER change current player and send new state
                if (isMaster()) {
                    GameState newGameState = new GameState(currentGameState);
                    newGameState.setCurrentPlayer(SLAVE);
                    newGameState.setCurrentPlayerDeviceId(isMaster() ? otherDeviceId : mDeviceId);
                    sendAndUpdateState(newGameState);
                } else { // If SLAVE send simple turn pass to MASTER and wait for state change
                    sendSimpleTurnPass();
                }
            }
        } else {
            soundPool.play(wrongSound, 1f, 1f, 0, 0, 1f);
            if (multi) {
                // If MASTER change current player and send new state
                if (isMaster()) {
                    GameState newGameState = new GameState(currentGameState);
                    newGameState.setCurrentPlayer(SLAVE);
                    newGameState.setCurrentPlayerDeviceId(isMaster() ? otherDeviceId : mDeviceId);
                    sendAndUpdateState(newGameState);
                } else { // If SLAVE send simple turn pass to MASTER and wait for state change
                    sendSimpleTurnPass();
                }
            }
        }
    }

    /**
     * React to a PageCompletedEvent, changing the layout
     */
    @Subscribe public void pageCompleted(PageCompletedEvent pageCompletedEvent) {
        if (currentGameState.lastPage()) {
            gameStat.setEndDate(new Date());
            storeSendStats();
        }
        showPageCompleted();
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
                Toast.makeText(this, "Connessione stabilita", Toast.LENGTH_SHORT).show();
                // If it is master initialize the game, if it is slave do nothing and wait
                if (isMaster()) {
                    startGame();
                }
                break;
            case BluetoothService.STATE_CONNECTING:
                break;
            case BluetoothService.STATE_LOST:
                Toast.makeText(this, "Connessione persa", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothService.STATE_FAILED:
                Toast.makeText(this, "Connessione fallita", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothService.STATE_LISTEN:
                break;
            case BluetoothService.STATE_NONE:
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
        if (readMessage.startsWith(Constants.GAME_STATE)) {
            // Only the SLAVE should receive this message and update its game state
            String pageInfoJson = readMessage.replace(Constants.GAME_STATE, "");
            GameState gameState = gson.fromJson(pageInfoJson, GameState.class);
            updateState(gameState);
        }

        // Simple turn pass
        if (readMessage.startsWith(Constants.SIMPLE_TURN_PASS)) {
            // Only the MASTER should receive this message, change the current player to the MASTER itself and send back to the SLAVE
            if (isMaster()) {
                GameState newGameState = new GameState(currentGameState);
                newGameState.setCurrentPlayer(MASTER);
                newGameState.setCurrentPlayerDeviceId(isMaster() ? mDeviceId : otherDeviceId);
                sendAndUpdateState(newGameState);
            }
        }

        // Word found by the other player
        if (readMessage.startsWith(Constants.WORD_FOUND)) {
            String wordFoundJson = readMessage.replace(Constants.WORD_FOUND, "");
            Word wordFound = gson.fromJson(wordFoundJson, Word.class);
            // Only the MASTER should receive this message, update the game state and send back to the SLAVE
            if (isMaster()) {
                GameState newGameState = new GameState(currentGameState);
                newGameState.wordFound(wordFound);
                newGameState.setCurrentPlayer(MASTER);
                newGameState.setCurrentPlayerDeviceId(isMaster() ? mDeviceId : otherDeviceId);
                sendAndUpdateState(newGameState);
            }
        }

        // Game finishd
        if (readMessage.startsWith(Constants.GAME_END)) {
            showEndDialog();
        }
    }

    /**
     * React to a ConnectedDeviceNameEvent
     */
    @Subscribe public void connectedDeviceNameEvent(ConnectedDeviceNameEvent connectedDeviceNameEvent) {
        Toast.makeText(this, "Connesso a " + connectedDeviceNameEvent.getName(), Toast.LENGTH_SHORT).show();
        newGameAlertDialog.dismiss();
        otherDeviceId = connectedDeviceNameEvent.getDeviceId();
    }

    /**
     * React to a ConnectedDeviceNameEvent
     */
    @Subscribe public void deviceSelectedEvent(DeviceSelectedEvent deviceSelectedEvent) {
        connectDevice(deviceSelectedEvent.getDeviceId());
        role = MASTER;
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
     */
    private void connectDevice(String deviceAddress) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        // Attempt to connect to the device
        mBluetoothService.connect(device);
    }

    public boolean isMaster() {
        return MASTER.equals(role);
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
                if (lang.equals(Locale.ENGLISH)) {
                    mTTS.speak(word.getEng(), TextToSpeech.QUEUE_ADD, null, word.getEng());
                }
                if (lang.equals(Locale.ITALIAN)) {
                    mTTS.speak(word.getLemma(), TextToSpeech.QUEUE_ADD, null, word.getLemma());
                }
            } else {
                if (lang.equals(Locale.ENGLISH)) {
                    mTTS.speak(word.getEng(), TextToSpeech.QUEUE_ADD, null);
                }
                if (lang.equals(Locale.ITALIAN)) {
                    mTTS.speak(word.getLemma(), TextToSpeech.QUEUE_ADD, null);
                }
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

}