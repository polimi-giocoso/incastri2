package it.gbresciani.poligame.events;

import it.gbresciani.poligame.model.Syllable;

public class SyllableSelectedEvent {

    private Syllable syllable;

    public SyllableSelectedEvent(Syllable syllable) {
        this.syllable = syllable;
    }

    public Syllable getSyllable() {
        return syllable;
    }
}
