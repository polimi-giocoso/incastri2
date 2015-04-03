package it.gbresciani.legodigitalsonoro.events;

/**
 * Created by bear on 03/04/15.
 */
public class DeviceSelectedEvent {

    private String deviceId;

    public DeviceSelectedEvent(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
