package it.gbresciani.legodigitalsonoro.model;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class GameStat extends SugarRecord<GameStat> {

    private Date startDate;
    private Date endDate;
    private String deviceId1;
    private String deviceId2;

    public GameStat() {
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDeviceId1() {
        return deviceId1;
    }

    public void setDeviceId1(String deviceId1) {
        this.deviceId1 = deviceId1;
    }

    public String getDeviceId2() {
        return deviceId2;
    }

    public void setDeviceId2(String deviceId2) {
        this.deviceId2 = deviceId2;
    }
}
