package net.lennartolsen.blescanner;

/**
 * Created by gkevi on 3/23/2017.
 */

public class Devices {
    private String id;
    private String deviceName;
    private String roomNumber;

    public Devices(){

    }
    public Devices(String _id, String _roomNumber){
        this.id = _id;
        this.roomNumber = _roomNumber;
    }
    public String getId() {
        return id;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }


}
