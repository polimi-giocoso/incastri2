package it.gbresciani.legodigitalsonoro.model;

import com.orm.SugarRecord;

import java.util.Date;


public class WordStat extends SugarRecord<WordStat> {

    private Date foundDate;
    private String word;
    private int pageNumber;
    private GameStat gameStat;
    private String deviceId;

    public WordStat() {
    }

    public WordStat(Date foundDate, String word, int pageNumber, GameStat gameStat, String device) {
        this.foundDate = foundDate;
        this.word = word;
        this.pageNumber = pageNumber;
        this.gameStat = gameStat;
        this.deviceId = device;
    }

    public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public GameStat getGameStat() {
        return gameStat;
    }

    public void setGameStat(GameStat gameStat) {
        this.gameStat = gameStat;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
