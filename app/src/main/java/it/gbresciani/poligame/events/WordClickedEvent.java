package it.gbresciani.poligame.events;

import it.gbresciani.poligame.model.Word;

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
