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
import nl.basjes.dsmr.ParseDsmrTelegram;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CHECKSTYLE.OFF: ParenPad
class TestDsmrParserOnSpecification30 {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParserOnSpecification30.class);

    // The example telegram below is based on:
    // - DSMR version 3.0
    @Test
    void testParseTestcaseFrom30Specification(){
        String testcase =
            "/ISk5\\2MT382-1000\r\n" +
            "\r\n" +
            "0-0:96.1.1(4B384547303034303436333935353037)\r\n" +
            "1-0:1.8.1(12345.678*kWh)\r\n" +
            "1-0:1.8.2(12345.678*kWh)\r\n" +
            "1-0:2.8.1(12345.678*kWh)\r\n" +
            "1-0:2.8.2(12345.678*kWh)\r\n" +
            "0-0:96.14.0(0002)\r\n" +
            "1-0:1.7.0(001.19*kW)\r\n" +
            "1-0:2.7.0(000.00*kW)\r\n" +
            "0-0:17.0.0(016*A)\r\n" +
            "0-0:96.3.10(1)\r\n" +
            "0-0:96.13.1(303132333435363738)\r\n" +
            "0-0:96.13.0(303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F" +
            "303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F" +
            "303132333435363738393A3B3C3D3E3F)\r\n" +
            "0-1:96.1.0(3232323241424344313233343536373839)\r\n" +
            "0-1:24.1.0(03)\r\n" +
            "0-1:24.3.0(090212160000)(00)(60)(1)(0-1:24.2.1)(m3)\r\n" +
            "(00000.000)\r\n" +
            "0-1:24.4.0(1)\r\n" +
            "!\r\n";

        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);

//        LOG.info("{}", dsmrTelegram);

        assertEquals("/ISk5\\2MT382-1000", dsmrTelegram.getRawIdent());
        assertEquals("ISK", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("MT382-1000", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("K8EG004046395507", dsmrTelegram.getEquipmentId());
        assertEquals("0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?0123456789:;<=>?", dsmrTelegram.getMessage());

        assertEquals(      2,      dsmrTelegram.getElectricityTariffIndicator());
        assertEquals(  12345.678,  dsmrTelegram.getElectricityReceivedLowTariff(),    0.001);
        assertEquals(  12345.678,  dsmrTelegram.getElectricityReceivedNormalTariff(), 0.001);
        assertEquals(  12345.678,  dsmrTelegram.getElectricityReturnedLowTariff(),    0.001);
        assertEquals(  12345.678,  dsmrTelegram.getElectricityReturnedNormalTariff(), 0.001);
        assertEquals(      1.19,  dsmrTelegram.getElectricityPowerReceived(),        0.001);
        assertEquals(      0.0,    dsmrTelegram.getElectricityPowerReturned(),        0.001);
        assertNull(            dsmrTelegram.getPowerFailures());
        assertNull(            dsmrTelegram.getLongPowerFailures());

        assertEquals(0, dsmrTelegram.getPowerFailureEventLogSize());
        assertEquals(0, dsmrTelegram.getPowerFailureEventLog().size());

        assertEquals(      1,      dsmrTelegram.getMBusEvents().size());

        assertEquals(    3, dsmrTelegram.getMBusEvents().get(1).getDeviceType());
        assertEquals("2222ABCD123456789", dsmrTelegram.getMBusEvents().get(1).getEquipmentId());
        assertEquals(ZonedDateTime.parse("2009-02-12T16:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getMBusEvents().get(1).getTimestamp());
        assertEquals( 0.000, dsmrTelegram.getMBusEvents().get(1).getValue(), 0.001);
        assertEquals(     "m3", dsmrTelegram.getMBusEvents().get(1).getUnit());

        assertEquals("2222ABCD123456789", dsmrTelegram.getGasEquipmentId());
        assertEquals(ZonedDateTime.parse("2009-02-12T16:00+01:00[Europe/Amsterdam]"), dsmrTelegram.getGasTimestamp());
        assertEquals(0.000, dsmrTelegram.getGasM3(), 0.001);

        assertNull(dsmrTelegram.getCrc());
        assertTrue(dsmrTelegram.isValid());
    }

}
