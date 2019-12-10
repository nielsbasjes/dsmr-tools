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


import nl.basjes.dsmr.CheckCRC;
import org.junit.jupiter.api.Test;

import static nl.basjes.dsmr.CheckCRC.calculatedCrc;
import static nl.basjes.dsmr.CheckCRC.crcIsValid;
import static nl.basjes.dsmr.CheckCRC.extractCrcFromTelegram;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCRCValidator {

    private String dsmrTelegram = "/ISK5\\2M550T-1012\r\n" +
        "\r\n" +
        "1-3:0.2.8(50)\r\n" +
        "0-0:1.0.0(190324151445W)\r\n" +
        "0-0:96.1.1(4530303434303037313331363530363138)\r\n" +
        "1-0:1.8.1(003432.921*kWh)\r\n" +
        "1-0:1.8.2(003224.632*kWh)\r\n" +
        "1-0:2.8.1(000000.000*kWh)\r\n" +
        "1-0:2.8.2(000000.000*kWh)\r\n" +
        "0-0:96.14.0(0001)\r\n" +
        "1-0:1.7.0(00.744*kW)\r\n" +
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
        "1-0:32.7.0(237.1*V)\r\n" +
        "1-0:52.7.0(235.4*V)\r\n" +
        "1-0:72.7.0(236.7*V)\r\n" +
        "1-0:31.7.0(000*A)\r\n" +
        "1-0:51.7.0(000*A)\r\n" +
        "1-0:71.7.0(003*A)\r\n" +
        "1-0:21.7.0(00.044*kW)\r\n" +
        "1-0:41.7.0(00.011*kW)\r\n" +
        "1-0:61.7.0(00.681*kW)\r\n" +
        "1-0:22.7.0(00.000*kW)\r\n" +
        "1-0:42.7.0(00.000*kW)\r\n" +
        "1-0:62.7.0(00.000*kW)\r\n" +
        "!478B\r\n";

    @Test
    public void testCrc(){
        assertTrue(crcIsValid(dsmrTelegram), "CRC is not valid");
    }

    @Test
    public void testCaculateCrc(){
        String hexCrc = String.format("%04X", calculatedCrc(dsmrTelegram));

        assertEquals("478B", hexCrc);
        assertTrue(crcIsValid(dsmrTelegram + hexCrc), "CRC is not valid");
    }

    @Test
    public void testNull(){
        assertFalse(crcIsValid(null));
    }

    @Test
    public void testEmpty(){
        assertFalse(crcIsValid(""));
    }

    @Test
    public void testSyntaxError(){
        assertFalse(crcIsValid("Bla bla bla"));
    }

    @Test
    public void testRepairingCrc(){
        String record = "/ISK5\\2M550T-1012\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(190324151445W)\r\n" +
            "0-0:96.1.1(4530303434303037313331363530363138)\r\n" +
            "!1234\r\n";

        assertFalse(crcIsValid(record), "CRC is not valid");
        assertEquals("1234", extractCrcFromTelegram(record), "Wrong CRC extracted");
        record = CheckCRC.fixCrc(record);
        assertTrue(crcIsValid(record), "CRC should be valid");
        assertNotEquals("1234", extractCrcFromTelegram(record), "Wrong CRC extracted");
    }

    @Test
    public void testHandlingBadInput() {
        assertNull(calculatedCrc(null));
        assertNull(calculatedCrc("Bla"));
        assertNull(extractCrcFromTelegram(null));
        assertNull(extractCrcFromTelegram("Bla"));
        assertFalse(crcIsValid(null));
        assertFalse(crcIsValid("Bla"));

    }


}
