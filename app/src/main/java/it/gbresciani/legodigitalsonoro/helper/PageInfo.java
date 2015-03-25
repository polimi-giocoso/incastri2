package it.gbresciani.legodigitalsonoro.helper;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import it.gbresciani.legodigitalsonoro.model.Syllable;
import it.gbresciani.legodigitalsonoro.model.Word;

public class PageInfo {

    public final static String PAGE_NUM = "number";
    public final static String PAGE_WORDS_AVAIL = "pageWordsAvailable";
    public final static String PAGE_WORDS_TO_FIND_NUM = "pageWordsToFindNum";
    public final static String PAGE_SYLLABLES = "syllables";

    @SerializedName(PAGE_NUM)
    private int number;

    @SerializedName(PAGE_WORDS_TO_FIND_NUM)
    private int pageWordsToFindNum;

    @SerializedName(PAGE_WORDS_AVAIL)
    private ArrayList<Word> wordsAvailable;

    @SerializedName(PAGE_SYLLABLES)
    private ArrayList<Syllable> syllables;

    public PageInfo() {
    }

    public PageInfo(int number, int pageWordsToFindNum, ArrayList<Word> wordsAvailable, ArrayList<Syllable> syllables) {
        this.number = number;
        this.pageWordsToFindNum = pageWordsToFindNum;
        this.wordsAvailable = wordsAvailable;
        this.syllables = syllables;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPageWordsToFindNum() {
        return pageWordsToFindNum;
    }

    public void setPageWordsToFindNum(int pageWordsToFindNum) {
        this.pageWordsToFindNum = pageWordsToFindNum;
    }

    public void wordFound(Word word){
        wordsAvailable.remove(word);
        this.pageWordsToFindNum--;
    }

    public ArrayList<Word> getWordsAvailable() {
        return wordsAvailable;
    }

    public void setWordsAvailable(ArrayList<Word> wordsAvailable) {
        this.wordsAvailable = wordsAvailable;
    }

    public ArrayList<Syllable> getSyllables() {
        return syllables;
    }

    public void setSyllables(ArrayList<Syllable> syllables) {
        this.syllables = syllables;
    }

    public boolean allWordsFound() {
        return pageWordsToFindNum == 0;
    }
}
