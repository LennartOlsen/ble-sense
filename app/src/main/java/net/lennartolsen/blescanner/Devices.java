package net.lennartolsen.blescanner;

/**
 * Created by gkevi on 3/23/2017.
 */

public class Devices {
    private String deviceName;
    private String roomNumber;

    public Devices(){

    }
    public Devices(String _deviceName, String _roomNumber){
        this.deviceName = _deviceName;
        this.roomNumber = _roomNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }


}
