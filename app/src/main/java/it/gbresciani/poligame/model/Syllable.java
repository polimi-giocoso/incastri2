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
}
