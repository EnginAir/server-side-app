/*
 * The MIT License
 *
 * Copyright © 2010-2020 EnginAir Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.nau.enginair.models;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;
@Entity
@Indexes({
        @Index(fields = {@Field(value = "rolldown.geometry", type = IndexType.GEO2DSPHERE)})
})
public class CEDASUpload {

//    Engine rolldown GPS location
//    Engine rolldown time/date
//    Engine start GPS location
//    Engine start time/date
//    Upload GPS location (assumed to be half way between rolldown and start location)
//    WAP signal strength
//    WAP ID
//    Airport Code
    @Id
    public ObjectId id;
    String tailNumber;
    LatLong rolldown;
    @Getter
    @Setter
    Date rolldownTimeDate;
    LatLong startUp;
    @Getter
    @Setter
    Date startUpTimeDate;
    @Getter
    LatLong uploadLocation;
    @Getter
    String wapStrength;
    @Getter
    String wapID;
    @Getter
    String airportCode;

    public CEDASUpload() {
    }

    public CEDASUpload(String tailNumber, LatLong rolldown, Date rolldownTimeDate, LatLong startUp, Date startUpTimeDate, LatLong uploadLocation, String wapStrength, String wapID, String airportCode) {
        this.tailNumber = tailNumber;
        this.rolldown = rolldown;
        this.rolldownTimeDate = rolldownTimeDate;
        this.startUp = startUp;
        this.startUpTimeDate = startUpTimeDate;
        this.uploadLocation = uploadLocation;
        this.wapStrength = wapStrength;
        this.wapID = wapID;
        this.airportCode = airportCode;
    }
}
