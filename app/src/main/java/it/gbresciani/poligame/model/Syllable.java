package it.gbresciani.poligame.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Syllable extends SugarRecord<Syllable> implements Parcelable {

    public final static String VALUE = "val";
    public final static String COLOR = "color";

    @SerializedName(VALUE)
    private String val;

    @SerializedName(COLOR)
    private String color;

    public Syllable() {
    }

    public Syllable(String val, String color) {
        this.val = val;
        this.color = color;
    }

    public String getVal() {
        return val;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Syllable)) return false;

        Syllable syllable = (Syllable) o;

        if (color != null ? !color.equals(syllable.color) : syllable.color != null) return false;
        if (val != null ? !val.equals(syllable.val) : syllable.val != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = val != null ? val.hashCode() : 0;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.val);
        dest.writeString(this.color);
        dest.writeValue(this.id);
    }

    private Syllable(Parcel in) {
        this.val = in.readString();
        this.color = in.readString();
        this.id = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<Syllable> CREATOR = new Creator<Syllable>() {
        public Syllable createFromParcel(Parcel source) {
            return new Syllable(source);
        }

        public Syllable[] newArray(int size) {
            return new Syllable[size];
        }
    };
}
