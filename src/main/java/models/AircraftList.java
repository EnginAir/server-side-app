package models;

public class AircraftList {
    public String Reg;
    public Float Alt;
    public Float Lat;
    public Float Long;
    public Float Spd;
    public Long PosTime;

    public AircraftList(String Reg, Float Alt, Float Lat, Float Long, Float Spd, Long PosTime) {
        this.Reg = Reg;
        this.Alt = Alt;
        this.Lat = Lat;
        this.Long = Long;
        this.Spd = Spd;
        this.PosTime = PosTime;
    }
}