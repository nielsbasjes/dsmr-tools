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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDsmrParser {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParser.class);


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
        assertEquals("/ISk5\\2MT382-1000", dsmrTelegram.getRawIdent());
        assertEquals("ISK", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("MT382-1000", dsmrTelegram.getIdent());

        assertEquals("5.0", dsmrTelegram.getP1Version());
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
        assertEquals(      4,      dsmrTelegram.getPowerFailures());
        assertEquals(      2,      dsmrTelegram.getLongPowerFailures());
        assertEquals(      2,      dsmrTelegram.getVoltageSagsPhaseL1());
        assertEquals(      1,      dsmrTelegram.getVoltageSagsPhaseL2());
        assertEquals(      0,      dsmrTelegram.getVoltageSagsPhaseL3());
        assertEquals(      0,      dsmrTelegram.getVoltageSwellsPhaseL1());
        assertEquals(      3,      dsmrTelegram.getVoltageSwellsPhaseL2());
        assertEquals(      0,      dsmrTelegram.getVoltageSwellsPhaseL3());
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

        List<PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(2, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0), "2010-12-08T15:20:15+01:00", "2010-12-08T15:24:15+01:00", "PT4M");
        assertPowerFailureEvent(powerFailureEventLog.get(1), "2010-12-08T15:05:03+01:00", "2010-12-08T15:10:04+01:00", "PT5M1S");

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
        assertEquals("/ISk5\\2MT382-1000", dsmrTelegram.getRawIdent());
        assertEquals("ISK", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("MT382-1000", dsmrTelegram.getIdent());

        assertEquals("5.0", dsmrTelegram.getP1Version());
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

    @Test
    void testUnknownCosemId(){
        String testcase = "\r\n" +
            "/ISk5\\2MT382-1000\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:0.0.0(101209113020W)\r\n" +
            "0-0:96.1.1(4B384547303034303436333935353037)\r\n" +
            "!8AF2\r\n" +
            "\r\n";

//        LOG.info("{}", CheckCRC.fixCrc(testcase));

        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);
        assertNotNull(dsmrTelegram);
        assertTrue(dsmrTelegram.isValid());
    }

}

