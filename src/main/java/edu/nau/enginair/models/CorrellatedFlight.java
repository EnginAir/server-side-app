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

import com.google.gson.annotations.Expose;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Indexes({
        @Index(fields = {@Field("tailNumber"), @Field(value = "takeoffPoint.geometry", type = IndexType.GEO2DSPHERE)},
                options = @IndexOptions(unique = true)),
        @Index(fields = {@Field(value = "landingPoint.geometry", type = IndexType.GEO2DSPHERE)})
})
public class CorrellatedFlight {
    @Getter
    @Id
    private ObjectId id;
    @Getter
    @Setter
    private Float lastAltitude;
    @Getter
    @Setter
    private Date lastPing;
    @Getter
    @Setter
    private String tailNumber;
    @Getter
    @Setter
    private LatLong landingPoint;
    @Getter
    @Setter
    private LatLong takeoffPoint;
    @Getter
    @Setter
    private Date landingDate;
    @Getter
    @Expose(serialize = false, deserialize = false)
    private List<LatLong> flightPath;
    @Getter
    @Setter
    private FlightOutcome outcome;

    public CorrellatedFlight() {
        flightPath = new ArrayList<>();
    }
}
