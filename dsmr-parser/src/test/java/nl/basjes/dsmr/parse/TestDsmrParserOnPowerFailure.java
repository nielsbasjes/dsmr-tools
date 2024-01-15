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

import java.util.List;

import static nl.basjes.dsmr.parse.Utils.assertPowerFailureEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDsmrParserOnPowerFailure {

    @Test
    void testParseBadTestcase(){
        String testcase = "/ISK5\\2M550T-1012\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(220528151729S)\r\n" +
            "0-0:96.1.1(4530303434303037313331363530363138)\r\n" +
            "1-0:1.8.1(016366.258*kWh)\r\n" +
            "1-0:1.8.2(013315.593*kWh)\r\n" +
            "1-0:2.8.1(002435.025*kWh)\r\n" +
            "1-0:2.8.2(006153.962*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(00.000*kW)\r\n" +
            "1-0:2.7.0(00.098*kW)\r\n" +
            "0-0:96.7.21(00005)\r\n" +
            "0-0:96.7.9(00004)\r\n" +
            "1-0:99.97.0(2)(0-0:96.7.19)(180417201458S)(0000000236*s)(220525094346S)(0000002936*s)\r\n" +
            "1-0:32.32.0(00004)\r\n" +
            "1-0:52.32.0(00003)\r\n" +
            "1-0:72.32.0(00003)\r\n" +
            "1-0:32.36.0(00001)\r\n" +
            "1-0:52.36.0(00001)\r\n" +
            "1-0:72.36.0(00001)\r\n" +
            "0-0:96.13.0()\r\n" +
            "1-0:32.7.0(238.3*V)\r\n" +
            "1-0:52.7.0(237.1*V)\r\n" +
            "1-0:72.7.0(237.7*V)\r\n" +
            "1-0:31.7.0(000*A)\r\n" +
            "1-0:51.7.0(003*A)\r\n" +
            "1-0:71.7.0(003*A)\r\n" +
            "1-0:21.7.0(00.054*kW)\r\n" +
            "1-0:41.7.0(00.000*kW)\r\n" +
            "1-0:61.7.0(00.631*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "1-0:42.7.0(00.842*kW)\r\n" +
            "1-0:62.7.0(00.000*kW)\r\n" +
            "!46B4\r\n" +
            "\r\n";

        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);

        assertTrue(dsmrTelegram.isValid());
        assertTrue(dsmrTelegram.isValidCRC());

        List<DSMRTelegram.PowerFailureEvent> powerFailureEventLog = dsmrTelegram.getPowerFailureEventLog();
        assertEquals(2, powerFailureEventLog.size());
        assertPowerFailureEvent(powerFailureEventLog.get(0), "2018-04-17T20:11:02+02:00", "2018-04-17T20:14:58+02:00", "PT3M56S");
        assertPowerFailureEvent(powerFailureEventLog.get(1), "2022-05-25T08:54:50+02:00", "2022-05-25T09:43:46+02:00", "PT48M56S");

        // Start was reported on Twitter @ 8:57 AM · May 25, 2022
        // - https://twitter.com/LianderNL/status/1529355943716200454
        // End was reported on twitter @ 9:49 AM · May 25, 2022
        // - https://twitter.com/LianderNL/status/1529369061255266304
    }
}

