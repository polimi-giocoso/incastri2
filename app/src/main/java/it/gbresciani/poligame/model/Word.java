package it.gbresciani.poligame.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Word extends SugarRecord<Word> implements Parcelable {

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

    public Word(String lemma, String syllable1, String syllable2) {
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


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.lemma);
        dest.writeString(this.syllable1);
        dest.writeString(this.syllable2);
        dest.writeValue(this.id);
    }

    private Word(Parcel in) {
        this.lemma = in.readString();
        this.syllable1 = in.readString();
        this.syllable2 = in.readString();
        this.id = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() {
        public Word createFromParcel(Parcel source) {
            return new Word(source);
        }

        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
}
