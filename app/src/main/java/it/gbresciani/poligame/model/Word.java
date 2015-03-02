package it.gbresciani.poligame.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Word extends SugarRecord<Word> {

    public final static String LEMMA = "lemma";
    public final static String SYLLABLE_1 = "syllable1";
    public final static String SYLLABLE_2 = "syllable2";

    @SerializedName(LEMMA)
    private String lemma;

    @SerializedName(SYLLABLE_1)
    private String syllable1;

    @SerializedName(SYLLABLE_2)
    private String syllable2;


    public Word() {
    }

    public Word(String lemma, String syllable1, String syllable2, String inverSyllable1, String inverSyllable2) {
        this.lemma = lemma;
        this.syllable1 = syllable1;
        this.syllable2 = syllable2;
    }
}
