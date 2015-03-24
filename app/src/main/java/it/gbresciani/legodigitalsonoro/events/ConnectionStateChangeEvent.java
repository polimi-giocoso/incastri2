package it.gbresciani.legodigitalsonoro.events;

/**
 * Created by bear on 24/03/15.
 */
public class ConnectionStateChangeEvent {

    private int newState;

    public ConnectionStateChangeEvent(int newState) {
        this.newState = newState;
    }

    public int getNewState() {
        return newState;
    }
}