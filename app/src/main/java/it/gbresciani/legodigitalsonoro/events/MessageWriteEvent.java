package it.gbresciani.legodigitalsonoro.events;

/**
 * Created by bear on 24/03/15.
 */
public class MessageWriteEvent {

    private byte[] buffer;

    public MessageWriteEvent(byte[] buffer) {
        this.buffer = buffer;
    }

    public byte[] getBuffer() {
        return buffer;
    }
}
