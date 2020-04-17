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

import com.mongodb.DuplicateKeyException;
import dev.morphia.Datastore;
import dev.morphia.geo.GeoJson;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import edu.nau.enginair.models.*;

import java.util.*;

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
        Set<String> meme = new HashSet<>();
        while (tails.hasNext()) {
            meme.add(tails.next()._id);
        }
        int i = 0;
        for (String s : meme) {
            processTail(s);
            i++;
        }
        return 0;
    }

    private void processTail(String tailNum) {
        Query<ADSBData> q = this.connection.createQuery(ADSBData.class)
                .field("tailNumber").equalIgnoreCase(tailNum)
                .field("processed").equal(false)
                .order(Sort.ascending("PosTime"));
        Iterator<ADSBData> data = q.find();
        ADSBData previous = null, current;
        List<CorrellatedFlight> flights = new ArrayList<>();
        CorrellatedFlight currentFlight = null;
        while (data.hasNext()) {
            current = data.next();
            if (shouldBeNewFlight(previous, current) || !data.hasNext()) {
                if (currentFlight != null) {
                    if (isLandingSituation(previous)) {
                        currentFlight.setLandingDate(previous.PosTime);
                        currentFlight.setLandingPoint(previous.location);
                    } else {
                        System.out.println("Location was not a landing disposition! Not setting a landing point!");
                        currentFlight.setOutcome(FlightOutcome.WARN_IN_PROGRESS);
                    }
                }
                if (data.hasNext()) {
                    currentFlight = new CorrellatedFlight();
                    currentFlight.getFlightPath().add(current.location);
                    currentFlight.setTakeoffPoint(current.location);
                    currentFlight.setTailNumber(current.tailNumber);
                    currentFlight.setLastAltitude(current.altitude);
                    currentFlight.setLastPing(current.PosTime);
                    flights.add(currentFlight);
                }
            } else {
                currentFlight.getFlightPath().add(current.location);
                currentFlight.setLastAltitude(current.altitude);
                currentFlight.setLastPing(current.PosTime);
            }
            previous = current;
        }
        correlate(flights);
    }

    private void correlate(List<CorrellatedFlight> flights) {
        for (CorrellatedFlight flight : flights) {
            if (flight.getLandingPoint() == null && flight.getOutcome() != FlightOutcome.WARN_IN_PROGRESS) {
                flight.setOutcome(FlightOutcome.WARN_IN_PROGRESS);
            } else if (flight.getOutcome() == FlightOutcome.WARN_IN_PROGRESS) {
                //intentionally blank
            } else {
                Query<CEDASUpload> hasUpload = connection.createQuery(CEDASUpload.class)
                        .field("tailNumber").equalIgnoreCase(flight.getTailNumber())
                        .field("rolldownTimeDate").greaterThan(flight.getLandingDate())
                        .field("rolldownTimeDate").lessThan(new Date(flight.getLandingDate().getTime() + (1000 * 60 * 60)))
                        .field("rolldown.geometry").near(GeoJson.point(flight.getLandingPoint().getLatitude(), flight.getLandingPoint().getLongitude()), 1000d, 0d);
                CEDASUpload c = hasUpload.first();
                if (c == null) {
                    Query<CEDASUpload> anyUploadsAtPoint = connection.createQuery(CEDASUpload.class)
                            .field("rolldown.geometry").near(GeoJson.point(flight.getLandingPoint().getLatitude(), flight.getLandingPoint().getLongitude()), 1000);
                    CEDASUpload a = anyUploadsAtPoint.first();
                    if (a == null) {
                        flight.setOutcome(FlightOutcome.FAIL_NO_WIFI_AIRPORT);
                    } else {
                        Query<CorrellatedFlight> anyUploadsRecently = connection.createQuery(CorrellatedFlight.class)
                                .field("tailNumber").equalIgnoreCase(flight.getTailNumber())
                                .order(Sort.descending("landingDate"));
                        Iterator<CorrellatedFlight> it = anyUploadsRecently.find(new FindOptions().limit(3));
                        int counter = 0;
                        while (it.hasNext()) {
                            CorrellatedFlight up = it.next();
                            if (up.getOutcome() == FlightOutcome.SUCCESS_UPLOAD) {
                                flight.setOutcome(FlightOutcome.FAIL_WAP_CHANGED);
                            } else {
                                counter++;
                            }
                        }
                        if (counter == 3) {
                            flight.setOutcome(FlightOutcome.FAIL_DEAD_EDG100);
                        } else {
                            flight.setOutcome(FlightOutcome.FAIL_NO_WIFI_AIRCRAFT);
                        }
                    }
                } else {
                    flight.setOutcome(FlightOutcome.SUCCESS_UPLOAD);
                }
            }
            connection.ensureIndexes();
            try {
                connection.save(flight);
            } catch (DuplicateKeyException e) {
                System.out.println("Ignored duplicated correlated flight: " + flight.getTailNumber() + " : " + flight.getOutcome());
            }
        }
    }

    private boolean shouldBeNewFlight(ADSBData previous, ADSBData current) {
        if (previous == null) {
            return true;
        }
        //If we lose tracking for more than 2 hours, we can be pretty sure they landed
        if (current.PosTime.getTime() - previous.PosTime.getTime() > 1000 * 60 * 120) {
            assert current.altitude < 2000 && previous.altitude < 2000;
            return true;
        }
        return false;
    }

    private boolean isLandingSituation(ADSBData data) {
        return data.altitude < 6000 && data.speed < 300;
    }
}
