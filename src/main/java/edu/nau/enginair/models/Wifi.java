package edu.nau.enginair.models;

public class Wifi {
    public String ssid;
    public String password;
    public String airportCode;
    public LatLong location;
    public float range;

    public Wifi() {}
    public Wifi(String ssid, String password, String airportCode, LatLong location, float strength) {
        this.ssid = ssid;
        this.password = password;
        this.airportCode = airportCode;
        this.location = location;
        this.range = ((10 - strength) / 10) * 91; //in theory, 300 ft (91 meters) is the max range of 2.4Ghz Wifi.
    }
}
