package it.gbresciani.legodigitalsonoro.events;

/**
 * Created by bear on 24/03/15.
 */
public class ConnectedDeviceNameEvent {

    private String name;
    private String deviceId;

    public ConnectedDeviceNameEvent() {
    }

    public ConnectedDeviceNameEvent(String name, String deviceId) {
        this.name = name;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
