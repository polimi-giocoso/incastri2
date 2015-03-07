package it.gbresciani.poligame.events;


public class PageCompletedEvent {
    private int pageNumber;

    public PageCompletedEvent(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
