/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2021 Niels Basjes
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

import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.MBusEvent;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

final class Utils {

    private Utils() {
        // Utility class
    }

    public static void assertPowerFailureEvent(DSMRTelegram.PowerFailureEvent powerFailureEvent, String startTime, String endTime, String duration) {
        String actualStartTime  = powerFailureEvent.getStartTime().toString();
        String actualEndTime    = powerFailureEvent.getEndTime().toString();
        String actualDuration   = powerFailureEvent.getDuration().toString();

        if (!actualStartTime.equals(startTime) || !actualEndTime.equals(endTime) || !actualDuration.equals(duration)) {
            fail("PowerFailureLogEntry is different: \n" +
                "Expected: " + startTime + " --> " + endTime + " ~~ " + duration + "\n" +
                "Actual  : " + actualStartTime + " --> " + actualEndTime + " ~~ " + actualDuration + "\n"
            );
        }
    }

    public static void checkMbus(DSMRTelegram dsmrTelegram,
                           int mBusId,
                           String timeString,
                           int deviceType,
                           String equipmentId,
                           Double value,
                           String unit) {
        final MBusEvent mBusEvent = dsmrTelegram.getMBusEvents().get(mBusId);
        assertNotNull(mBusEvent);
        assertEquals(ZonedDateTime.parse(timeString),   mBusEvent.getTimestamp());
        assertEquals(deviceType,                        mBusEvent.getDeviceType());
        assertEquals(equipmentId,                       mBusEvent.getEquipmentId());
        assertEquals(value,                             mBusEvent.getValue(), 0.0001);
        assertEquals(unit,                              mBusEvent.getUnit());
    }

}
