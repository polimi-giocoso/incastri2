package it.gbresciani.legodigitalsonoro.events;

/**
 * Created by bear on 24/03/15.
 */
public class MessageReadEvent {

    private byte[] buffer;
    private int bytes;

    public MessageReadEvent(int bytes, byte[] buffer) {
        this.bytes = bytes;
        this.buffer = buffer;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getBytes() {
        return bytes;
    }
}
