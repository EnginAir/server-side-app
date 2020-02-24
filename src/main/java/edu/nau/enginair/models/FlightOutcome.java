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

import lombok.Getter;

/**
 * Created by ianot on 2/10/2020. None of this software may be reproduced without
 * the express written permission of PlaygroundMC.
 */
public enum FlightOutcome {
    SUCCESS_UPLOAD(0),
    FAIL_NO_LANDING(1),
    WARN_IN_PROGRESS(2),
    FAIL_NO_WIFI_AIRPORT(3),
    FAIL_NO_WIFI_AIRCRAFT(4),
    FAIL_DEAD_EDG100(5),
    FAIL_WAP_CHANGED(6);
    @Getter
    final int outcomeNum;
    FlightOutcome(int outcomeNum) {
        this.outcomeNum = outcomeNum;
    }
    public static FlightOutcome fromInt(int i) {
        switch(i) {
            case 0:
                return SUCCESS_UPLOAD;
            case 1:
                return FAIL_NO_LANDING;
            case 2:
                return WARN_IN_PROGRESS;
            case 3:
                return FAIL_NO_WIFI_AIRPORT;
            case 4:
                return FAIL_NO_WIFI_AIRCRAFT;
            case 5:
                return FAIL_DEAD_EDG100;
            case 6:
                return FAIL_WAP_CHANGED;
            default:
                return null;
        }
    }
}
