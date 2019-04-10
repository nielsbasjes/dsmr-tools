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

package nl.basjes.dsmr.parse;

import lombok.Getter;
import lombok.ToString;
import nl.basjes.dsmr.parse.DsmrParser.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.basjes.dsmr.parse.DsmrParser.*;

public class ParseDsmrTelegram extends DsmrBaseVisitor<Void> {

    @Getter
    @ToString
    public static class MBusEvent {
        private String deviceType;        // MBus event: Device type.
        private String equipmentId;       // MBus event: Equipment Identifier.
        private Double value;             // MBus event: Last 5 minute reading (the value).
        private String unit;              // MBus event: Last 5 minute reading (the unit: m3 or GJ).
        private ZonedDateTime timestamp;  // MBus event: Timestamp of last 5 minute reading.
    }

    @Getter
    @ToString
    public static class DSMRTelegram {
        //        private String telegram;
        private boolean isValidCRC = false;
        private String ident;
        private String crc;

        private String p1Version;                        // P1 Version information
        private ZonedDateTime timestamp;                        // Timestamp
        private String equipmentId;                      // Equipment identifier
        private Double electricityReceivedLowTariff;     // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
        private Double electricityReceivedNormalTariff;  // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
        private Double electricityReturnedLowTariff;     // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
        private Double electricityReturnedNormalTariff;  // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
        private Double electricityTariffIndicator;       // Tariff indicator electricity
        private Double electricityPowerReceived;         // Actual electricity power delivered (+P) in 1 Watt resolution
        private Double electricityPowerReturned;         // Actual electricity power received (-P) in 1 Watt resolution
        private Long powerFailures;                    // Number of power failures in any phases
        private Long longPowerFailures;                // Number of long power failures in any phases
//        private double powerFailureEventLog;             // Power failure event log
        private Long voltageSagsPhaseL1;               // Number of voltage sags in phase L1
        private Long voltageSagsPhaseL2;               // Number of voltage sags in phase L2
        private Long voltageSagsPhaseL3;               // Number of voltage sags in phase L3
        private Long voltageSwellsPhaseL1;             // Number of voltage swells in phase L1
        private Long voltageSwellsPhaseL2;             // Number of voltage swells in phase L2
        private Long voltageSwellsPhaseL3;             // Number of voltage swells in phase L3
        private Double voltageL1;                        // Instantaneous voltage L1
        private Double voltageL2;                        // Instantaneous voltage L2
        private Double voltageL3;                        // Instantaneous voltage L3
        private Double currentL1;                        // Instantaneous current L1
        private Double currentL2;                        // Instantaneous current L2
        private Double currentL3;                        // Instantaneous current L3
        private Double powerReceivedL1;                  // Instantaneous active power L1 (+P)
        private Double powerReceivedL2;                  // Instantaneous active power L2 (+P)
        private Double powerReceivedL3;                  // Instantaneous active power L3 (+P)
        private Double powerReturnedL1;                  // Instantaneous active power L1 (-P)
        private Double powerReturnedL2;                  // Instantaneous active power L2 (-P)
        private Double powerReturnedL3;                  // Instantaneous active power L3 (-P)
        private String message;                          // Text message max 1024 characters.

        private Map<Integer, MBusEvent> mBusEvents = new TreeMap<>();
    }

    public static synchronized DSMRTelegram parse(String telegram) {
        return new ParseDsmrTelegram(telegram).parse();
    }

    private String telegramString;
    private DSMRTelegram dsmrTelegram;
    private TimestampParser timestampParser = new TimestampParser();

    private ParseDsmrTelegram(String telegram) {
        telegramString = telegram;
        dsmrTelegram = new DSMRTelegram();
        dsmrTelegram.isValidCRC = CheckCRC.crcIsValid(telegramString);
    }

    private DSMRTelegram parse() {
        CodePointCharStream input = CharStreams.fromString(telegramString);
        DsmrLexer lexer = new DsmrLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        DsmrParser parser = new DsmrParser(tokens);

//        if (!verbose) {
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
//        }
//        lexer.addErrorListener(PARSER);
//        parser.addErrorListener(PARSER);

        TelegramContext telegramContext = parser.telegram();

        this.visitTelegram(telegramContext);

        return dsmrTelegram;
    }

    // https://stackoverflow.com/questions/50712987/hex-string-to-byte-array-conversion-java
    public byte[] hexStringToByteArray(String s) {
        byte[] data = new byte[s.length()/2];
        for (int i = 0; i < data.length; i ++) {
            data[i] = (byte) ((Character.digit(s.charAt(i*2), 16) << 4)
                + Character.digit(s.charAt(i*2 + 1), 16));
        }
        return data;
    }

    private String hexStringToString(String hexString) {
        return new String(hexStringToByteArray(hexString), UTF_8);
    }

