package it.gbresciani.poligame.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Syllable extends SugarRecord<Syllable> implements Parcelable {

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


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.val);
        dest.writeValue(this.id);
    }

    private Syllable(Parcel in) {
        this.val = in.readString();
        this.id = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Parcelable.Creator<Syllable> CREATOR = new Parcelable.Creator<Syllable>() {
        public Syllable createFromParcel(Parcel source) {
            return new Syllable(source);
        }

        public Syllable[] newArray(int size) {
            return new Syllable[size];
        }
    };
}
