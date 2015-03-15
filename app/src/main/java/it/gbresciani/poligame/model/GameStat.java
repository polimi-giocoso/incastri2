package it.gbresciani.poligame.model;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Created by bear on 15/03/15.
 */
public class GameStat extends SugarRecord<GameStat> {

    private Date startDate;
    private Date endDate;

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
}
