package it.gbresciani.legodigitalsonoro.events;

import it.gbresciani.legodigitalsonoro.model.Word;

/**
 * Created by bear on 25/03/15.
 */
public class OtherFoundWordEvent {

    private Word newWord;

    public OtherFoundWordEvent(Word newWord) {
        this.newWord = newWord;
    }

    public Word getNewWord() {
        return newWord;
    }
}
