package it.gbresciani.legodigitalsonoro.helper;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import it.gbresciani.legodigitalsonoro.model.Syllable;
import it.gbresciani.legodigitalsonoro.model.Word;

public class GameState {

    public final static String PAGE_NUM = "pageNumber";
    public final static String PAGES = "pages";
    public final static String PAGE_WORDS_AVAIL = "pageWordsAvailable";
    public final static String PAGE_WORDS_FOUND = "pageWordsFound";
    public final static String PAGE_WORDS_TO_FIND_NUM = "pageWordsToFindNum";
    public final static String PAGE_SYLLABLES = "pageSyllables";
    public final static String CURRENT_PLAYER = "currentPlayer";
    public final static String CURRENT_PLAYER_DEVICE_ID = "currentPlayerDeviceId";

    @SerializedName(PAGE_NUM)
    private int pageNumber;

    @SerializedName(PAGES)
    private int pages;

    @SerializedName(PAGE_WORDS_TO_FIND_NUM)
    private int pageWordsToFindNum;

    @SerializedName(PAGE_WORDS_AVAIL)
    private ArrayList<Word> wordsAvailable;

    @SerializedName(PAGE_WORDS_FOUND)
    private ArrayList<Word> wordsFound = new ArrayList<Word>();

    @SerializedName(PAGE_SYLLABLES)
    private ArrayList<Syllable> syllables;

    @SerializedName(CURRENT_PLAYER)
    private String currentPlayer;

    @SerializedName(CURRENT_PLAYER_DEVICE_ID)
    private String currentPlayerDeviceId;

    public GameState() {
    }

    public GameState(int pageNumber, int pages, int pageWordsToFindNum, ArrayList<Word> wordsAvailable, ArrayList<Word> wordsFound, ArrayList<Syllable> syllables, String currentPlayer, String currentPlayerDeviceId) {
        this.pageNumber = pageNumber;
        this.pages = pages;
        this.pageWordsToFindNum = pageWordsToFindNum;
        this.wordsAvailable = wordsAvailable;
        this.wordsFound = wordsFound;
        this.syllables = syllables;
        this.currentPlayer = currentPlayer;
        this.currentPlayerDeviceId = currentPlayerDeviceId;
    }

    public GameState(GameState gameState) {
        this.pageNumber = gameState.pageNumber;
        this.pages = gameState.pages;
        this.pageWordsToFindNum = gameState.pageWordsToFindNum;
        this.wordsAvailable = new ArrayList<>(gameState.wordsAvailable);
        this.wordsFound = new ArrayList<>(gameState.wordsFound);
        this.syllables = new ArrayList<>(gameState.syllables);
        this.currentPlayer = gameState.currentPlayer;
        this.currentPlayerDeviceId = gameState.currentPlayerDeviceId;
    }


    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
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

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ArrayList<Word> getWordsFound() {
        return wordsFound;
    }

    public void setWordsFound(ArrayList<Word> wordsFound) {
        this.wordsFound = wordsFound;
    }

    public String getCurrentPlayerDeviceId() {
        return currentPlayerDeviceId;
    }

    public void setCurrentPlayerDeviceId(String currentPlayerDeviceId) {
        this.currentPlayerDeviceId = currentPlayerDeviceId;
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
     *
     * @param word The found word
     */
    public void wordFound(Word word) {
        wordsAvailable.remove(word);
        wordsFound.add(word);
        this.pageWordsToFindNum--;
    }

    /**
     * Increments the page number and clears the word arrayLists
     */
    public void nextPage() {
        wordsAvailable.clear();
        wordsFound.clear();
        pageNumber++;
    }

    public boolean lastPage() {
        return pages == pageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState)) return false;

        GameState gameState = (GameState) o;

        if (pageNumber != gameState.pageNumber) return false;
        if (pageWordsToFindNum != gameState.pageWordsToFindNum) return false;
        if (pages != gameState.pages) return false;
        if (currentPlayer != null ? !currentPlayer.equals(gameState.currentPlayer) : gameState.currentPlayer != null)
            return false;
        if (currentPlayerDeviceId != null ? !currentPlayerDeviceId.equals(gameState.currentPlayerDeviceId) : gameState.currentPlayerDeviceId != null)
            return false;
        if (syllables != null ? !syllables.equals(gameState.syllables) : gameState.syllables != null)
            return false;
        if (wordsAvailable != null ? !wordsAvailable.equals(gameState.wordsAvailable) : gameState.wordsAvailable != null)
            return false;
        if (wordsFound != null ? !wordsFound.equals(gameState.wordsFound) : gameState.wordsFound != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pageNumber;
        result = 31 * result + pages;
        result = 31 * result + pageWordsToFindNum;
        result = 31 * result + (wordsAvailable != null ? wordsAvailable.hashCode() : 0);
        result = 31 * result + (wordsFound != null ? wordsFound.hashCode() : 0);
        result = 31 * result + (syllables != null ? syllables.hashCode() : 0);
        result = 31 * result + (currentPlayer != null ? currentPlayer.hashCode() : 0);
        result = 31 * result + (currentPlayerDeviceId != null ? currentPlayerDeviceId.hashCode() : 0);
        return result;
    }
}
