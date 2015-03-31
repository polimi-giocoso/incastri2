package it.gbresciani.legodigitalsonoro.events;

import it.gbresciani.legodigitalsonoro.helper.GameState;

/**
 * Created by bear on 26/03/15.
 */
public class StateUpdatedEvent {

    private GameState newGameState;
    private GameState oldGameState;

    public StateUpdatedEvent(GameState newGameState, GameState oldGameState) {
        this.newGameState = newGameState;
        this.oldGameState = oldGameState;
    }

    public GameState getNewGameState() {
        return newGameState;
    }

    public void setNewGameState(GameState newGameState) {
        this.newGameState = newGameState;
    }

    public GameState getOldGameState() {
        return oldGameState;
    }

    public void setOldGameState(GameState oldGameState) {
        this.oldGameState = oldGameState;
    }
}


