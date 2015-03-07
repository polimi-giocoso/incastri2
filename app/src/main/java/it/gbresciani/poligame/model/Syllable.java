package it.gbresciani.poligame.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Syllable extends SugarRecord<Syllable> {

    public final static String VALUE = "val";

    @SerializedName(VALUE)
    private String val;

    public Syllable() {
    }

    public Syllable(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Syllable)) return false;

        Syllable syllable = (Syllable) o;

        if (val != null ? !val.equals(syllable.val) : syllable.val != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return val != null ? val.hashCode() : 0;
    }
}
