package edu.nau.enginair.models;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;


//ID,Name,City,Country,IATA,ICAO,Latitude,Longitude,Altitude,Timezone,DST,Tz,Type,Source

@Entity
@Indexes({
        @Index(fields = {@Field("IATA"), @Field(value = "location.geometry", type = IndexType.GEO2DSPHERE)},
                options = @IndexOptions(unique = true))
})
public class Airport {
    @Id
    public ObjectId id;
    public String Name;
    public String City;
    public String Country;
    public String IATA;
    public Float Latitude;
    public Float Longitude;
    public LatLong location = new LatLong();

    public Airport() {
    }

    public Airport(String name, String city, String country, String iata, Float longitude, Float latitude) {
        this.Name = name;
        this.City = city;
        this.Country = country;
        this.IATA = iata;
        this.Longitude = longitude;
        this.Latitude =  latitude;
    }


    public void printAirPort(){
        System.out.println("Airport Name: " + this.Name);
        System.out.println("Airport City: " + this.City);
        System.out.println("Airport Cont: " + this.Country);
        System.out.println("Airport iata: " + this.IATA);
        System.out.println("Airport Lati: " + this.Latitude);
        System.out.println("Airport Long: " + this.Longitude);
        System.out.println();
    }

    public void setLocation(){
        this.location.setLongitude(this.Longitude);
        this.location.setLatitude(this.Latitude);
    }
    public void killQuotes(){
        this.Name = this.Name.replaceAll("\"", "");
        this.City = this.City.replaceAll("\"", "");
        this.Country = this.Country.replaceAll("\"", "");
        this.IATA = this.IATA.replaceAll("\"", "");
    }

}
