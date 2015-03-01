package it.gbresciani.poligame;

import com.squareup.otto.Bus;

import de.halfbit.tinymachine.StateHandler;
import de.halfbit.tinymachine.TinyMachine;
import it.gbresciani.poligame.events.EnterStateInitEvent;
import it.gbresciani.poligame.events.EnterStateSyllSelectedEvent;
import it.gbresciani.poligame.events.EnterStateWordSelectedEvent;


/*
* This class implements a finite-state machine (using TinyMachine) that handles the game logic
*/
public class GameMachine {

    private TinyMachine mGameMachine;

    // States
    public static final int STATE_INIT = 0;
    public static final int STATE_SYLL_SELECTED = 1;
    public static final int STATE_WORD_SELECTED = 2;
    public static final int STATE_END = 3;

    // Events
    private static final String EVENT_SYLL_TAP = "event_syll_tap";
    private static final String EVENT_SYLL_TIMEOUT = "event_syll_timeout";
    private static final String EVENT_WORD_ERROR = "event_word_error";
    private static final String EVENT_WORD_CORRECT = "event_word_correct";

    public GameMachine(int words, Bus BUS) {
        mGameMachine = new TinyMachine(new GameHandler(words, BUS), STATE_INIT);
    }

    private TinyMachine getTM(){
        return mGameMachine;
    }

    private static class GameHandler {

        private Bus mBUS;
        private int wordsFound = 0;
        private final int wordsAvailable;

        private GameHandler(int wordsAvailable, Bus bus) {
            this.mBUS = bus;
            this.wordsAvailable = wordsAvailable;
        }

        /*
        * STATE_INIT
        * When there is no syllable selected
        * */

        @StateHandler(state = STATE_INIT)
        public void onEventStateInit(String event, TinyMachine tm) {

            switch (event) {
                case EVENT_SYLL_TAP:
                    tm.transitionTo(STATE_SYLL_SELECTED);
                    break;
            }
        }

        @StateHandler(state = STATE_INIT, type = StateHandler.Type.OnEntry)
        public void onEntryStateInit() {
            mBUS.post(new EnterStateInitEvent(wordsAvailable));
        }

        /*
        * STATE_SYLL_SELECTED
        * When a syllable is selected
        * */

        @StateHandler(state = STATE_SYLL_SELECTED)
        public void onEventStateSyllSelected(String event, TinyMachine tm) {

            switch (event) {
                case EVENT_SYLL_TAP:
                    tm.transitionTo(STATE_WORD_SELECTED);
                    break;
                case EVENT_SYLL_TIMEOUT:
                    tm.transitionTo(STATE_INIT);
                    break;
            }
        }


        @StateHandler(state = STATE_SYLL_SELECTED, type = StateHandler.Type.OnEntry)
        public void onEntryStateSyllSelected() {
            mBUS.post(new EnterStateSyllSelectedEvent());
        }

        /*
         * STATE_WORD_SELECTED
         * When a second syllable is selected, waiting for confirmation
         */

        @StateHandler(state = STATE_WORD_SELECTED)
        public void onEventStateWordSelected(String event, TinyMachine tm) {

            switch (event) {
                case EVENT_WORD_CORRECT:
                    wordsFound++;
                    if (wordsFound == wordsAvailable) {
                        tm.transitionTo(STATE_END);
                    } else {
                        tm.transitionTo(STATE_INIT);
                    }
                    break;
                case EVENT_WORD_ERROR:
                    tm.transitionTo(STATE_INIT);
                    break;
            }
        }

        @StateHandler(state = STATE_WORD_SELECTED, type = StateHandler.Type.OnEntry)
        public void onEntryStateWordSelected() {
            mBUS.post(new EnterStateWordSelectedEvent());
        }

        /*
        * STATE_END
        * When all the available words are found
        * */

        @StateHandler(state = STATE_END, type = StateHandler.Type.OnEntry)
        public void onEntryStateEnd(String event, TinyMachine tm) {
            mBUS.post(new EnterStateWordSelectedEvent());
        }
    }

}
