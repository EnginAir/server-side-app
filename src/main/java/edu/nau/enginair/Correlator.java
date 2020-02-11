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

package edu.nau.enginair;/*
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

import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import edu.nau.enginair.models.ADSBData;
import edu.nau.enginair.models.AggregatedTailNumber;
import edu.nau.enginair.models.CorrellatedFlight;

import java.util.HashMap;
import java.util.Iterator;

public class Correlator {
    private Datastore connection;
    private HashMap<String, String> config;

    public Correlator(HashMap<String, String> config, Datastore connection) {
        this.connection = connection;
        this.config = config;
    }

    public int execute() {
        Query<ADSBData> q = this.connection.createQuery(ADSBData.class)
                .field("processed").equal(false)
                .field("altitude").lessThan(2000f)
                .field("speed").lessThan(300f);

        Iterator<AggregatedTailNumber> tails = this.connection.createAggregation(ADSBData.class)
                .match(q)
                .group("tailNumber")
                .aggregate(AggregatedTailNumber.class);
        if (tails.hasNext())
            tails.next(); //advance cursor by one? apparently that's what morphia wants
        while (tails.hasNext()) {
            processTail(tails.next());
        }
        return 0;
    }

    private void processTail(AggregatedTailNumber item) {
        String tailNum = item._id; //we only get this out of the aggregation
        Iterator<ADSBData> data = this.connection.createQuery(ADSBData.class)
                .filter("tailNumber", tailNum)
                .filter("processed", false)
                .order(Sort.ascending("PosTime"))
                .find();
        ADSBData previous = null, current;
        CorrellatedFlight currentFlight = null;
        while (data.hasNext()) {
            current = data.next();
            if (shouldBeNewFlight(previous, current)) {
                if (currentFlight != null) {
                    if (isLandingSituation(previous)) {
                        currentFlight.setLandingDate(previous.PosTime);
                        currentFlight.setLandingPoint(previous.location);
                    } else {
                        System.out.println("Location was not a landing disposition! Not setting a landing point!");
                    }
                    this.connection.save(currentFlight);
                }
                currentFlight = new CorrellatedFlight();
                currentFlight.getFlightPath().add(current.location);
                currentFlight.setTakeoffPoint(current.location);
            } else {
                currentFlight.getFlightPath().add(current.location);
            }
            previous = current;
        }
    }

    private boolean shouldBeNewFlight(ADSBData previous, ADSBData current) {
        if (previous == null) {
            return true;
        }
        //If we lose tracking for more than 1 hour, we can be pretty sure they landed
        if (current.PosTime.getTime() - previous.PosTime.getTime() > 1000 * 60 * 60) {
            assert current.altitude < 2000 && previous.altitude < 2000;
            return true;
        }
        return false;
    }

    private boolean isLandingSituation(ADSBData data) {
        return data.altitude < 3000 && data.speed < 300;
    }
}
