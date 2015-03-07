package it.gbresciani.poligame.events;

public class SyllableSelectedEvent {

    private String syllable;

    public SyllableSelectedEvent(String syllable) {
        this.syllable = syllable;
    }

    public String getSyllable() {
        return syllable;
    }
}
