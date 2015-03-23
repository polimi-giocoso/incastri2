package it.gbresciani.legodigitalsonoro.events;

import it.gbresciani.legodigitalsonoro.model.Syllable;

public class SyllableSelectedEvent {

    private Syllable syllable;

    public SyllableSelectedEvent(Syllable syllable) {
        this.syllable = syllable;
    }

    public Syllable getSyllable() {
        return syllable;
    }
}