    @Override
    public Void visitTelegram(TelegramContext ctx) {
        dsmrTelegram.ident = ctx.ident.getText();
        dsmrTelegram.crc = ctx.crc.getText().substring(1); // Skip the '!' at the start
        return visitChildren(ctx);
    }

    @Override
    public Void visitP1Version   (P1VersionContext   ctx) {
        dsmrTelegram.p1Version   = ctx.version.getText();
        return null;
    }

    @Override
    public Void visitTimestamp   (TimestampContext   ctx) {
        dsmrTelegram.timestamp   = timestampParser.parse(ctx.timestamp.getText());
        return null;
    }

    @Override
    public Void visitEquipmentId (EquipmentIdContext ctx) {
        dsmrTelegram.equipmentId = hexStringToString(ctx.id.getText());
        return null;
    }
    @Override
    public Void visitMessage     (MessageContext     ctx) {
        // Text message max 1024 characters.
        dsmrTelegram.message     = (ctx.text == null) ? "" : hexStringToString(ctx.text.getText());
        return null;
    }

    @Override public Void visitElectricityReceivedLowTariff     (ElectricityReceivedLowTariffContext     ctx) { dsmrTelegram.electricityReceivedLowTariff    = Double.valueOf(ctx.value.getText()); return null; } // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
    @Override public Void visitElectricityReceivedNormalTariff  (ElectricityReceivedNormalTariffContext  ctx) { dsmrTelegram.electricityReceivedNormalTariff = Double.valueOf(ctx.value.getText()); return null; } // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
    @Override public Void visitElectricityReturnedLowTariff     (ElectricityReturnedLowTariffContext     ctx) { dsmrTelegram.electricityReturnedLowTariff    = Double.valueOf(ctx.value.getText()); return null; } // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
    @Override public Void visitElectricityReturnedNormalTariff  (ElectricityReturnedNormalTariffContext  ctx) { dsmrTelegram.electricityReturnedNormalTariff = Double.valueOf(ctx.value.getText()); return null; } // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
    @Override public Void visitElectricityTariffIndicator       (ElectricityTariffIndicatorContext       ctx) { dsmrTelegram.electricityTariffIndicator      = Double.valueOf(ctx.value.getText()); return null; } // Tariff indicator electricity
    @Override public Void visitElectricityPowerReceived         (ElectricityPowerReceivedContext         ctx) { dsmrTelegram.electricityPowerReceived        = Double.valueOf(ctx.value.getText()); return null; } // Actual electricity power delivered (+P) in 1 Watt resolution
    @Override public Void visitElectricityPowerReturned         (ElectricityPowerReturnedContext         ctx) { dsmrTelegram.electricityPowerReturned        = Double.valueOf(ctx.value.getText()); return null; } // Actual electricity power received (-P) in 1 Watt resolution

    @Override public Void visitPowerFailures                    (PowerFailuresContext                    ctx) { dsmrTelegram.powerFailures                   = Long.valueOf(ctx.count.getText());   return null; } // Number of power failures in any phases
    @Override public Void visitLongPowerFailures                (LongPowerFailuresContext                ctx) { dsmrTelegram.longPowerFailures               = Long.valueOf(ctx.count.getText());   return null; } // Number of long power failures in any phases

    // TODO: Implement @Override public Void visitPowerFailureEventLog             (PowerFailureEventLogContext             ctx) { return null ; } // Power failure event log

