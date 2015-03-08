package it.gbresciani.poligame.events;

/**
 * Created by bear on 08/03/15.
 */
public class WordConfirmedEvent {

    private String wordConfirmed;

    public WordConfirmedEvent(String wordConfirmed) {
        this.wordConfirmed = wordConfirmed;
    }

    public String getWordConfirmed() {
        return wordConfirmed;
    }
}
