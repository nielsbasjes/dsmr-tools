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
}
