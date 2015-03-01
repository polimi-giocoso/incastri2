package it.gbresciani.poligame.events;

/**
 * Bus Event representing the entering in the STATE_INIT
 */
public class EnterStateInitEvent {

    public int wordsAvailable;

    public EnterStateInitEvent(int wordsAvailable) {
        this.wordsAvailable = wordsAvailable;
    }
}
