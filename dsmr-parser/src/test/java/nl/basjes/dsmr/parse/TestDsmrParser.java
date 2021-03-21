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
import nl.basjes.dsmr.ParseDsmrTelegram;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDsmrParser {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParser.class);

    // This is the example testcase documented in the specification.
    // https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_a727fce1f1.pdf
    //
    // The example telegram below is based on:
    // - DSMR version 5.0 (value 50 behind OBIS code 1-3:0.2.8.255)
    // - It is send at 2010, December 9th, 11h30m20s
    // - Gas value of 2010, December 9th, 11:25h is presented
    // - 4 power failures in any phase
    // - 2 long power failure in any phase
    // - Power Failure Event log:
    //   Failure at 2010, December 8th, 15h20m15s, duration 240 seconds
    //   Failure at 2010, December 8th, 15h05m03s, duration 301 seconds
    // - 2 voltage sags in phase L1
    // - 1 voltage sag in phase L2 (poly phase meters only)
    // - 0 voltage sags in phase L3 (poly phase meters only)
    // - 0 voltage swells in phase L1
    // - 3 voltage swells in phase L2 (poly phase meters only)
    // - 0 voltage swells in phase L3 (poly phase meters only)
    // - Only one M-Bus device is connected to the Electricity meter.
    //   The register value of the Gas meter is 12785,123 m3.
    //   This value is captured by the G meter at 2010, December 9th,at 11h25m00s Wintertime.
    // - Instantaneous voltage per phase
    // - Instantaneous current per phase
    // - Instantaneous active power (+P) per phase
    // - Instantaneous active power (-P) per phase

    @Test
    void testParseTestcaseFromSpecification(){
        String testcase = "\r\n" +
            "/ISk5\\2MT382-1000\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(101209113020W)\r\n" +
            "0-0:96.1.1(4B384547303034303436333935353037)\r\n" +
            "1-0:1.8.1(123456.789*kWh)\r\n" +
            "1-0:1.8.2(123456.789*kWh)\r\n" +
            "1-0:2.8.1(123456.789*kWh)\r\n" +
            "1-0:2.8.2(123456.789*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(01.193*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00004)\r\n" +
            "0-0:96.7.9(00002)\r\n" +
            "1-0:99.97.0(2)(0-0:96.7.19)(101208152415W)(0000000240*s)(101208151004W)(0000000301*s)\r\n" +
            "1-0:32.32.0(00002)\r\n" +
            "1-0:52.32.0(00001)\r\n" +
            "1-0:72.32.0(00000)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "1-0:52.36.0(00003)\r\n" +
            "1-0:72.36.0(00000)\r\n" +
            "0-0:96.13.0(303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F30313233343536373839" +
                        "3A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F)\r\n" +
            "1-0:32.7.0(220.1*V)\r\n" +
            "1-0:52.7.0(220.2*V)\r\n" +
            "1-0:72.7.0(220.3*V)\r\n" +
            "1-0:31.7.0(001*A)\r\n" +
            "1-0:51.7.0(002*A)\r\n" +
            "1-0:71.7.0(003*A)\r\n" +
            "1-0:21.7.0(01.111*kW)\r\n" +
            "1-0:41.7.0(02.222*kW)\r\n" +
            "1-0:61.7.0(03.333*kW)\r\n" +
            "1-0:22.7.0(04.444*kW)\r\n" +
            "1-0:42.7.0(05.555*kW)\r\n" +
            "1-0:62.7.0(06.666*kW)\r\n" +
            "0-1:24.1.0(003)\r\n" +
            "0-1:96.1.0(3232323241424344313233343536373839)\r\n" +
            "0-1:24.2.1(101209112500W)(12785.123*m3)\r\n" +
            "!EF2F\r\n" +
            "\r\n";
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);

        // CHECKSTYLE.OFF: ParenPad
        assertEquals("/ISk5\\2MT382-1000", dsmrTelegram.getIdent());
        assertEquals("50", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2010-12-09T11:30:20+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("K8EG004046395507", dsmrTelegram.getEquipmentId());
        assertEquals("0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?", dsmrTelegram.getMessage());

        assertEquals(      2,      dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(      1.193,  dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0,    dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertEquals(      4,      dsmrTelegram.getPowerFailures().longValue());
        assertEquals(      2,      dsmrTelegram.getLongPowerFailures().longValue());
        assertEquals(      2,      dsmrTelegram.getVoltageSagsPhaseL1().longValue());
        assertEquals(      1,      dsmrTelegram.getVoltageSagsPhaseL2().longValue());
        assertEquals(      0,      dsmrTelegram.getVoltageSagsPhaseL3().longValue());
        assertEquals(      0,      dsmrTelegram.getVoltageSwellsPhaseL1().longValue());
        assertEquals(      3,      dsmrTelegram.getVoltageSwellsPhaseL2().longValue());
        assertEquals(      0,      dsmrTelegram.getVoltageSwellsPhaseL3().longValue());
        assertEquals(    220.1,    dsmrTelegram.getVoltageL1(),       0.001);
        assertEquals(    220.2,    dsmrTelegram.getVoltageL2(),       0.001);
        assertEquals(    220.3,    dsmrTelegram.getVoltageL3(),       0.001);
        assertEquals(      1,      dsmrTelegram.getCurrentL1(),       0.001);
        assertEquals(      2,      dsmrTelegram.getCurrentL2(),       0.001);
        assertEquals(      3,      dsmrTelegram.getCurrentL3(),       0.001);
        assertEquals(      1.111,  dsmrTelegram.getPowerReceivedL1(), 0.001);
        assertEquals(      2.222,  dsmrTelegram.getPowerReceivedL2(), 0.001);
        assertEquals(      3.333,  dsmrTelegram.getPowerReceivedL3(), 0.001);
        assertEquals(      4.444,  dsmrTelegram.getPowerReturnedL1(), 0.001);
        assertEquals(      5.555,  dsmrTelegram.getPowerReturnedL2(), 0.001);
        assertEquals(      6.666,  dsmrTelegram.getPowerReturnedL3(), 0.001);
        assertEquals(      1,      dsmrTelegram.getMBusEvents().size());

        assertEquals(    3, dsmrTelegram.getMBusEvents().get(1).getDeviceType());
        assertEquals("2222ABCD123456789", dsmrTelegram.getMBusEvents().get(1).getEquipmentId());
        assertEquals(ZonedDateTime.parse("2010-12-09T11:25+01:00"), dsmrTelegram.getMBusEvents().get(1).getTimestamp());
        assertEquals( 12785.123, dsmrTelegram.getMBusEvents().get(1).getValue(), 0.001);
        assertEquals(     "m3", dsmrTelegram.getMBusEvents().get(1).getUnit());

        assertEquals("2222ABCD123456789", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2010-12-09T11:25+01:00"), dsmrTelegram.getGasTimestamp());
        assertEquals(12785.123, dsmrTelegram.getGasM3(), 0.001);

        assertEquals("EF2F", dsmrTelegram.getCrc());

        // The CRC of this testcase is invalid.
        // Or better: I have not been able to copy it from the documentation and
        // recreate the "original" record for which the provided CRC was calculated.
        // assertTrue(dsmrTelegram.isValidCRC());
    }

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
        assertEquals(        5, dsmrTelegram.getPowerFailures().longValue());
        assertEquals(        3, dsmrTelegram.getLongPowerFailures().longValue());
        assertEquals(        1, dsmrTelegram.getVoltageSagsPhaseL1().longValue());
        assertEquals(        1, dsmrTelegram.getVoltageSagsPhaseL2().longValue());
        assertEquals(        1, dsmrTelegram.getVoltageSagsPhaseL3().longValue());
        assertEquals(        1, dsmrTelegram.getVoltageSwellsPhaseL1().longValue());
        assertEquals(        1, dsmrTelegram.getVoltageSwellsPhaseL2().longValue());
        assertEquals(        1, dsmrTelegram.getVoltageSwellsPhaseL3().longValue());
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
        assertEquals(        3, dsmrTelegram.getPowerFailures().longValue());
        assertEquals(        1, dsmrTelegram.getLongPowerFailures().longValue());
        assertEquals(        0, dsmrTelegram.getVoltageSagsPhaseL1().longValue());
        assertNull(             dsmrTelegram.getVoltageSagsPhaseL2());
        assertNull(             dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL1().longValue());
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
        assertEquals(      933, dsmrTelegram.getPowerFailures().longValue());
        assertEquals(      108, dsmrTelegram.getLongPowerFailures().longValue());
        assertEquals(       60, dsmrTelegram.getVoltageSagsPhaseL1().longValue());
        assertEquals(       44, dsmrTelegram.getVoltageSagsPhaseL2());
        assertEquals(       45, dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(        0, dsmrTelegram.getVoltageSwellsPhaseL1().longValue());
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
        assertEquals(10, dsmrTelegram.getPowerFailureEventLog().size());
        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-11-22T08:24:19+01:00, " +
                                                      "endTime=2018-12-01T03:08:14+01:00, " +
                                                     "duration=PT210H43M55S)",
            dsmrTelegram.getPowerFailureEventLog().get(0).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-09-04T17:09:03+02:00, " +
                                                      "endTime=2018-09-04T17:39:23+02:00, " +
                                                     "duration=PT30M20S)",
            dsmrTelegram.getPowerFailureEventLog().get(1).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-07-07T15:19:52+02:00, " +
                                                      "endTime=2018-07-07T22:04:40+02:00, " +
                                                     "duration=PT6H44M48S)",
            dsmrTelegram.getPowerFailureEventLog().get(2).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-05-16T20:39:43+02:00, " +
                                                      "endTime=2018-06-11T20:44:09+02:00, " +
                                                     "duration=PT624H4M26S)",
            dsmrTelegram.getPowerFailureEventLog().get(3).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-04-25T17:04:31+02:00, " +
                                                      "endTime=2018-04-25T17:52:48+02:00, " +
                                                     "duration=PT48M17S)",
            dsmrTelegram.getPowerFailureEventLog().get(4).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-04-20T21:59:06+02:00, " +
                                                      "endTime=2018-04-20T22:16:54+02:00, " +
                                                     "duration=PT17M48S)",
            dsmrTelegram.getPowerFailureEventLog().get(5).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-02-15T20:05:31+01:00, " +
                                                      "endTime=2018-02-26T20:34:38+01:00, " +
                                                     "duration=PT264H29M7S)",
            dsmrTelegram.getPowerFailureEventLog().get(6).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-02-12T16:42:35+01:00, " +
                                                      "endTime=2018-02-15T19:23:56+01:00, " +
                                                     "duration=PT74H41M21S)",
            dsmrTelegram.getPowerFailureEventLog().get(7).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-02-09T17:48:30+01:00, " +
                                                      "endTime=2018-02-09T18:43:37+01:00, " +
                                                     "duration=PT55M7S)",
            dsmrTelegram.getPowerFailureEventLog().get(8).toString());

        assertEquals("DSMRTelegram.PowerFailureEvent(startTime=2018-02-08T20:02:51+01:00, " +
                                                      "endTime=2018-02-08T20:07:17+01:00, " +
                                                     "duration=PT4M26S)",
            dsmrTelegram.getPowerFailureEventLog().get(9).toString());



        assertNull(dsmrTelegram.getGasEquipmentId());

        assertTrue(dsmrTelegram.isValidCRC());
        assertEquals("0B43", dsmrTelegram.getCrc());
        assertTrue(dsmrTelegram.isValid());

//        LOG.info("{}", dsmrTelegram);
    }


    @Test
    void testParseExtendedTestcase(){
        String testcase = "\r\n" +
            "/ISk5\\2MT382-1000\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(101209113020W)\r\n" +
            "0-0:96.1.1(4B384547303034303436333935353037)\r\n" +
            "1-0:1.8.1(123456.789*kWh)\r\n" +
            "1-0:1.8.2(123456.789*kWh)\r\n" +
            "1-0:2.8.1(123456.789*kWh)\r\n" +
            "1-0:2.8.2(123456.789*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(01.193*kW)\r\n" +
            "1-0:2.7.0(00.000*kW)\r\n" +
            "0-0:96.7.21(00004)\r\n" +
            "0-0:96.7.9(00002)\r\n" +
            "1-0:99.97.0(2)(0-0:96.7.19)(101208152415W)(0000000240*s)(101208151004W)(0000000301*s)\r\n" +
            "1-0:32.32.0(00002)\r\n" +
            "1-0:52.32.0(00001)\r\n" +
            "1-0:72.32.0(00000)\r\n" +
            "1-0:32.36.0(00000)\r\n" +
            "1-0:52.36.0(00003)\r\n" +
            "1-0:72.36.0(00000)\r\n" +
            "0-0:96.13.0(303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F30313233343536373839" +
            "3A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F)\r\n" +
            "1-0:32.7.0(220.1*V)\r\n" +
            "1-0:52.7.0(220.2*V)\r\n" +
            "1-0:72.7.0(220.3*V)\r\n" +
            "1-0:31.7.0(001*A)\r\n" +
            "1-0:51.7.0(002*A)\r\n" +
            "1-0:71.7.0(003*A)\r\n" +
            "1-0:21.7.0(01.111*kW)\r\n" +
            "1-0:41.7.0(02.222*kW)\r\n" +
            "1-0:61.7.0(03.333*kW)\r\n" +
            "1-0:22.7.0(04.444*kW)\r\n" +
            "1-0:42.7.0(05.555*kW)\r\n" +
            "1-0:62.7.0(06.666*kW)\r\n" +

            // NOTE: These values are created from what I understand of the specs.
            // NOTE: I really NEED a REAL example of these values !!!
            // NOTE: There won't be any examples because these are not used

            "0-1:24.1.0(002)\r\n" +
            "0-1:96.1.0(5f5f5f5f4f4e455f5f5f5f)\r\n" +
            "0-1:24.2.1(101209112100W)(12785.111*kWh)\r\n" +
            "0-2:24.1.0(003)\r\n" +
            "0-2:96.1.0(5f5f5f5f54574f5f5f5f5f)\r\n" +
            "0-2:24.2.1(101209112200W)(12785.222*m3)\r\n" +
            "0-3:24.1.0(004)\r\n" +
            "0-3:96.1.0(5f5f5f5f54485245455f5f)\r\n" +
            "0-3:24.2.1(101209112300W)(12785.333*GJ)\r\n" +
            "0-4:24.1.0(010)\r\n" +
            "0-4:96.1.0(5f5f5f5f464f55525f5f5f)\r\n" +
            "0-4:24.2.1(101209112400W)(12785.444*GJ)\r\n" +
            "!BAD0\r\n" +
            "\r\n";



        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);

        // CHECKSTYLE.OFF: ParenPad
        assertEquals("/ISk5\\2MT382-1000", dsmrTelegram.getIdent());
        assertEquals("50", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2010-12-09T11:30:20+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("K8EG004046395507", dsmrTelegram.getEquipmentId());
        assertEquals("0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?", dsmrTelegram.getMessage());

        assertEquals(      2,      dsmrTelegram.getElectricityTariffIndicator());
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals( 123456.789,  dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(      1.193,  dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0,    dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertEquals(      4,      dsmrTelegram.getPowerFailures().longValue());
        assertEquals(      2,      dsmrTelegram.getLongPowerFailures().longValue());
        assertEquals(      2,      dsmrTelegram.getVoltageSagsPhaseL1().longValue());
        assertEquals(      1,      dsmrTelegram.getVoltageSagsPhaseL2().longValue());
        assertEquals(      0,      dsmrTelegram.getVoltageSagsPhaseL3().longValue());
        assertEquals(      0,      dsmrTelegram.getVoltageSwellsPhaseL1().longValue());
        assertEquals(      3,      dsmrTelegram.getVoltageSwellsPhaseL2().longValue());
        assertEquals(      0,      dsmrTelegram.getVoltageSwellsPhaseL3().longValue());
        assertEquals(    220.1,    dsmrTelegram.getVoltageL1(),       0.001);
        assertEquals(    220.2,    dsmrTelegram.getVoltageL2(),       0.001);
        assertEquals(    220.3,    dsmrTelegram.getVoltageL3(),       0.001);
        assertEquals(      1,      dsmrTelegram.getCurrentL1(),       0.001);
        assertEquals(      2,      dsmrTelegram.getCurrentL2(),       0.001);
        assertEquals(      3,      dsmrTelegram.getCurrentL3(),       0.001);
        assertEquals(      1.111,  dsmrTelegram.getPowerReceivedL1(), 0.001);
        assertEquals(      2.222,  dsmrTelegram.getPowerReceivedL2(), 0.001);
        assertEquals(      3.333,  dsmrTelegram.getPowerReceivedL3(), 0.001);
        assertEquals(      4.444,  dsmrTelegram.getPowerReturnedL1(), 0.001);
        assertEquals(      5.555,  dsmrTelegram.getPowerReturnedL2(), 0.001);
        assertEquals(      6.666,  dsmrTelegram.getPowerReturnedL3(), 0.001);
        assertEquals(      4,      dsmrTelegram.getMBusEvents().size());

        checkMbus(dsmrTelegram, 1, "2010-12-09T11:21+01:00",  2, "____ONE____",  12785.111, "kWh");
        checkMbus(dsmrTelegram, 2, "2010-12-09T11:22+01:00",  3, "____TWO____",  12785.222, "m3");
        checkMbus(dsmrTelegram, 3, "2010-12-09T11:23+01:00",  4, "____THREE__",  12785.333, "GJ");
        checkMbus(dsmrTelegram, 4, "2010-12-09T11:24+01:00", 10, "____FOUR___",  12785.444, "GJ");

        assertEquals("BAD0", dsmrTelegram.getCrc());

        // The CRC of this testcase is invalid.
    }


    void validateParseExtendedTestcaseMultipleMbus(int mbusType, String unit){
        String testcase = "\r\n" +
            "/ISk5\\2MT382-1000\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(101209113020W)\r\n" +
            "0-0:96.1.1(4B384547303034303436333935353037)\r\n" +

            "0-1:24.1.0(00"+mbusType+")\r\n" +
            "0-1:96.1.0(5f5f5f5f4f4e455f5f5f5f)\r\n" +
            "0-1:24.2.1(101209112100W)(12785.111*"+unit+")\r\n" +
            "0-2:24.1.0(00"+mbusType+")\r\n" +
            "0-2:96.1.0(5f5f5f5f54574f5f5f5f5f)\r\n" +
            "0-2:24.2.1(101209112200W)(12785.222*"+unit+")\r\n" +
            "0-3:24.1.0(00"+mbusType+")\r\n" +
            "0-3:96.1.0(5f5f5f5f54485245455f5f)\r\n" +
            "0-3:24.2.1(101209112300W)(12785.333*"+unit+")\r\n" +
            "0-4:24.1.0(00"+mbusType+")\r\n" +
            "0-4:96.1.0(5f5f5f5f464f55525f5f5f)\r\n" +
            "0-4:24.2.1(101209112400W)(12785.444*"+unit+")\r\n" +
            "!BAD0\r\n" +
            "\r\n";

        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);

        // CHECKSTYLE.OFF: ParenPad
        assertEquals("/ISk5\\2MT382-1000", dsmrTelegram.getIdent());
        assertEquals("50", dsmrTelegram.getP1Version());
        assertEquals(ZonedDateTime.parse("2010-12-09T11:30:20+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("K8EG004046395507", dsmrTelegram.getEquipmentId());

        assertEquals(      4,      dsmrTelegram.getMBusEvents().size());

        checkMbus(dsmrTelegram, 1, "2010-12-09T11:21+01:00", mbusType, "____ONE____",  12785.111, unit);
        checkMbus(dsmrTelegram, 2, "2010-12-09T11:22+01:00", mbusType, "____TWO____",  12785.222, unit);
        checkMbus(dsmrTelegram, 3, "2010-12-09T11:23+01:00", mbusType, "____THREE__",  12785.333, unit);
        checkMbus(dsmrTelegram, 4, "2010-12-09T11:24+01:00", mbusType, "____FOUR___",  12785.444, unit);

        assertEquals("BAD0", dsmrTelegram.getCrc());

        // The CRC of this testcase is invalid.
    }


    @Test
    void testParseExtendedTestcaseMultipleElectric(){
        validateParseExtendedTestcaseMultipleMbus(2, "kWh");
    }

    @Test
    void testParseExtendedTestcaseMultipleGas() {
        validateParseExtendedTestcaseMultipleMbus(3, "m3");
    }

    @Test
    void testParseExtendedTestcaseMultipleHeat(){
        validateParseExtendedTestcaseMultipleMbus(4, "GJ");
    }

    @Test
    void testParseExtendedTestcaseMultipleWarmWater(){
        validateParseExtendedTestcaseMultipleMbus(6, "m3");
    }

    @Test
    void testParseExtendedTestcaseMultipleWater(){
        validateParseExtendedTestcaseMultipleMbus(7, "m3");
    }

    @Test
    void testParseExtendedTestcaseMultipleCoolingInlet(){
        validateParseExtendedTestcaseMultipleMbus(11, "GJ");
    }

    @Test
    void testParseExtendedTestcaseMultipleCoolingOutlet(){
        validateParseExtendedTestcaseMultipleMbus(10, "GJ");
    }



    @Test
    void testNull(){
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(null);
        assertNull(dsmrTelegram);
    }

    @Test
    void testEmpty(){
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse("");
        assertNull(dsmrTelegram);
    }

    @Test
    void testSyntaxNotEmpty(){
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(" ");
        assertNotNull(dsmrTelegram);
        assertFalse(dsmrTelegram.isValid());
    }

    @Test
    void testSyntaxError(){
        String testcase = "\r\n" +
            "/ISk5\\2MT382-1000\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(101209113020W)\r\n" +
            "A-0:96.1.1(4B384547303034303436333935353037)\r\n" +
            "!BAD0\r\n" +
            "\r\n";

        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);
        assertNotNull(dsmrTelegram);
        assertFalse(dsmrTelegram.isValid());
    }

    private void checkMbus(DSMRTelegram dsmrTelegram,
                           int mBusId,
                           String timeString,
                           int deviceType,
                           String equipmentId,
                           Double value,
                           String unit ) {
        final MBusEvent mBusEvent = dsmrTelegram.getMBusEvents().get(mBusId);
        assertNotNull(mBusEvent);
        assertEquals(ZonedDateTime.parse(timeString),   mBusEvent.getTimestamp());
        assertEquals(deviceType,                        mBusEvent.getDeviceType());
        assertEquals(equipmentId,                       mBusEvent.getEquipmentId());
        assertEquals(value,                             mBusEvent.getValue(), 0.0001);
        assertEquals(unit,                              mBusEvent.getUnit());
    }
}
