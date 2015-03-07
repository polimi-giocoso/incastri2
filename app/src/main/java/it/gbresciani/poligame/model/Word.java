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

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getSyllable1() {
        return syllable1;
    }

    public void setSyllable1(String syllable1) {
        this.syllable1 = syllable1;
    }

    public String getSyllable2() {
        return syllable2;
    }

    public void setSyllable2(String syllable2) {
        this.syllable2 = syllable2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Word)) return false;

        Word word = (Word) o;

        if (lemma != null ? !lemma.equals(word.lemma) : word.lemma != null) return false;
        if (syllable1 != null ? !syllable1.equals(word.syllable1) : word.syllable1 != null)
            return false;
        if (syllable2 != null ? !syllable2.equals(word.syllable2) : word.syllable2 != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lemma != null ? lemma.hashCode() : 0;
        result = 31 * result + (syllable1 != null ? syllable1.hashCode() : 0);
        result = 31 * result + (syllable2 != null ? syllable2.hashCode() : 0);
        return result;
    }
}
