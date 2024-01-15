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

package nl.basjes.dsmr;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampParser {
    // YYMMDDhhmmssX ASCII presentation of Time stamp
    // Year, Month, Day, Hour, Minute, Second, and an indication whether
    // DST is active (X=S ... SummerTime) or DST is not active (X=W ... WinterTime).

    // IMPORTANT ASSUMPTION:
    // This is for the DSMR = Dutch Smart Meter Requirements.
    // Dutch ! means Netherlands which means timezone "Europe/Amsterdam"

    //                                           Year        Month      Day         Hour        Minute      Second      Summer/Winter time
    // Format                                    Y    Y      M   M      D    D      h    h      m    m      s    s      S or W
    private static final String TIME_FORMAT = "([0-9][0-9])([01][0-9])([0-3][0-9])([0-2][0-9])([0-5][0-9])([0-5][0-9])([SsWw]?)";

    private static final Pattern DATE_TIME_PATTERN = Pattern.compile(TIME_FORMAT);

    public ZonedDateTime parse(String dsmrTimestamp) {
        if (dsmrTimestamp == null || dsmrTimestamp.isEmpty()) {
            return null;
        }

        Matcher matcher = DATE_TIME_PATTERN.matcher(dsmrTimestamp);

        if (!matcher.find()) {
            return null;
        }
        // CHECKSTYLE.OFF: ParenPad
        Instant baseInstant = Instant.ofEpochSecond(0);
        ZonedDateTime zonedDateTime = ZonedDateTime
            .ofInstant(baseInstant, ZoneOffset.UTC)
            .withYear(2000 +  Integer.parseInt(matcher.group(1)) )
            .withMonth(       Integer.parseInt(matcher.group(2)) )
            .withDayOfMonth(  Integer.parseInt(matcher.group(3)) )
            .withHour(        Integer.parseInt(matcher.group(4)) )
            .withMinute(      Integer.parseInt(matcher.group(5)) )
            .withSecond(      Integer.parseInt(matcher.group(6)) );

        ZoneId zoneId;

        switch(matcher.group(7)) {
            case "S": // Dutch Summertime
            case "s":
                zoneId = ZoneOffset.of("+02:00");
                break;

            case "W": // Dutch Wintertime
            case "w":
                zoneId = ZoneOffset.of("+01:00");
                break;

            default:
                zoneId = ZoneId.of("Europe/Amsterdam");
                break;
        }
        zonedDateTime = zonedDateTime.withZoneSameLocal(zoneId);

        return zonedDateTime;
    }

}
