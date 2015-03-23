package it.gbresciani.legodigitalsonoro.events;

/**
 * Bus Event representing the state of the database loading process
 */
public class ProgressChangeEvent {

    private int progress;

    public ProgressChangeEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
