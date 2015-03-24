package it.gbresciani.legodigitalsonoro.events;

/**
 * Created by bear on 24/03/15.
 */
public class ConnectedDeviceNameEvent {

    private String name;

    public ConnectedDeviceNameEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
