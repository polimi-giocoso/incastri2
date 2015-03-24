package it.gbresciani.legodigitalsonoro.events;

import java.util.Locale;

import it.gbresciani.legodigitalsonoro.model.Word;

public class WordClickedEvent {

    private Word word;
    private Locale LANG;

    public WordClickedEvent(Word word, Locale LANG) {
        this.word = word;
        this.LANG = LANG;
    }

    public Word getWord() {
        return word;
    }

    public Locale getLANG() {
        return LANG;
    }
}
