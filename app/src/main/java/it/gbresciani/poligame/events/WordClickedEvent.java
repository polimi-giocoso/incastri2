package it.gbresciani.poligame.events;

import it.gbresciani.poligame.model.Word;

/**
 * Created by bear on 15/03/15.
 */
public class WordClickedEvent {

    private Word word;

    public WordClickedEvent(Word word) {
        this.word = word;
    }

    public Word getWord() {
        return word;
    }
}