    @Override public Void visitVoltageSagsPhaseL1               (VoltageSagsPhaseL1Context               ctx) { dsmrTelegram.voltageSagsPhaseL1              = Long.valueOf(ctx.count.getText());   return null; } // Number of voltage sags in phase L1
    @Override public Void visitVoltageSagsPhaseL2               (VoltageSagsPhaseL2Context               ctx) { dsmrTelegram.voltageSagsPhaseL2              = Long.valueOf(ctx.count.getText());   return null; } // Number of voltage sags in phase L2
    @Override public Void visitVoltageSagsPhaseL3               (VoltageSagsPhaseL3Context               ctx) { dsmrTelegram.voltageSagsPhaseL3              = Long.valueOf(ctx.count.getText());   return null; } // Number of voltage sags in phase L3
    @Override public Void visitVoltageSwellsPhaseL1             (VoltageSwellsPhaseL1Context             ctx) { dsmrTelegram.voltageSwellsPhaseL1            = Long.valueOf(ctx.count.getText());   return null; } // Number of voltage swells in phase L1
    @Override public Void visitVoltageSwellsPhaseL2             (VoltageSwellsPhaseL2Context             ctx) { dsmrTelegram.voltageSwellsPhaseL2            = Long.valueOf(ctx.count.getText());   return null; } // Number of voltage swells in phase L2
    @Override public Void visitVoltageSwellsPhaseL3             (VoltageSwellsPhaseL3Context             ctx) { dsmrTelegram.voltageSwellsPhaseL3            = Long.valueOf(ctx.count.getText());   return null; } // Number of voltage swells in phase L3
    @Override public Void visitVoltageL1                        (VoltageL1Context                        ctx) { dsmrTelegram.voltageL1                       = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous voltage L1
    @Override public Void visitVoltageL2                        (VoltageL2Context                        ctx) { dsmrTelegram.voltageL2                       = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous voltage L2
    @Override public Void visitVoltageL3                        (VoltageL3Context                        ctx) { dsmrTelegram.voltageL3                       = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous voltage L3
    @Override public Void visitCurrentL1                        (CurrentL1Context                        ctx) { dsmrTelegram.currentL1                       = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous current L1
    @Override public Void visitCurrentL2                        (CurrentL2Context                        ctx) { dsmrTelegram.currentL2                       = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous current L2
    @Override public Void visitCurrentL3                        (CurrentL3Context                        ctx) { dsmrTelegram.currentL3                       = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous current L3
    @Override public Void visitPowerReceivedL1                  (PowerReceivedL1Context                  ctx) { dsmrTelegram.powerReceivedL1                 = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous active power L1 (+P)
    @Override public Void visitPowerReceivedL2                  (PowerReceivedL2Context                  ctx) { dsmrTelegram.powerReceivedL2                 = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous active power L2 (+P)
    @Override public Void visitPowerReceivedL3                  (PowerReceivedL3Context                  ctx) { dsmrTelegram.powerReceivedL3                 = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous active power L3 (+P)
    @Override public Void visitPowerReturnedL1                  (PowerReturnedL1Context                  ctx) { dsmrTelegram.powerReturnedL1                 = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous active power L1 (-P)
    @Override public Void visitPowerReturnedL2                  (PowerReturnedL2Context                  ctx) { dsmrTelegram.powerReturnedL2                 = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous active power L2 (-P)
    @Override public Void visitPowerReturnedL3                  (PowerReturnedL3Context                  ctx) { dsmrTelegram.powerReturnedL3                 = Double.valueOf(ctx.value.getText()); return null; } // Instantaneous active power L3 (-P)


    private MBusEvent getMBusEvent(int index) {
        return dsmrTelegram.mBusEvents.computeIfAbsent(index, i -> new MBusEvent());
    }

    private void setMBusType(int index, String type) {
        MBusEvent mBusEvent = getMBusEvent(index);
        mBusEvent.deviceType = type;
    }

    @Override public Void visitMBus1Type(MBus1TypeContext ctx) { setMBusType(1, ctx.type.getText()); return null; }
    @Override public Void visitMBus2Type(MBus2TypeContext ctx) { setMBusType(2, ctx.type.getText()); return null; }
    @Override public Void visitMBus3Type(MBus3TypeContext ctx) { setMBusType(3, ctx.type.getText()); return null; }
    @Override public Void visitMBus4Type(MBus4TypeContext ctx) { setMBusType(4, ctx.type.getText()); return null; }

    private void setMBusEquipmentId(int index, String equipmentId) {
        MBusEvent mBusEvent = getMBusEvent(index);
        mBusEvent.equipmentId = hexStringToString(equipmentId);
    }

    @Override public Void visitMBus1EquipmentId(MBus1EquipmentIdContext ctx) { setMBusEquipmentId(1, ctx.id.getText()); return null; }
    @Override public Void visitMBus2EquipmentId(MBus2EquipmentIdContext ctx) { setMBusEquipmentId(2, ctx.id.getText()); return null; }
    @Override public Void visitMBus3EquipmentId(MBus3EquipmentIdContext ctx) { setMBusEquipmentId(3, ctx.id.getText()); return null; }
    @Override public Void visitMBus4EquipmentId(MBus4EquipmentIdContext ctx) { setMBusEquipmentId(4, ctx.id.getText()); return null; }

    private void setMBusUsage(int index, String timestampText, String value, String unit) {
        MBusEvent mBusEvent = getMBusEvent(index);
        mBusEvent.timestamp = timestampParser.parse(timestampText);
        mBusEvent.value = Double.valueOf(value);
        mBusEvent.unit = unit;
    }

    @Override public Void visitMBus1Usage(MBus1UsageContext ctx) { setMBusUsage(1, ctx.timestamp.getText(), ctx.value.getText(), ctx.unit.getText()); return null; }
    @Override public Void visitMBus2Usage(MBus2UsageContext ctx) { setMBusUsage(2, ctx.timestamp.getText(), ctx.value.getText(), ctx.unit.getText()); return null; }
    @Override public Void visitMBus3Usage(MBus3UsageContext ctx) { setMBusUsage(3, ctx.timestamp.getText(), ctx.value.getText(), ctx.unit.getText()); return null; }
    @Override public Void visitMBus4Usage(MBus4UsageContext ctx) { setMBusUsage(4, ctx.timestamp.getText(), ctx.value.getText(), ctx.unit.getText()); return null; }
}
