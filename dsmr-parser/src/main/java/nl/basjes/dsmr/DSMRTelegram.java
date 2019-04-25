/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2019 Niels Basjes
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
import java.util.Map;
import java.util.TreeMap;

@Getter
@ToString
public class DSMRTelegram {
    boolean isValid;
    boolean validCRC;
    String ident;
    String crc;

    String p1Version;                        // P1 Version information
    ZonedDateTime timestamp;                 // Timestamp
    String equipmentId;                      // Equipment identifier

    Double electricityReceivedLowTariff;     // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
    Double electricityReceivedNormalTariff;  // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
    Double electricityReturnedLowTariff;     // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
    Double electricityReturnedNormalTariff;  // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
    Double electricityTariffIndicator;       // Tariff indicator electricity
    Double electricityPowerReceived;         // Actual electricity power delivered (+P) in 1 Watt resolution
    Double electricityPowerReturned;         // Actual electricity power received (-P) in 1 Watt resolution

    Long powerFailures;                      // Number of power failures in any phases
    Long longPowerFailures;                  // Number of long power failures in any phases
    // TODO: List<...> powerFailureEventLog; // Power failure event log
    Long voltageSagsPhaseL1;                 // Number of voltage sags in phase L1
    Long voltageSagsPhaseL2;                 // Number of voltage sags in phase L2
    Long voltageSagsPhaseL3;                 // Number of voltage sags in phase L3
    Long voltageSwellsPhaseL1;               // Number of voltage swells in phase L1
    Long voltageSwellsPhaseL2;               // Number of voltage swells in phase L2
    Long voltageSwellsPhaseL3;               // Number of voltage swells in phase L3

    Double voltageL1;                        // Instantaneous voltage L1
    Double voltageL2;                        // Instantaneous voltage L2
    Double voltageL3;                        // Instantaneous voltage L3
    Double currentL1;                        // Instantaneous current L1
    Double currentL2;                        // Instantaneous current L2
    Double currentL3;                        // Instantaneous current L3
    Double powerReceivedL1;                  // Instantaneous active power L1 (+P)
    Double powerReceivedL2;                  // Instantaneous active power L2 (+P)
    Double powerReceivedL3;                  // Instantaneous active power L3 (+P)
    Double powerReturnedL1;                  // Instantaneous active power L1 (-P)
    Double powerReturnedL2;                  // Instantaneous active power L2 (-P)
    Double powerReturnedL3;                  // Instantaneous active power L3 (-P)
    String message;                          // Text message max 1024 characters.

    Map<Integer, MBusEvent> mBusEvents = new TreeMap<>();

    // NOTE: This assumes only AT MOST ONE attached thing per type of meter.
    // Doing two 'gas meters' will only map the first one (i.e. with the lowest MBus id)!!!

    // Water
    String        waterEquipmentId;
    ZonedDateTime waterTimestamp;    // Water measurement timestamp
    Double        waterM3;           // Water consumption in cubic meters

    // Gas
    String        gasEquipmentId;
    ZonedDateTime gasTimestamp;      // Gas measurement timestamp
    Double        gasM3;             // Gas consumption in cubic meters

    // Thermal: Heat or Cold
    String        thermalHeatEquipmentId;
    ZonedDateTime thermalHeatTimestamp;  // Thermal Timestamp
    Double        thermalHeatGJ;         // Thermal GigaJoule

    String        thermalColdEquipmentId;
    ZonedDateTime thermalColdTimestamp;  // Thermal Timestamp
    Double        thermalColdGJ;         // Thermal GigaJoule

    // Electricity via a slave
    String        slaveEMeterEquipmentId;
    ZonedDateTime slaveEMeterTimestamp;  // Slave e-meter measurement timestamp
    Double        slaveEMeterkWh;        // Slave e-meter consumption in kWh
}
