package it.gbresciani.poligame.model;

import com.orm.SugarRecord;

public class Word extends SugarRecord<Word> {
    private String lemma;
    private String syllable1;
    private String syllable2;
    private String inverSyllable1;
    private String inverSyllable2;

    public Word() {
    }

    public Word(String lemma, String syllable1, String syllable2) {
        this.lemma = lemma;
        this.syllable1 = syllable1;
        this.syllable2 = syllable2;
    }

    public Word(String lemma, String syllable1, String syllable2, String inverSyllable1, String inverSyllable2) {
        this.lemma = lemma;
        this.syllable1 = syllable1;
        this.syllable2 = syllable2;
        this.inverSyllable1 = inverSyllable1;
        this.inverSyllable2 = inverSyllable2;
    }
}
