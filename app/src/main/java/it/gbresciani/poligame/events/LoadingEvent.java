package it.gbresciani.poligame.events;

/**
 * Event representing the state of the database loading process
 */
public class LoadingEvent {

    public final static int STATE_STARTED = 0;
    public final static int STATE_FINISHED = 1;

    public int state;

    public LoadingEvent(int state) {
        this.state = state;
    }
}
