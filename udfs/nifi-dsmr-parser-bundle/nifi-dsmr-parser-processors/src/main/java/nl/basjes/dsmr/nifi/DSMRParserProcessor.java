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
package nl.basjes.dsmr.nifi;

import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.MBusEvent;
import nl.basjes.dsmr.ParseDsmrTelegram;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.stream.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Tags({"iot", "dsmr"})
@CapabilityDescription("Parses a DSMR record into attributes. " +
    "Use this end-of-record regex for the sensor-stream-cutter:  \\r\\n![0-9A-F]{4}\\r\\n   ")
@SeeAlso({})
@SideEffectFree
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class DSMRParserProcessor extends AbstractProcessor {

    public static final Relationship VALID = new Relationship.Builder()
        .name("Valid")
        .description("Complete and valid records")
        .build();
    public static final Relationship INVALID_CRC = new Relationship.Builder()
        .name("InvalidCRC")
        .description("Complete records with invalid CRC")
        .build();
    public static final Relationship BAD_RECORDS = new Relationship.Builder()
        .name("BadRecords")
        .description("Incomplete records / Parsing failed")
        .build();

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final Set<Relationship> relationshipSet = new HashSet<Relationship>();
        relationshipSet.add(VALID);
        relationshipSet.add(INVALID_CRC);
        relationshipSet.add(BAD_RECORDS);
        this.relationships = Collections.unmodifiableSet(relationshipSet);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return Collections.emptyList();
    }

    private static final String ATTRIBUTE_PREFIX = "dsmr.";

    private void put(Map<String, String> map, String name, String value) {
        if (value != null) {
            map.put(ATTRIBUTE_PREFIX + name, value);
        }
    }

    private void put(Map<String, String> map, String name, Double value) {
        if (value != null) {
            map.put(ATTRIBUTE_PREFIX + name, Double.toString(value));
        }
    }

    private void put(Map<String, String> map, String name, Boolean value) {
        if (value != null) {
            map.put(ATTRIBUTE_PREFIX + name, value.toString());
        }
    }

    private void put(Map<String, String> map, String name, Long value) {
        if (value != null) {
            map.put(ATTRIBUTE_PREFIX + name, Long.toString(value));
        }
    }

    private void put(Map<String, String> map, String name, Integer value) {
        if (value != null) {
            map.put(ATTRIBUTE_PREFIX + name, Integer.toString(value));
        }
    }

    private void put(Map<String, String> map, String name, ZonedDateTime value) {
        if (value != null) {
            map.put(ATTRIBUTE_PREFIX + name, ISO_OFFSET_DATE_TIME.format(value));

            // In DSMR the are no times more accurate than a second.
            // If you need milli/micro or even nano seconds just append some zeros.
            map.put(ATTRIBUTE_PREFIX + name + ".epochSecond", String.valueOf(value.toEpochSecond()));
        }
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        long size = flowFile.getSize();
        if (size < 5 || size > 1024 * 1024) {
            getLogger().info("Received a flowfile with an invalid content size (it was {} bytes)", new Object[]{size});
            session.transfer(flowFile, BAD_RECORDS);
            return;
        }

        final byte[] byteBuffer = new byte[(int)size];
        session.read(flowFile, new InputStreamCallback() {
            @Override
            public void process(InputStream in) throws IOException {
                StreamUtils.fillBuffer(in, byteBuffer, false);
            }
        });
        final long len = Math.min(byteBuffer.length, flowFile.getSize());
        String contentString = new String(byteBuffer, 0, (int) len, UTF_8);

        DSMRTelegram record = ParseDsmrTelegram.parse(contentString);

        if (record == null) {
            return; // This should not happen.
        }

        Map<String, String> parseResults = new HashMap<>();

        // CHECKSTYLE.OFF: LineLength
        put(parseResults, "validCRC",                         record.isValidCRC());

        put(parseResults, "ident",                            record.getIdent());
        put(parseResults, "crc",                              record.getCrc());

        put(parseResults, "p1Version",                        record.getP1Version());                        // P1 Version information
        put(parseResults, "timestamp",                        record.getTimestamp());                        // Timestamp
        put(parseResults, "equipmentId",                      record.getEquipmentId());                      // Equipment identifier

        put(parseResults, "electricityReceivedLowTariff",     record.getElectricityReceivedLowTariff());     // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
        put(parseResults, "electricityReceivedNormalTariff",  record.getElectricityReceivedNormalTariff());  // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
        put(parseResults, "electricityReturnedLowTariff",     record.getElectricityReturnedLowTariff());     // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
        put(parseResults, "electricityReturnedNormalTariff",  record.getElectricityReturnedNormalTariff());  // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
        put(parseResults, "electricityTariffIndicator",       record.getElectricityTariffIndicator());       // Tariff indicator electricity
        put(parseResults, "electricityPowerReceived",         record.getElectricityPowerReceived());         // Actual electricity power delivered (+P) in 1 Watt resolution
        put(parseResults, "electricityPowerReturned",         record.getElectricityPowerReturned());         // Actual electricity power received (-P) in 1 Watt resolution

        put(parseResults, "powerFailures",                    record.getPowerFailures());                    // Number of power failures in any phases
        put(parseResults, "longPowerFailures",                record.getLongPowerFailures());                // Number of long power failures in any phases
// TODO: private double powerFailureEventLog;                       // Power failure event log
        put(parseResults, "voltageSagsPhaseL1",               record.getVoltageSagsPhaseL1());               // Number of voltage sags in phase L1
        put(parseResults, "voltageSagsPhaseL2",               record.getVoltageSagsPhaseL2());               // Number of voltage sags in phase L2
        put(parseResults, "voltageSagsPhaseL3",               record.getVoltageSagsPhaseL3());               // Number of voltage sags in phase L3
        put(parseResults, "voltageSwellsPhaseL1",             record.getVoltageSwellsPhaseL1());             // Number of voltage swells in phase L1
        put(parseResults, "voltageSwellsPhaseL2",             record.getVoltageSwellsPhaseL2());             // Number of voltage swells in phase L2
        put(parseResults, "voltageSwellsPhaseL3",             record.getVoltageSwellsPhaseL3());             // Number of voltage swells in phase L3

        put(parseResults, "voltageL1",                        record.getVoltageL1());                        // Instantaneous voltage L1
        put(parseResults, "voltageL2",                        record.getVoltageL2());                        // Instantaneous voltage L2
        put(parseResults, "voltageL3",                        record.getVoltageL3());                        // Instantaneous voltage L3
        put(parseResults, "currentL1",                        record.getCurrentL1());                        // Instantaneous current L1
        put(parseResults, "currentL2",                        record.getCurrentL2());                        // Instantaneous current L2
        put(parseResults, "currentL3",                        record.getCurrentL3());                        // Instantaneous current L3
        put(parseResults, "powerReceivedL1",                  record.getPowerReceivedL1());                  // Instantaneous active power L1 (+P)
        put(parseResults, "powerReceivedL2",                  record.getPowerReceivedL2());                  // Instantaneous active power L2 (+P)
        put(parseResults, "powerReceivedL3",                  record.getPowerReceivedL3());                  // Instantaneous active power L3 (+P)
        put(parseResults, "powerReturnedL1",                  record.getPowerReturnedL1());                  // Instantaneous active power L1 (-P)
        put(parseResults, "powerReturnedL2",                  record.getPowerReturnedL2());                  // Instantaneous active power L2 (-P)
        put(parseResults, "powerReturnedL3",                  record.getPowerReturnedL3());                  // Instantaneous active power L3 (-P)
        put(parseResults, "message",                          record.getMessage());                          // Text message max 1024 characters.

        put(parseResults, "mBusEvents",                       record.getMBusEvents().size());                // The number of available MBus entries

        for(Map.Entry<Integer, MBusEvent> mBusEventEntry: record.getMBusEvents().entrySet()) {
            MBusEvent mBusEvent = mBusEventEntry.getValue();
            if (mBusEvent != null) {
                put(parseResults, "mbus."+mBusEventEntry.getKey()+".deviceType",  mBusEvent.getDeviceType());
                put(parseResults, "mbus."+mBusEventEntry.getKey()+".equipmentId", mBusEvent.getEquipmentId());
                put(parseResults, "mbus."+mBusEventEntry.getKey()+".timestamp",   mBusEvent.getTimestamp());
                put(parseResults, "mbus."+mBusEventEntry.getKey()+".value",       mBusEvent.getValue());
                put(parseResults, "mbus."+mBusEventEntry.getKey()+".unit",        mBusEvent.getUnit());
            }
        }

        // NOTE: This assumes only AT MOST ONE attached thing per type of meter.
        // Doing two 'gas meters' will only map the first one (i.e. with the lowest MBus id)!!!

        // Water
        put(parseResults, "waterEquipmentId",                 record.getWaterEquipmentId());                 // Water measuring equipment id
        put(parseResults, "waterTimestamp",                   record.getWaterTimestamp());                   // Water measurement timestamp
        put(parseResults, "waterM3",                          record.getWaterM3());                          // Measured Water quantity in M3

        // Gas
        put(parseResults, "gasEquipmentId",                   record.getGasEquipmentId());                   // Gas measuring equipment id
        put(parseResults, "gasTimestamp",                     record.getGasTimestamp());                     // Gas measurement timestamp
        put(parseResults, "gasM3",                            record.getGasM3());                            // Measured Gas quantity in M3

        // Thermal: Heat or Cold
        put(parseResults, "thermalHeatEquipmentId",           record.getThermalHeatEquipmentId());           // Heat measuring equipment id
        put(parseResults, "thermalHeatTimestamp",             record.getThermalHeatTimestamp());             // Heat measurement timestamp
        put(parseResults, "thermalHeatGJ",                    record.getThermalHeatGJ());                    // Measured Heat quantity in GigaJoule

        put(parseResults, "thermalColdEquipmentId",           record.getThermalColdEquipmentId());           // Cold measuring equipment id
        put(parseResults, "thermalColdTimestamp",             record.getThermalColdTimestamp());             // Cold measurement timestamp
        put(parseResults, "thermalColdGJ",                    record.getThermalColdGJ());                    // Measured Cold quantity in GigaJoule

        // Electricity via a slave
        put(parseResults, "slaveEMeterEquipmentId",           record.getSlaveEMeterEquipmentId());           // Slave e-meter equipment id
        put(parseResults, "slaveEMeterTimestamp",             record.getSlaveEMeterTimestamp());             // Slave e-meter measurement timestamp
        put(parseResults, "slaveEMeterkWh",                   record.getSlaveEMeterkWh());                   // Slave e-meter consumption in kWh

        flowFile = session.putAllAttributes(flowFile, parseResults);

        session.getProvenanceReporter().modifyAttributes(flowFile);

        if (record.isValidCRC()) {
            session.transfer(flowFile, VALID);
        } else {
            session.transfer(flowFile, INVALID_CRC);
        }
    }
}
