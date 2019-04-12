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
    public boolean validCRC = false;
    public String ident;
    public String crc;

    public String p1Version;                        // P1 Version information
    public ZonedDateTime timestamp;                 // Timestamp
    public String equipmentId;                      // Equipment identifier

    public Double electricityReceivedLowTariff;     // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
    public Double electricityReceivedNormalTariff;  // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
    public Double electricityReturnedLowTariff;     // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
    public Double electricityReturnedNormalTariff;  // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
    public Double electricityTariffIndicator;       // Tariff indicator electricity
    public Double electricityPowerReceived;         // Actual electricity power delivered (+P) in 1 Watt resolution
    public Double electricityPowerReturned;         // Actual electricity power received (-P) in 1 Watt resolution

    public Long powerFailures;                      // Number of power failures in any phases
    public Long longPowerFailures;                  // Number of long power failures in any phases
    // TODO: public double powerFailureEventLog;             // Power failure event log
    public Long voltageSagsPhaseL1;                 // Number of voltage sags in phase L1
    public Long voltageSagsPhaseL2;                 // Number of voltage sags in phase L2
    public Long voltageSagsPhaseL3;                 // Number of voltage sags in phase L3
    public Long voltageSwellsPhaseL1;               // Number of voltage swells in phase L1
    public Long voltageSwellsPhaseL2;               // Number of voltage swells in phase L2
    public Long voltageSwellsPhaseL3;               // Number of voltage swells in phase L3

    public Double voltageL1;                        // Instantaneous voltage L1
    public Double voltageL2;                        // Instantaneous voltage L2
    public Double voltageL3;                        // Instantaneous voltage L3
    public Double currentL1;                        // Instantaneous current L1
    public Double currentL2;                        // Instantaneous current L2
    public Double currentL3;                        // Instantaneous current L3
    public Double powerReceivedL1;                  // Instantaneous active power L1 (+P)
    public Double powerReceivedL2;                  // Instantaneous active power L2 (+P)
    public Double powerReceivedL3;                  // Instantaneous active power L3 (+P)
    public Double powerReturnedL1;                  // Instantaneous active power L1 (-P)
    public Double powerReturnedL2;                  // Instantaneous active power L2 (-P)
    public Double powerReturnedL3;                  // Instantaneous active power L3 (-P)
    public String message;                          // Text message max 1024 characters.

    public Map<Integer, MBusEvent> mBusEvents = new TreeMap<>();

    // NOTE: This assumes only AT MOST ONE attached thing per type of meter.
    // Doing two 'gas meters' will only map the first one (i.e. with the lowest MBus id)!!!

    // Water
    public String        waterEquipmentId;
    public ZonedDateTime waterTimestamp;    // Water measurement timestamp
    public Double        waterM3;           // Water consumption in cubic meters

    // Gas
    public String        gasEquipmentId;
    public ZonedDateTime gasTimestamp;      // Gas measurement timestamp
    public Double        gasM3;             // Gas consumption in cubic meters

    // Thermal: Heat or Cold
    public String        thermalHeatEquipmentId;
    public ZonedDateTime thermalHeatTimestamp;  // Thermal Timestamp
    public Double        thermalHeatGJ;         // Thermal GigaJoule

    public String        thermalColdEquipmentId;
    public ZonedDateTime thermalColdTimestamp;  // Thermal Timestamp
    public Double        thermalColdGJ;         // Thermal GigaJoule

    // Electricity via a slave
    public String        slaveEMeterEquipmentId;
    public ZonedDateTime slaveEMeterTimestamp;  // Slave e-meter measurement timestamp
    public Double        slaveEMeterkWh;        // Slave e-meter consumption in kWh
}
