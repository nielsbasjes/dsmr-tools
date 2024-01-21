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

import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
public class MBusEvent {
    /** MBus event: Device type.                                */ Integer deviceType;
    /** MBus event: Equipment Identifier.                       */ String equipmentId;
    /** MBus event: Last 5 minute reading (the value).          */ Double value;
    /** MBus event: Last 5 minute reading (the unit: m3 or GJ). */ String unit;
    /** MBus event: Timestamp of last 5 minute reading.         */ ZonedDateTime timestamp;

    /** MBus event: Device type name (Human readable form of the device type). */
    public String getDeviceTypeName() {
        switch (deviceType) {
            // DSMR only supports a subset of the MBus.
            // And of that subset only 2 (Electricity) and 3 (Gas) actually occur.
            case 0x02: return "Electricity";
            case 0x03: return "Gas";
            case 0x04: return "Heat";
            case 0x06: return "Warm water (30-90 Celcius)";
            case 0x07: return "Water";
            case 0x15: return "Hot water (>90 Celcius)";
            case 0x17: return "Dual register meter (Hot/Cold water meter)";

            // This mapping is based on the documentation found on http://www.m-bus.com/
//            case 0x00:  return "Other";                                                               // 0000 0000  00
//            case 0x01:  return "Oil";                                                                 // 0000 0001  01
//            case 0x02:  return "Electricity";                                                         // 0000 0010  02
//            case 0x03:  return "Gas";                                                                 // 0000 0011  03
//            case 0x05:  return "Steam";                                                               // 0000 0101  05
//            case 0x06:  return "Warm Water (30-90 Celcius)";                                          // 0000 0110  06
//            case 0x07:  return "Water";                                                               // 0000 0111  07
//            case 0x08:  return "Heat Cost Allocator";                                                 // 0000 1000  08
//            case 0x09:  return "Compressed Air";                                                      // 0000 1001  09
//            case 0x0A:  return "Cooling load meter (Volume measured at return temperature: outlet)";  // 0000 1010  0A
//            case 0x0B:  return "Cooling load meter (Volume measured at flow temperature: inlet)";     // 0000 1011  0B
//            case 0x0C:  return "Heat (Volume measured at flow temperature: inlet)";                   // 0000 1100  0C
//            case 0x0D:  return "Heat / Cooling load meter";                                           // 0000 1101  OD
//            case 0x0E:  return "Bus / System";                                                        // 0000 1110  0E
//            case 0x0F:  return "Unknown Medium";                                                      // 0000 1111  0F
//            case 0x10:  return "Reserve";                                                             // .......... 10 - 14
//            case 0x15:  return "Hot Water (>90 Celsius)";                                             // 0001 0101  15
//            case 0x16:  return "Cold Water";                                                          // 0001 0110  16
//            case 0x17:  return "Dual register (hot/cold) Water";                                      // 0001 0111  17
//            case 0x18:  return "Pressure";                                                            // 0001 1000  18
//            case 0x19:  return "A/D Converter";                                                       // 0001 1001  19
//            case 0x20:  return "Reserve";                                                             // .......... 20 - FF

            default: return "MBus device type " + deviceType + " does not occur in DSMR.";
        }
    }
}



