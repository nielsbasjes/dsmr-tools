/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2024 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nl.basjes.dsmr.parse;

import nl.basjes.dsmr.TimestampParser;
import org.junit.jupiter.api.Test;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestTimestampParser {

    @Test
    void testReferenceExample(){
        TimestampParser timestampParser = new TimestampParser();

        // "0-0:1.0.0(101209113020W)"
        // It is send at 2010, December 9 th , 11h30m20s
        assertEquals("2010-12-09T11:30:20+01:00", timestampParser.parse("101209113020W").format(ISO_OFFSET_DATE_TIME));

        // "0-1:24.2.1(101209112500W)(12785.123*m3)"
        // Gas value of 2010, December 9 th , 11:25h is presented
        assertEquals("2010-12-09T11:25:00+01:00", timestampParser.parse("101209112500W").format(ISO_OFFSET_DATE_TIME));

        // 1-0:99.97.0(2)(0-0:96.7.19)(101208152415W)(0000000240*s)(101208151004W)(0000000301*s)
        // Failure at 2010, December 8 th , 15h20m15s, duration 240 seconds
        // Failure at 2010, December 8 th , 15h05m03s, duration 301 seconds
        assertEquals("2010-12-08T15:24:15+01:00", timestampParser.parse("101208152415W").format(ISO_OFFSET_DATE_TIME));
        assertEquals("2010-12-08T15:10:04+01:00", timestampParser.parse("101208151004W").format(ISO_OFFSET_DATE_TIME));

        assertEquals("2018-04-17T20:14:58+02:00", timestampParser.parse("180417201458S").format(ISO_OFFSET_DATE_TIME));
        assertEquals("2019-03-24T15:14:44+01:00", timestampParser.parse("190324151444W").format(ISO_OFFSET_DATE_TIME));
    }

    @Test
    void testOldFormat() {
        TimestampParser timestampParser = new TimestampParser();
        assertEquals("2019-03-24T15:14:44+01:00", timestampParser.parse("190324151444").format(ISO_OFFSET_DATE_TIME));
        assertEquals("2019-03-24T15:14:44+01:00", timestampParser.parse("190324151444xxx").format(ISO_OFFSET_DATE_TIME));
    }

    @Test
    void testNull() {
        TimestampParser timestampParser = new TimestampParser();
        assertNull(timestampParser.parse(null));
    }

    @Test
    void testEmpty() {
        TimestampParser timestampParser = new TimestampParser();
        assertNull(timestampParser.parse(""));
    }

    @Test
    void testBad() {
        TimestampParser timestampParser = new TimestampParser();
        assertNull(timestampParser.parse("Not a date string at all"));
    }

}
