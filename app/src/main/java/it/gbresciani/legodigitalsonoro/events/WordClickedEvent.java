package it.gbresciani.legodigitalsonoro.events;

import it.gbresciani.legodigitalsonoro.model.Word;

/**
 * Created by bear on 15/03/15.
 */
public class WordClickedEvent {

    public static final String ENGLISH= "english";
    public static final String ITALIAN = "italian";

    private Word word;
    private String LANG;

    public WordClickedEvent(Word word, String LANG) {
        this.word = word;
        this.LANG = LANG;
    }

    public Word getWord() {
        return word;
    }

    public String getLANG() {
        return LANG;
    }
}
