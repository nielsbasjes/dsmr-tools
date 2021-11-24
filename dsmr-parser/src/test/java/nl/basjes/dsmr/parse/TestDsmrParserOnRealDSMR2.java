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
import nl.basjes.dsmr.ParseDsmrTelegram;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CHECKSTYLE.OFF: ParenPad
class TestDsmrParserOnRealDSMR2 {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParserOnRealDSMR2.class);

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

        assertEquals("/ISk5\\2MT382-1003", dsmrTelegram.getRawIdent());
        assertEquals("ISK", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("MT382-1003", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
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

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMRTelegramIssue54Extra() {
        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(
            "/ISk5\\2MT382-1003\r\n" +
            "\r\n" +
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

        assertEquals("/ISk5\\2MT382-1003", dsmrTelegram.getRawIdent());
        assertEquals("ISK", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("MT382-1003", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
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

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMR22Telegram1() {
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

        assertEquals("/XMX5XMXABCE100103855", dsmrTelegram.getRawIdent());
        assertEquals("XMX", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("XMXABCE100103855", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
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

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMR22Telegram2() {
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

        assertEquals("/KMP5 ZABF001551772711", dsmrTelegram.getRawIdent());
        assertEquals("KMP", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("ZABF001551772711", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("ZABF00151772711", dsmrTelegram.getEquipmentId());

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

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMR22Telegram3() {
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

        assertEquals("/KMP5 KA6U001660297912", dsmrTelegram.getRawIdent());
        assertEquals("KMP", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("KA6U001660297912", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
        assertNull(dsmrTelegram.getTimestamp());

        assertEquals("KA6U001660297912", dsmrTelegram.getEquipmentId());

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

//        LOG.info("{}", dsmrTelegram);
    }

    @Test
    void testDSMR22Telegram4() {
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

        assertEquals("/KMP5 ZABF001587315111", dsmrTelegram.getRawIdent());
        assertEquals("KMP", dsmrTelegram.getEquipmentBrandTag());
        assertEquals("ZABF001587315111", dsmrTelegram.getIdent());

        assertEquals("2.2", dsmrTelegram.getP1Version());
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

//        LOG.info("{}", dsmrTelegram);
    }

}
