package it.gbresciani.legodigitalsonoro.events;

import java.util.Locale;

public class SayTextEvent {

    private String text;
    private Locale LANG;

    public SayTextEvent(String text, Locale LANG) {
        this.text = text;
        this.LANG = LANG;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Locale getLANG() {
        return LANG;
    }

    public void setLANG(Locale LANG) {
        this.LANG = LANG;
    }
}
