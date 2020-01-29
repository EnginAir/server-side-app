package Models;

public class ADSBData {

    String tailNumber;
    LatLong location;
    Float altitude;
    Float speed;


    public ADSBData(String tailNumber, LatLong location, Float altitude, Float speed){
        this.tailNumber = tailNumber;
        this. location = location;
        this.altitude = altitude;
        this.speed = speed;
    }

}
