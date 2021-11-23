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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDsmrParserOnReal {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParserOnReal.class);

    @Test
    void testParseRealTelegram(){
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/ISK5\\2M550T-1012\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(190324150541W)\r\n" +
            "0-0:96.1.1(4530303434303037313331363530363138)\r\n" +
            "1-0:1.8.1(003432.829*kWh)\r\n" +
            "1-0:1.8.2(003224.632*kWh)\r\n" +
            "1-0:2.8.1(000000.000*kWh)\r\n" +
            "1-0:2.8.2(000000.000*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(00.433*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00005)\r\n" +
            "0-0:96.7.9(00003)\r\n" +
            "1-0:99.97.0(1)(0-0:96.7.19)(180417201458S)(0000000236*s)\r\n" +
            "1-0:32.32.0(00001)\r\n" +
            "1-0:52.32.0(00001)\r\n" +
            "1-0:72.32.0(00001)\r\n" +
            "1-0:32.36.0(00001)\r\n" +
            "1-0:52.36.0(00001)\r\n" +
            "1-0:72.36.0(00001)\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:32.7.0(236.7*V)\r\n" +
            "1-0:52.7.0(234.5*V)\r\n" +
            "1-0:72.7.0(236.0*V)\r\n" +
            "1-0:31.7.0(000*A)\r\n" +
            "1-0:51.7.0(000*A)\r\n" +
            "1-0:71.7.0(002*A)\r\n" +
            "1-0:21.7.0(00.045*kW)\r\n" +
            "1-0:41.7.0(00.010*kW)\r\n" +
            "1-0:61.7.0(00.379*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "1-0:42.7.0(00.000*kW)\r\n" +
            "1-0:62.7.0(00.000*kW)\r\n" +
            "!9DF0\r\n");

        // CHECKSTYLE.OFF: ParenPad
        assertEquals("/ISK5\\2M550T-1012", dsmrTelegram.getIdent());
        assertEquals("50", dsmrTelegram.getP1Version());
