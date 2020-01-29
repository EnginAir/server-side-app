package Models;

public class CorrellatedFlight {
    String tailNumber;
    LatLong landingPoint;

    public CorrellatedFlight(String tailNumber, LatLong landingPoint){

        this.tailNumber = tailNumber;
        this.landingPoint = landingPoint;

    }
}
