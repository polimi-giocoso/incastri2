package it.gbresciani.poligame.events;

import it.gbresciani.poligame.model.Word;

/**
 * Machine State Event representig a word selected
 */
public class WordSelectedEvent {

    private Word word;
    private boolean correct;
    private boolean itsNew;

    public WordSelectedEvent(Word word, boolean correct, boolean itsNew) {
        this.word = word;
        this.correct = correct;
        this.itsNew = itsNew;
    }

    public Word getWord() {
        return word;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean isNew() {
        return itsNew;
    }
}
