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

import static org.junit.jupiter.api.Assertions.assertFalse;

class TestDsmrParserOnBadTelegram {

    private static final Logger LOG = LoggerFactory.getLogger(TestDsmrParserOnBadTelegram.class);

    @Test
    void testParseBadTestcase(){
        String testcase = "\r\n" +
            "/ISK5\\2M550T-1012\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(220528150240S)\r\n" +
            "0-0:96.1.1(4530303434303037313331363530363138)\r\n" +
            "1-0:1.8.1(016366.258*kWh)\r\n" +
            "1-0:1.8.2(013315.593*kWh)\r\n" +
            "1-0:2.8.1(002434.782*kWh)\r\n" +
            "1-0:2.8.2(006153.962*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(00.000*kW)\r\n" +
            "1-0:2.7.0(02.875*kW)\r\n" +
            "0-0:96.7.21(00005)\r\n" +
            "0-0:96.7.9(00004)\r\n" +
            "1-0:99.97.0(2)(0-0:96.7.19)(180417201458S)(0000000236*s)(220525094346S)(0000002936*s)\r\n" +
            "1-0:32.32.0(0\r\n" +
            "/ISK5\\2M550T-1012\r\n" +
            "\r\n" +
            "1-3:0.2.8(50)\r\n" +
            "0-0:1.0.0(220528150245S)\r\n" +
            "0-0:96.1.1(4530303434303037313331363530363138)\r\n" +
            "1-0:1.8.1(016366.258*kWh)\r\n" +
            "1-0:1.8.2(013315.593*kWh)\r\n" +
            "1-0:2.8.1(002434.785*kWh)\r\n" +
            "1-0:2.8.2(006153.962*kWh)\r\n" +
            "0-0:96.14.0(0001)\r\n" +
            "1-0:1.7.0(00.000*kW)\r\n" +
            "1-0:2.7.0(02.857*kW)\r\n" +
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
            "1-0:32.7.0(239.5*V)\r\n" +
            "1-0:52.7.0(239.2*V)\r\n" +
            "1-0:72.7.0(238.7*V)\r\n" +
            "1-0:31.7.0(000*A)\r\n" +
            "1-0:51.7.0(014*A)\r\n" +
            "1-0:71.7.0(003*A)\r\n" +
            "1-0:21.7.0(00.052*kW)\r\n" +
            "1-0:41.7.0(00.000*kW)\r\n" +
            "1-0:61.7.0(00.666*kW)\r\n" +
            "1-0:22.7.0(00.000*kW)\r\n" +
            "1-0:42.7.0(03.572*kW)\r\n" +
            "1-0:62.7.0(00.000*kW)\r\n" +
            "!34B6\r\n" +
            "\r\n";

        DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(testcase);

        assertFalse(dsmrTelegram.isValid());
        assertFalse(dsmrTelegram.isValidCRC());
    }
}

