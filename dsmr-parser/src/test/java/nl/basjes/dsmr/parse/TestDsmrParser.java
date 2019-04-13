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

import nl.basjes.dsmr.DSMRTelegram;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestDsmrParser {

    private static final Logger LOG = LoggerFactory.getLogger(TestRecordStream.class);

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
    public void testParse(){
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
            "0-0:96.13.0(303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F)\r\n" +
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

        LOG.info("{}", dsmrTelegram);
    }


    @Test
    public void testParseRealTelegram(){
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

        assertEquals("/ISK5\\2M550T-1012", dsmrTelegram.getIdent());
        assertEquals("50", dsmrTelegram.getP1Version());
//        assertEquals("2019-03-24T15:05:41+01:00", dsmrTelegram.getTimestamp());
        assertEquals(ZonedDateTime.parse("2019-03-24T15:05:41+01:00"), dsmrTelegram.getTimestamp());

        assertEquals("E0044007131650618", dsmrTelegram.getEquipmentId());
        assertEquals("", dsmrTelegram.getMessage());

        assertEquals( 3432.829, dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals( 3224.632, dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(      0.0, dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(      1.0, dsmrTelegram.getElectricityTariffIndicator(),      0.001);
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

//
            LOG.info("{}", dsmrTelegram);
    }

    @Test
    public void testParseRealTelegram2(){
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


}
