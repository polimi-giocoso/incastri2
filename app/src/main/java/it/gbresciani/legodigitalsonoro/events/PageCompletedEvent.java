package it.gbresciani.legodigitalsonoro.events;


public class PageCompletedEvent {
    private int pageNumber;

    public PageCompletedEvent(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
