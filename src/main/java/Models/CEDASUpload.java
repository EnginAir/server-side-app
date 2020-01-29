package Models;

public class CEDASUpload {
    String tailNumber;
    LatLong uploadPoint;

    public CEDASUpload(String tailNumber, LatLong uploadPoint){
        this.tailNumber = tailNumber;
        this.uploadPoint = uploadPoint;
    }
}