//        assertEquals("2019-03-24T15:05:41+01:00", dsmrTelegram.getTimestamp());
        assertEquals(ZonedDateTime.parse("2019-03-24T15:05:41+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("E0044007131650618", dsmrTelegram.getEquipmentId());
        assertEquals("", dsmrTelegram.getMessage());

        assertEquals(      1.0, dsmrTelegram.getElectricityTariffIndicator(),      0.001);
        assertEquals( 3432.829, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 3224.632, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(    0.433, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertEquals(        5, dsmrTelegram.getPowerFailures());
        assertEquals(        3, dsmrTelegram.getLongPowerFailures());

        assertEquals(1, dsmrTelegram.getPowerFailureEventLogSize());

        List<PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(1, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0), "2018-04-17T20:11:02+02:00", "2018-04-17T20:14:58+02:00", "PT3M56S");

        assertEquals(        1, dsmrTelegram.getVoltageSagsPhaseL1());
        assertEquals(        1, dsmrTelegram.getVoltageSagsPhaseL2());
        assertEquals(        1, dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(        1, dsmrTelegram.getVoltageSwellsPhaseL1());
        assertEquals(        1, dsmrTelegram.getVoltageSwellsPhaseL2());
        assertEquals(        1, dsmrTelegram.getVoltageSwellsPhaseL3());
        assertEquals(    236.7, dsmrTelegram.getVoltageL1(),       0.001);
        assertEquals(    234.5, dsmrTelegram.getVoltageL2(),       0.001);
        assertEquals(    236.0, dsmrTelegram.getVoltageL3(),       0.001);
        assertEquals(      0.0, dsmrTelegram.getCurrentL1(),       0.001);
        assertEquals(      0.0, dsmrTelegram.getCurrentL2(),       0.001);
        assertEquals(      2.0, dsmrTelegram.getCurrentL3(),       0.001);
        assertEquals(    0.045, dsmrTelegram.getPowerReceivedL1(), 0.001);
        assertEquals(     0.01, dsmrTelegram.getPowerReceivedL2(), 0.001);
        assertEquals(    0.379, dsmrTelegram.getPowerReceivedL3(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL1(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL2(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL3(), 0.001);
        assertEquals(        0, dsmrTelegram.getMBusEvents().size());

        assertTrue(dsmrTelegram.isValidCRC());
        assertEquals("9DF0", dsmrTelegram.getCrc());

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testParseRealTelegram2(){
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/Ene5\\XS210 ESMR 5.0\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(171105201324W)\r\n" +
            "0-0:96.1.1(4530303437303030303037363330383137)\r\n" +
            "1-0:1.8.1(000051.775*kWh)\r\n" +
            "1-0:1.8.2(000000.000*kWh)\r\n" +
            "1-0:2.8.1(000024.413*kWh)\r\n" +
            "1-0:2.8.2(000000.000*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(00.335*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00003)\r\n" +
            "0-0:96.7.9(00001)\r\n" +
            "1-0:99.97.0(0)(0-0:96.7.19)\r\n" +
            "1-0:32.32.0(00002)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:32.7.0(229.0*V)\r\n" +
            "1-0:31.7.0(001*A)\r\n" +
            "1-0:21.7.0(00.335*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "0-1:24.1.0(003)\r\n" +
            "0-1:96.1.0(4730303538353330303031313633323137)\r\n" +
            "0-1:24.2.1(171105201000W)(00016.713*m3)\r\n" +
            "!8F46\r\n"
        );

        assertEquals("/Ene5\\XS210 ESMR 5.0", dsmrTelegram.getIdent());

        assertEquals(0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(0, dsmrTelegram.getPowerFailureEventLog().size());

        LOG.info("{}", dsmrTelegram);
    }

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

        assertEquals("/XMX5LGBBFG1009325446", dsmrTelegram.getIdent());
        assertEquals("42", dsmrTelegram.getP1Version());
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
    void testParseRealTelegramWithSpaceInDeviceName(){
        // From a Enexis/Sagemcom T120D that has a space in the device name
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/Ene5\\SAGEMCOM CX2000-\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(181202060910W)\r\n" +
            "0-0:96.1.1(5354545454313233343530303237333137)\r\n" +
            "1-0:1.8.1(000098.508*kWh)\r\n" +
            "1-0:1.8.2(000000.000*kWh)\r\n" +
            "1-0:2.8.1(000000.917*kWh)\r\n" +
            "1-0:2.8.2(000000.000*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(00.004*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00933)\r\n" +
            "0-0:96.7.9(00108)\r\n" +
            "1-0:99.97.0(10)(0-0:96.7.19)" +
                "(181201030814W)(0000758635*s)" +
                "(180904173923S)(0000001820*s)" +
                "(180707220440S)(0000024288*s)" +
                "(180611204409S)(0002246666*s)" +
                "(180425175248S)(0000002897*s)" +
                "(180420221654S)(0000001068*s)" +
                "(180226203438W)(0000952147*s)" +
                "(180215192356W)(0000268881*s)" +
                "(180209184337W)(0000003307*s)" +
                "(180208200717W)(0000000266*s)\r\n" +
            "1-0:32.32.0(00060)\r\n" +
            "1-0:52.32.0(00044)\r\n" +
            "1-0:72.32.0(00045)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "1-0:52.36.0(00000)\r\n" +
            "1-0:72.36.0(00000)\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:32.7.0(227.0*V)\r\n" +
            "1-0:52.7.0(226.0*V)\r\n" +
            "1-0:72.7.0(228.0*V)\r\n" +
            "1-0:31.7.0(000*A)\r\n" +
            "1-0:51.7.0(000*A)\r\n" +
            "1-0:71.7.0(000*A)\r\n" +
            "1-0:21.7.0(00.004*kW)\r\n" +
            "1-0:41.7.0(00.000*kW)\r\n" +
            "1-0:61.7.0(00.000*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "1-0:42.7.0(00.000*kW)\r\n" +
            "1-0:62.7.0(00.000*kW)\r\n" +
            "!0B43\r\n"
        );

        assertEquals("/Ene5\\SAGEMCOM CX2000-", dsmrTelegram.getIdent());
        assertEquals("50", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2018-12-02T06:09:10+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("STTTT123450027317", dsmrTelegram.getEquipmentId());
        assertEquals("", dsmrTelegram.getMessage());

        assertEquals(        1, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals(   98.508, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(    0.917, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(    0.004, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertEquals(      933, dsmrTelegram.getPowerFailures());
        assertEquals(      108, dsmrTelegram.getLongPowerFailures());
        assertEquals(       60, dsmrTelegram.getVoltageSagsPhaseL1());
        assertEquals(       44, dsmrTelegram.getVoltageSagsPhaseL2());
        assertEquals(       45, dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL1());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL2());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL3());
        assertEquals(    227.0, dsmrTelegram.getVoltageL1());
        assertEquals(    226.0, dsmrTelegram.getVoltageL2());
        assertEquals(    228.0, dsmrTelegram.getVoltageL3());
        assertEquals(      0.0, dsmrTelegram.getCurrentL1(),       0.001);
        assertEquals(      0.0, dsmrTelegram.getCurrentL2());
        assertEquals(      0.0, dsmrTelegram.getCurrentL3());
        assertEquals(    0.004, dsmrTelegram.getPowerReceivedL1(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReceivedL2(),  0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReceivedL3(),  0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL1(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL2(),  0.001);
        assertEquals(      0.0, dsmrTelegram.getPowerReturnedL3(),  0.001);
        assertEquals(        0, dsmrTelegram.getMBusEvents().size());

        assertEquals(10, dsmrTelegram.getPowerFailureEventLogSize());

        List<PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(10, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0), "2018-11-22T08:24:19+01:00", "2018-12-01T03:08:14+01:00", "PT210H43M55S");
        assertPowerFailureEvent(powerFailureEventLog.get(1), "2018-09-04T17:09:03+02:00", "2018-09-04T17:39:23+02:00", "PT30M20S");
        assertPowerFailureEvent(powerFailureEventLog.get(2), "2018-07-07T15:19:52+02:00", "2018-07-07T22:04:40+02:00", "PT6H44M48S");
        assertPowerFailureEvent(powerFailureEventLog.get(3), "2018-05-16T20:39:43+02:00", "2018-06-11T20:44:09+02:00", "PT624H4M26S");
        assertPowerFailureEvent(powerFailureEventLog.get(4), "2018-04-25T17:04:31+02:00", "2018-04-25T17:52:48+02:00", "PT48M17S");
        assertPowerFailureEvent(powerFailureEventLog.get(5), "2018-04-20T21:59:06+02:00", "2018-04-20T22:16:54+02:00", "PT17M48S");
        assertPowerFailureEvent(powerFailureEventLog.get(6), "2018-02-15T20:05:31+01:00", "2018-02-26T20:34:38+01:00", "PT264H29M7S");
        assertPowerFailureEvent(powerFailureEventLog.get(7), "2018-02-12T16:42:35+01:00", "2018-02-15T19:23:56+01:00", "PT74H41M21S");
        assertPowerFailureEvent(powerFailureEventLog.get(8), "2018-02-09T17:48:30+01:00", "2018-02-09T18:43:37+01:00", "PT55M7S");
        assertPowerFailureEvent(powerFailureEventLog.get(9), "2018-02-08T20:02:51+01:00", "2018-02-08T20:07:17+01:00", "PT4M26S");

        assertNull(dsmrTelegram.getGasEquipmentId());

        assertTrue(dsmrTelegram.isValidCRC());
        assertEquals("0B43", dsmrTelegram.getCrc());
        assertTrue(dsmrTelegram.isValid());

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

        assertEquals("/XMX5LGBBFG1009089532", dsmrTelegram.getIdent());
        assertEquals("42", dsmrTelegram.getP1Version());
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

        LOG.info("{}", dsmrTelegram);
    }

    // As reported https://github.com/nielsbasjes/dsmr-tools/issues/54
    @Test
    void testDSMRTelegramIssue54() {
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/ISk5\\2MT382-1003\r\n" +
            "\r\n" +
            "0-0:96.1.1(5A424556303035313036383434393132)\r\n" +
            "1-0:1.8.1(16719.940*kWh)\r\n" +
            "1-0:1.8.2(19403.220*kWh)\r\n" +
            "1-0:2.8.1(00859.681*kWh)\r\n" +
            "1-0:2.8.2(01817.057*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(0000.89*kW)\r\n" +
            "1-0:2.7.0(0000.00*kW)\r\n" +
            "0-0:17.0.0(0999.00*kW)\r\n" +
            "0-0:96.3.10(1)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "0-2:24.1.0(3)\r\n" +
            "0-2:96.1.0(3238303131303038333036343239303133)\r\n" +
            "0-2:24.3.0(211122210000)(00)(60)(1)(0-2:24.2.1)(m3)\r\n" +
            "(13368.864)\r\n" +
            "0-2:24.4.0(1)\r\n" +
            "!\r\n"
        );

        assertEquals("/ISk5\\2MT382-1003", dsmrTelegram.getIdent());
        assertNull(dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("ZBEV005106844912", dsmrTelegram.getEquipmentId());

        assertEquals(        2, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 16719.94, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 19403.22, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(  859.681, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 1817.057, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(     0.89, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("28011008306429013", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2021-11-22T21:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals( 13368.864, dsmrTelegram.getGasM3(), 0.001);

        // There is no CRC
        assertFalse(dsmrTelegram.isValidCRC());

        // Really old record --> is valid.
        assertTrue(dsmrTelegram.isValid());

        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMRTelegramIssue54Extra() {
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/ISk5\\2MT382-1003\r\n" +
            "\n" +
            "0-0:96.1.1(5A424556303035313036383434393132)\r\n" +
            "1-0:1.8.1(16722.627*kWh)\r\n" +
            "1-0:1.8.2(19412.737*kWh)\r\n" +
            "1-0:2.8.1(00859.681*kWh)\r\n" +
            "1-0:2.8.2(01817.110*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(0000.67*kW)\r\n" +
            "1-0:2.7.0(0000.00*kW)\r\n" +
            "0-0:17.0.0(0999.00*kW)\r\n" +
            "0-0:96.3.10(1)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "0-2:24.1.0(3)\r\n" +
            "0-2:96.1.0(3238303131303038333036343239303133)\r\n" +
            "0-2:24.3.0(211123200000)(00)(60)(1)(0-2:24.2.1)(m3)\r\n" +
            "(13376.292)\r\n" +
            "0-2:24.4.0(1)\r\n" +
            "!\r\n"
        );

        assertEquals("/ISk5\\2MT382-1003", dsmrTelegram.getIdent());
        assertNull(dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("ZBEV005106844912", dsmrTelegram.getEquipmentId());

        assertEquals(        2, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals(16722.627, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals(19412.737, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(  859.681, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 1817.110, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(     0.67, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("28011008306429013", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2021-11-23T20:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals( 13376.292, dsmrTelegram.getGasM3(), 0.001);

        // There is no CRC
        assertFalse(dsmrTelegram.isValidCRC());

        // Really old record --> is valid.
        assertTrue(dsmrTelegram.isValid());

        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMRTelegramOld1() {
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/XMX5XMXABCE100103855\r\n" +
            "\r\n" +
            "0-0:96.1.1(30313233343536373839)\r\n" +
            "1-0:1.8.1(03687.771*kWh)\r\n" +
            "1-0:1.8.2(04456.167*kWh)\r\n" +
            "1-0:2.8.1(01450.360*kWh)\r\n" +
            "1-0:2.8.2(03098.554*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(0000.00*kW)\r\n" +
            "1-0:2.7.0(0000.62*kW)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "0-1:96.1.0(30313233343536373839)\r\n" +
            "0-1:24.1.0(03)\r\n" +
            "0-1:24.3.0(190218120000)(00)(60)(1)(0-1:24.2.0)(m3)\r\n" +
            "(05271.144)\r\n" +
            "!\r\n");

        assertEquals("/XMX5XMXABCE100103855", dsmrTelegram.getIdent());
        assertNull(dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("0123456789", dsmrTelegram.getEquipmentId());

        assertEquals(        2, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 3687.771, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 4456.167, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals( 1450.360, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 3098.554, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(     0.62, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("0123456789", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2019-02-18T12:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals( 5271.144, dsmrTelegram.getGasM3(), 0.001);

        // There is no CRC
        assertFalse(dsmrTelegram.isValidCRC());

        // Really old record --> is valid.
        assertTrue(dsmrTelegram.isValid());

        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMRTelegramOld2() {
        // From https://www.domoticz.com/forum/viewtopic.php?t=10020
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/KMP5 ZABF001551772711\r\n" +
            "\r\n" +
            "0-0:96.1.1(205A4142463030313531373732373131)\r\n" +
            "1-0:1.8.1(05080.475*kWh)\r\n" +
            "1-0:1.8.2(05008.385*kWh)\r\n" +
            "1-0:2.8.1(00000.000*kWh)\r\n" +
            "1-0:2.8.2(00000.000*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(0000.68*kW)\r\n" +
            "1-0:2.7.0(0000.00*kW)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "0-2:24.1.0(3)\r\n" +
            "0-2:96.1.0(3238313031353431303037313632343132)\r\n" +
            "0-2:24.3.0(160117120000)(00)(60)(1)(0-2:24.2.1)(m3)\r\n" +
            "(02025.003)\r\n" +
            "!\r\n");

        assertEquals("/KMP5 ZABF001551772711", dsmrTelegram.getIdent());
        assertNull(dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        // Yes, with the space in front of it ...
        assertEquals(" ZABF00151772711", dsmrTelegram.getEquipmentId());

        assertEquals(        1, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 5080.475, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 5008.385, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(     0.68, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("28101541007162412", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2016-01-17T12:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals( 2025.003, dsmrTelegram.getGasM3(), 0.001);

        // There is no CRC
        assertFalse(dsmrTelegram.isValidCRC());

        // Really old record --> is valid.
        assertTrue(dsmrTelegram.isValid());

        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMRTelegramOld3() {
        // From https://github.com/gejanssen/slimmemeter-rpi/blob/master/README.md
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/KMP5 KA6U001660297912\r\n" +
            "\r\n" +
            "0-0:96.1.1(204B413655303031363630323937393132)\r\n" +
            "1-0:1.8.1(07041.882*kWh)\r\n" +
            "1-0:1.8.2(02351.565*kWh)\r\n" +
            "1-0:2.8.1(03125.397*kWh)\r\n" +
            "1-0:2.8.2(07729.108*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(0000.54*kW)\r\n" +
            "1-0:2.7.0(0000.00*kW)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "0-1:24.1.0(3)\r\n" +
            "0-1:96.1.0(3238313031353431303034303232323131)\r\n" +
            "0-1:24.3.0(160315210000)(00)(60)(1)(0-1:24.2.1)(m3)\r\n" +
            "(04083.631)\r\n" +
            "!\r\n");

        assertEquals("/KMP5 KA6U001660297912", dsmrTelegram.getIdent());
        assertNull(dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals(" KA6U001660297912", dsmrTelegram.getEquipmentId());

        assertEquals(        1, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 7041.882, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 2351.565, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals( 3125.397, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 7729.108, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(     0.54, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("28101541004022211", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2016-03-15T21:00:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals( 4083.631, dsmrTelegram.getGasM3(), 0.001);

        // There is no CRC
        assertFalse(dsmrTelegram.isValidCRC());

        // Really old record --> is valid.
        assertTrue(dsmrTelegram.isValid());

        LOG.info("{}", dsmrTelegram);
    }


    @Test
    void testDSMRTelegramOld4() {
        // From http://domoticx.com/p1-poort-slimme-meter-hardware/
        // Only replaced the invalid device ids (both were invalid)
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/KMP5 ZABF001587315111\r\n" +
            "0-0:96.1.1(5A424556303035303931323037363132)\r\n" +
            "1-0:1.8.1(00185.000*kWh)\r\n" +
            "1-0:1.8.2(00084.000*kWh)\r\n" +
            "1-0:2.8.1(00013.000*kWh)\r\n" +
            "1-0:2.8.2(00019.000*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(0000.98*kW)\r\n" +
            "1-0:2.7.0(0000.00*kW)\r\n" +
            "0-0:17.0.0(999*A)\r\n" +
            "0-0:96.3.10(1)\r\n" +
            "0-0:96.13.1()\r\n" +
            "0-0:96.13.0()\r\n" +
            "0-1:24.1.0(3)\r\n" +
            "0-1:96.1.0(32383130314536313730383830337131)\r\n" +
            "0-1:24.3.0(120517020000)(08)(60)(1)(0-1:24.2.1)(m3)\r\n" +
            "(00124.477)\r\n" +
            "0-1:24.4.0(1)\r\n" +
            "!\r\n");

        assertEquals("/KMP5 ZABF001587315111", dsmrTelegram.getIdent());
        assertNull(dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("ZBEV005091207612", dsmrTelegram.getEquipmentId());

        assertEquals(        1, dsmrTelegram.getElectricityTariffIndicator());
        assertEquals(    185.0, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals(     84.0, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(     13.0, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(     19.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(     0.98, dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityPowerReturned(),        0.001);

        assertEquals(        0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(        0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(        1, dsmrTelegram.getMBusEvents().size());
        assertEquals("28101E61708803q1", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2012-05-17T02:00+02:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals(  124.477, dsmrTelegram.getGasM3(), 0.001);

        // There is no CRC
        assertFalse(dsmrTelegram.isValidCRC());

        // Really old record --> is valid.
        assertTrue(dsmrTelegram.isValid());

        LOG.info("{}", dsmrTelegram);
    }

}
