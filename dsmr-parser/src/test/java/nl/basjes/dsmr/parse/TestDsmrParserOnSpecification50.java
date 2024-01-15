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
import static org.junit.jupiter.api.Assertions.assertEquals;

// CHECKSTYLE.OFF: ParenPad
class TestDsmrParserOnSpecification50 {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParserOnSpecification50.class);

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
    void testParseTestcaseFrom50Specification(){
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

        assertEquals(2, dsmrTelegram.getPowerFailureEventLogSize());

        List<PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(2, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0), "2010-12-08T15:20:15+01:00", "2010-12-08T15:24:15+01:00", "PT4M");
        assertPowerFailureEvent(powerFailureEventLog.get(1), "2010-12-08T15:05:03+01:00", "2010-12-08T15:10:04+01:00", "PT5M1S");

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

}
