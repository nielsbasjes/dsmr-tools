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

import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.DSMRTelegram.PowerFailureEvent;
import nl.basjes.dsmr.ParseDsmrTelegram;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;

import static nl.basjes.dsmr.parse.Utils.assertPowerFailureEvent;
import static nl.basjes.dsmr.parse.Utils.checkMbus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CHECKSTYLE.OFF: ParenPad
class TestDsmrParserOnRealDSMR4 {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParserOnRealDSMR4.class);

    @Test
    void testParseRealTelegramWithGas(){
        // From a Landis+Gyr E350 that also reports about the connected gas meter.
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/XMX5LGBBFG1009325446\r\n" +
            "\r\n" +
            "1-3:0.2.8(42)\r\n" +
            "0-0:1.0.0(190905214003S)\r\n" +
            "0-0:96.1.1(4530303331303033323339343536373136)\r\n" +
            "1-0:1.8.1(003235.689*kWh)\r\n" +
            "1-0:1.8.2(006777.240*kWh)\r\n" +
            "1-0:2.8.1(000000.313*kWh)\r\n" +
            "1-0:2.8.2(000000.000*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(00.374*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00003)\r\n" +
            "0-0:96.7.9(00001)\r\n" +
            "1-0:99.97.0(1)(0-0:96.7.19)(170117065912W)(0000009606*s)\r\n" +
            "1-0:32.32.0(00000)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:31.7.0(002*A)\r\n" +
            "1-0:21.7.0(00.374*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "0-1:24.1.0(003)\r\n" +
            "0-1:96.1.0(4730303139333430333135333730363136)\r\n" +
            "0-1:24.2.1(190905210000S)(01091.352*m3)\r\n" +
            "!BB2A\r\n"
        );

        assertEquals("/XMX5LGBBFG1009325446", dsmrTelegram.getRawIdent());
        assertEquals("XMX", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("LGBBFG1009325446", dsmrTelegram.getIdent());

        assertEquals("4.2", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2019-09-05T21:40:03+02:00"), dsmrTelegram.getTimestamp());

        assertEquals("E0031003239456716", dsmrTelegram.getEquipmentId());
        assertEquals("", dsmrTelegram.getMessage());

        assertEquals(      2,   dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 3235.689, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 6777.240, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(    0.313, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(    0.374, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertEquals(        3, dsmrTelegram.getPowerFailures());

        assertEquals(        1, dsmrTelegram.getPowerFailureEventLogSize());

        List<PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(1, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0), "2017-01-17T04:19:06+01:00", "2017-01-17T06:59:12+01:00", "PT2H40M6S");

        assertEquals(        1, dsmrTelegram.getLongPowerFailures());
        assertEquals(        0, dsmrTelegram.getVoltageSagsPhaseL1());
        assertNull(             dsmrTelegram.getVoltageSagsPhaseL2());
        assertNull(             dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL1());
        assertNull(             dsmrTelegram.getVoltageSwellsPhaseL2());
        assertNull(             dsmrTelegram.getVoltageSwellsPhaseL3());
        assertNull(             dsmrTelegram.getVoltageL1());
        assertNull(             dsmrTelegram.getVoltageL2());
        assertNull(             dsmrTelegram.getVoltageL3());
        assertEquals(      2.0, dsmrTelegram.getCurrentL1(),       0.001);
        assertNull(             dsmrTelegram.getCurrentL2());
        assertNull(             dsmrTelegram.getCurrentL3());
        assertEquals(    0.374, dsmrTelegram.getPowerReceivedL1(), 0.001);
        assertNull(             dsmrTelegram.getPowerReceivedL2());
        assertNull(             dsmrTelegram.getPowerReceivedL3());
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL1(), 0.001);
        assertNull(             dsmrTelegram.getPowerReturnedL2());
        assertNull(             dsmrTelegram.getPowerReturnedL3());
        assertEquals(        1, dsmrTelegram.getMBusEvents().size());

        checkMbus(dsmrTelegram, 1, "2019-09-05T21:00:00+02:00", 3, "G0019340315370616", 1091.352, "m3");

        assertEquals("G0019340315370616", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2019-09-05T21:00:00+02:00"), dsmrTelegram.getGasTimestamp());
        assertEquals( 1091.352, dsmrTelegram.getGasM3(), 0.001);

        assertTrue(dsmrTelegram.isValidCRC());
        assertEquals("BB2A", dsmrTelegram.getCrc());

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMR42Telegram(){
        // Output of a Landis+Gyr E350 (DSMR 4.2)
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/XMX5LGBBFG1009089532\r\n" +
            "\r\n" +
            "1-3:0.2.8(42)\r\n" +
            "0-0:1.0.0(000101010000W)\r\n" +
            "0-0:96.1.1(4530303330303033313131393539373135)\r\n" +
            "1-0:1.8.1(000024.487*kWh)\r\n" +
            "1-0:1.8.2(000030.536*kWh)\r\n" +
            "1-0:2.8.1(000000.528*kWh)\r\n" +
            "1-0:2.8.2(000000.000*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(00.000*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00116)\r\n" +
            "0-0:96.7.9(00000)\r\n" +
            "1-0:99.97.0(0)(0-0:96.7.19)\r\n" +
            "1-0:32.32.0(00011)\r\n" +
            "1-0:52.32.0(00028)\r\n" +
            "1-0:72.32.0(00028)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "1-0:52.36.0(00000)\r\n" +
            "1-0:72.36.0(00000)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:31.7.0(000*A)\r\n" +
            "1-0:51.7.0(000*A)\r\n" +
            "1-0:71.7.0(000*A)\r\n" +
            "1-0:21.7.0(00.004*kW)\r\n" +
            "1-0:41.7.0(00.000*kW)\r\n" +
            "1-0:61.7.0(00.000*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "1-0:42.7.0(00.000*kW)\r\n" +
            "1-0:62.7.0(00.000*kW)\r\n" +
            "0-1:24.1.0(003)\r\n" +
            "0-1:96.1.0(4730303032333430313537323637333134)\r\n" +
            "0-1:24.2.1(000101010000W)(0000000000)\r\n" +
            "!6D3C\r\n"
        );

        assertEquals("/XMX5LGBBFG1009089532", dsmrTelegram.getRawIdent());
        assertEquals("XMX", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("LGBBFG1009089532", dsmrTelegram.getIdent());

        assertEquals("4.2", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2000-01-01T01:00+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("E0030003111959715", dsmrTelegram.getEquipmentId());
        assertEquals("", dsmrTelegram.getMessage());

        assertEquals(        2, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals(   24.487, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals(   30.536, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(    0.528, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertEquals(      116, dsmrTelegram.getPowerFailures());
        assertEquals(        0, dsmrTelegram.getLongPowerFailures());
        assertEquals(       11, dsmrTelegram.getVoltageSagsPhaseL1());
        assertEquals(       28, dsmrTelegram.getVoltageSagsPhaseL2());
        assertEquals(       28, dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL1());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL2());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL3());
        assertNull(dsmrTelegram.getVoltageL1());
        assertNull(dsmrTelegram.getVoltageL2());
        assertNull(dsmrTelegram.getVoltageL3());
        assertEquals(      0.0, dsmrTelegram.getCurrentL1(),       0.001);
        assertEquals(      0.0, dsmrTelegram.getCurrentL2());
        assertEquals(      0.0, dsmrTelegram.getCurrentL3());
        assertEquals(    0.004, dsmrTelegram.getPowerReceivedL1(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReceivedL2(),  0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReceivedL3(),  0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL1(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL2(),  0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL3(),  0.001);
        assertEquals(        1, dsmrTelegram.getMBusEvents().size());

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals("G0002340157267314", dsmrTelegram.getGasEquipmentId());

        assertTrue(dsmrTelegram.isValidCRC());
        assertEquals("6D3C", dsmrTelegram.getCrc());
        assertTrue(dsmrTelegram.isValid());

//        LOG.info("{}", dsmrTelegram);
    }


    @Test
    void testDSMRTelegramKFM() {
        // From https://github.com/svrooij/smartmeter2mqtt#output---raw-tcp-socket
        // Changed the equipmentId (and CRC) to be parsable
        // This record is obviously constructed/manipulated ... like a power failure that started in 1931.
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/KFM5KAIFA-METER\r\n" +
            "\r\n" +
            "1-3:0.2.8(42)\r\n" +
            "0-0:1.0.0(200410102433S)\r\n" +
            "0-0:96.1.1(4530313233343536373839)\r\n" +
            "1-0:1.8.1(003000.497*kWh)\r\n" +
            "1-0:1.8.2(001000.248*kWh)\r\n" +
            "1-0:2.8.1(001000.458*kWh)\r\n" +
            "1-0:2.8.2(003000.394*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(00.105*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00000)\r\n" +
            "0-0:96.7.9(00000)\r\n" +
            "1-0:99.97.0(1)(0-0:96.7.19)(000101000001W)(2147483647*s)\r\n" +
            "1-0:32.32.0(00000)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:31.7.0(001*A)\r\n" +
            "1-0:21.7.0(00.105*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "0-2:24.1.0(003)\r\n" +
            "0-2:96.1.0(4530313233343536373839)\r\n" +
            "0-2:24.2.1(200410100000S)(02000.671*m3)\r\n" +
            "!DE3E\r\n");

        assertEquals("/KFM5KAIFA-METER", dsmrTelegram.getRawIdent());
        assertEquals("KFM", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("KAIFA-METER", dsmrTelegram.getIdent());

        assertEquals("4.2", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2020-04-10T10:24:33+02:00"), dsmrTelegram.getTimestamp());

        assertEquals("E0123456789", dsmrTelegram.getEquipmentId());

        assertEquals(        2, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 3000.497, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 1000.248, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals( 1000.458, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 3000.394, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(    0.105, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        1, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        1, dsmrTelegram.getPowerFailureEventLog().size());

        List<PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(1, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0),
            "1931-12-13T20:45:54+01:00", "2000-01-01T00:00:01+01:00", "PT596523H14M7S");

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("E0123456789", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2020-04-10T10:00+02:00"), dsmrTelegram.getGasTimestamp());
        assertEquals( 2000.671, dsmrTelegram.getGasM3(), 0.001);

        assertTrue(dsmrTelegram.isValidCRC());
        assertEquals("DE3E", dsmrTelegram.getCrc());
        assertTrue(dsmrTelegram.isValid());

//        LOG.info("{}", dsmrTelegram);
    }

}
