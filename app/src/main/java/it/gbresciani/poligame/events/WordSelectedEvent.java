package it.gbresciani.poligame.events;

/**
 * Machine State Event representig a word selected
 */
public class WordSelectedEvent {

    private String word;
    private boolean correct;

    public WordSelectedEvent(String word, boolean correct) {
        this.correct = correct;
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public boolean isCorrect() {
        return correct;
    }
}
