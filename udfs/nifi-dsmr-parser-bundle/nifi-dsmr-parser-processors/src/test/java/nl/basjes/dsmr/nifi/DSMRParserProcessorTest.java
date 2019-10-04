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
package nl.basjes.dsmr.nifi;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DSMRParserProcessorTest {

    private TestRunner runner;

    @BeforeEach
    public void init() {
        runner = TestRunners.newTestRunner(DSMRParserProcessor.class);
    }

    @Test
    public void testValidRecord() {
        // Test content
        String content =
            "\r\n" + // Stray newline that should be ignored.
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
            "!9DF0\r\n" +
            "\r\n"; // Stray newline that should be ignored.

        // Add the content to the runner (just because we 'should' have some content).
        MockFlowFile flowfile = runner.enqueue(content);
        Map<String, String> attributes = new HashMap<>();
        // NO attributes
        flowfile.putAttributes(attributes);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(DSMRParserProcessor.VALID);
        assertEquals(1, results.size(), "Should be 1 match");
        MockFlowFile result = results.get(0);

        assertAttributeEquals(result, "dsmr.validCRC",       "true");
        assertAttributeEquals(result, "dsmr.crc",            "9DF0");

        assertAttributeEquals(result, "dsmr.ident",          "/ISK5\\2M550T-1012");
        assertAttributeEquals(result, "dsmr.p1Version",      "50");
        assertAttributeEquals(result, "dsmr.timestamp",      "2019-03-24T15:05:41+01:00");

        assertAttributeEquals(result, "dsmr.equipmentId",    "E0044007131650618");
        assertAttributeEquals(result, "dsmr.message",        "");

        assertAttributeEquals(result, "dsmr.electricityReceivedLowTariff",     "3432.829");
        assertAttributeEquals(result, "dsmr.electricityReceivedNormalTariff",  "3224.632");
        assertAttributeEquals(result, "dsmr.electricityReturnedLowTariff",          "0.0");
        assertAttributeEquals(result, "dsmr.electricityReturnedNormalTariff",       "0.0");
        assertAttributeEquals(result, "dsmr.electricityTariffIndicator",            "1.0");
        assertAttributeEquals(result, "dsmr.electricityPowerReceived",            "0.433");
        assertAttributeEquals(result, "dsmr.electricityPowerReturned",              "0.0");
        assertAttributeEquals(result, "dsmr.powerFailures",                           "5");
        assertAttributeEquals(result, "dsmr.longPowerFailures",                       "3");
        assertAttributeEquals(result, "dsmr.voltageSagsPhaseL1",                      "1");
        assertAttributeEquals(result, "dsmr.voltageSagsPhaseL2",                      "1");
        assertAttributeEquals(result, "dsmr.voltageSagsPhaseL3",                      "1");
        assertAttributeEquals(result, "dsmr.voltageSwellsPhaseL1",                    "1");
        assertAttributeEquals(result, "dsmr.voltageSwellsPhaseL2",                    "1");
        assertAttributeEquals(result, "dsmr.voltageSwellsPhaseL3",                    "1");
        assertAttributeEquals(result, "dsmr.voltageL1",                           "236.7");
        assertAttributeEquals(result, "dsmr.voltageL2",                           "234.5");
        assertAttributeEquals(result, "dsmr.voltageL3",                           "236.0");
        assertAttributeEquals(result, "dsmr.currentL1",                             "0.0");
        assertAttributeEquals(result, "dsmr.currentL2",                             "0.0");
        assertAttributeEquals(result, "dsmr.currentL3",                             "2.0");
        assertAttributeEquals(result, "dsmr.powerReceivedL1",                     "0.045");
        assertAttributeEquals(result, "dsmr.powerReceivedL2",                      "0.01");
        assertAttributeEquals(result, "dsmr.powerReceivedL3",                     "0.379");
        assertAttributeEquals(result, "dsmr.powerReturnedL1",                       "0.0");
        assertAttributeEquals(result, "dsmr.powerReturnedL2",                       "0.0");
        assertAttributeEquals(result, "dsmr.powerReturnedL3",                       "0.0");
//        assertAttributeEquals(result, "dsmr.mBusEvents",                              "0");

        // Test attributes and content
        result.assertContentEquals(content);


    }

    public void assertAttributeEquals(MockFlowFile flowFile, String attributeName, String expectedValue) {
        assertEquals(expectedValue, flowFile.getAttribute(attributeName),
            "Attribute \"" + attributeName + "\" has the wrong value.");
    }
}
