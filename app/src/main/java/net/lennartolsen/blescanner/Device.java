package net.lennartolsen.blescanner;

/**
 * Created by gkevi on 3/23/2017.
 */

public class Device {
    private String deviceName;
    private String roomNumber;
    private int signalStrength;

    public int getBirth() {
        return birth;
    }

    public void setBirth(int birth) {
        this.birth = birth;
    }

    private int birth;

    public Device(String _deviceName, String _roomNumber){
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

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }


}
