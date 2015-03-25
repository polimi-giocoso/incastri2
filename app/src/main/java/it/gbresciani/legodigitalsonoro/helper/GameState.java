package it.gbresciani.legodigitalsonoro.helper;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import it.gbresciani.legodigitalsonoro.model.Syllable;
import it.gbresciani.legodigitalsonoro.model.Word;

public class GameState {

    public final static String PAGE_NUM = "pageNumber";
    public final static String PAGES_LEFT = "pagesLeft";
    public final static String PAGE_WORDS_AVAIL = "pageWordsAvailable";
    public final static String PAGE_WORDS_TO_FIND_NUM = "pageWordsToFindNum";
    public final static String PAGE_SYLLABLES = "pageSyllables";

    @SerializedName(PAGE_NUM)
    private int pageNumber;

    @SerializedName(PAGES_LEFT)
    private int pagesLeft;

    @SerializedName(PAGE_WORDS_TO_FIND_NUM)
    private int pageWordsToFindNum;

    @SerializedName(PAGE_WORDS_AVAIL)
    private ArrayList<Word> wordsAvailable;

    @SerializedName(PAGE_SYLLABLES)
    private ArrayList<Syllable> syllables;

    public GameState() {
    }

    public GameState(int pageNumber, int pagesLeft, int pageWordsToFindNum, ArrayList<Word> wordsAvailable, ArrayList<Syllable> syllables) {
        this.pageNumber = pageNumber;
        this.pagesLeft = pagesLeft;
        this.pageWordsToFindNum = pageWordsToFindNum;
        this.wordsAvailable = wordsAvailable;
        this.syllables = syllables;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPagesLeft() {
        return pagesLeft;
    }

    public void setPagesLeft(int pagesLeft) {
        this.pagesLeft = pagesLeft;
    }

    public int getPageWordsToFindNum() {
        return pageWordsToFindNum;
    }

    public void setPageWordsToFindNum(int pageWordsToFindNum) {
        this.pageWordsToFindNum = pageWordsToFindNum;
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

    /**
     * Whether all words are found
     *
     * @return true -> all words found, false -> remaining words to find
     */
    public boolean allWordsFound() {
        return pageWordsToFindNum == 0;
    }

    /**
     * Removes word from the available words and decrements the number of words to find
     * @param word The found word
     */
    public void wordFound(Word word){
        wordsAvailable.remove(word);
        this.pageWordsToFindNum--;
    }

    public void nextPageNumber(){
        pageNumber++;
    }

    public boolean lastPage(){
        return pagesLeft == 0;
    }
}
