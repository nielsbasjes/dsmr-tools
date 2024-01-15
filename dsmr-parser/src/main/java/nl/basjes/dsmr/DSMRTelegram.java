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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Getter
@ToString
public class DSMRTelegram {
    /** Is this record classified as a valid record. I.e. do we think you can use this data. */
    boolean isValid;
    /** Does this record have a CRC and is it valid. Always false on DSMR 2.2 records. */
    boolean validCRC;
    /** The raw identification of this device. */
    String rawIdent;
    /** The 3 letter brand identification of this device. */
    String equipmentBrandTag;
    /** The identification of this device. */
    String ident;
    /** The CRC of the record. Is null on DSMR 2.2 records. */
    String crc;

    /** P1 Version information. Is null on DSMR 2.2 records. */
    String p1Version;
    /** Timestamp of the measurement as recorded by the clock in the meter (which are usually quite inaccurate). */
    ZonedDateTime timestamp;
    /** Timestamp when the measurement was received by the server (usually very accurate because of NTP). */
    ZonedDateTime receiveTimestamp;
    /** Equipment identifier   */
    String equipmentId;

    /** Tariff indicator electricity */
    Long   electricityTariffIndicator;
    /** Meter Reading electricity delivered to client (low tariff) in 0,001 kWh */
    Double electricityReceivedLowTariff;
    /** Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh */
    Double electricityReceivedNormalTariff;
    /** Actual electricity power delivered (+P) in 1 Watt resolution (value in kW) */
    Double electricityPowerReceived;

    /** Meter Reading electricity returned by client (low tariff) in 0,001 kWh */
    Double electricityReturnedLowTariff;
    /** Meter Reading electricity returned by client (normal tariff) in 0,001 kWh */
    Double electricityReturnedNormalTariff;
    /** Actual electricity power returned (-P) in 1 Watt resolution (value in kW) */
    Double electricityPowerReturned;

    /** Number of power failures in any phases */
    Long powerFailures;
    /** Number of long power failures in any phases */
    Long longPowerFailures;

    @Getter
    @ToString
    public static final class PowerFailureEvent {
        /** When did the power failure start    */ ZonedDateTime startTime;
        /** When did the power failure end      */ ZonedDateTime endTime;
        /** How long did the power failure last */ Duration duration;
    }

    Long powerFailureEventLogSize = 0L;      // Power failure event log size (as indicated in the output)
    List<PowerFailureEvent> powerFailureEventLog = new ArrayList<>(); // Power failure event log

    /** Number of voltage sags in phase L1    */ Long voltageSagsPhaseL1;
    /** Number of voltage sags in phase L2    */ Long voltageSagsPhaseL2;
    /** Number of voltage sags in phase L3    */ Long voltageSagsPhaseL3;

    /** Number of voltage swells in phase L1  */ Long voltageSwellsPhaseL1;
    /** Number of voltage swells in phase L2  */ Long voltageSwellsPhaseL2;
    /** Number of voltage swells in phase L3  */ Long voltageSwellsPhaseL3;

    /** Instantaneous voltage L1              */ Double voltageL1;
    /** Instantaneous voltage L2              */ Double voltageL2;
    /** Instantaneous voltage L3              */ Double voltageL3;

    /** Instantaneous current L1              */ Double currentL1;
    /** Instantaneous current L2              */ Double currentL2;
    /** Instantaneous current L3              */ Double currentL3;

    /** Instantaneous active power L1 (+P)    */ Double powerReceivedL1;
    /** Instantaneous active power L2 (+P)    */ Double powerReceivedL2;
    /** Instantaneous active power L3 (+P)    */ Double powerReceivedL3;

    /** Instantaneous active power L1 (-P)    */ Double powerReturnedL1;
    /** Instantaneous active power L2 (-P)    */ Double powerReturnedL2;
    /** Instantaneous active power L3 (-P)    */ Double powerReturnedL3;

    /** Text message codes: numeric 8 digits. */ String messageCodes;
    /** Text message max 1024 characters.     */ String message;

    final Map<Integer, MBusEvent> mBusEvents = new TreeMap<>();

    /** Also expose the mBusEvents as a List instead of a Map */
    @SuppressWarnings("unused") // Use via reflection by the GraphQL service
    public List<MBusEventEntry> getMBusEventList() {
        return mBusEvents
            .entrySet().stream()
            .map(e -> new MBusEventEntry(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    // NOTE: This assumes only AT MOST ONE attached thing per type of meter.
    // Doing two 'gas meters' will only map the first one (i.e. with the lowest MBus id)!!!

    // Gas
    /** Gas measurement device id             */ String        gasEquipmentId;
    /** Gas measurement timestamp             */ ZonedDateTime gasTimestamp;
    /** Gas consumption in cubic meters       */ Double        gasM3;

    // Electricity via a slave
    /** Slave e-meter device id               */ String        slaveEMeterEquipmentId;
    /** Slave e-meter measurement timestamp   */ ZonedDateTime slaveEMeterTimestamp;
    /** Slave e-meter consumption in kWh      */ Double        slaveEMeterkWh;
}
