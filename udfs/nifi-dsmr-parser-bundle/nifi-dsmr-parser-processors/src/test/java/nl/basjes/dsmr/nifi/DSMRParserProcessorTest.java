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
package nl.basjes.dsmr.nifi;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.dsmr.nifi.DSMRParserProcessor.BAD_RECORDS;
import static nl.basjes.dsmr.nifi.DSMRParserProcessor.INVALID_CRC;
import static nl.basjes.dsmr.nifi.DSMRParserProcessor.VALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DSMRParserProcessorTest {

    private TestRunner runner;

    @BeforeEach
    void init() {
        runner = TestRunners.newTestRunner(DSMRParserProcessor.class);
    }

    @Test
    void testValidRecord() {
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
            "1-0:99.97.0(2)(0-0:96.7.19)(180417201458S)(0000000236*s)(210321163842S)(0000001234*s)\r\n" +
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

            // NOTE: These values are created from what I understand of the specs.
            // I have put them 'out-of-order' deliberately to test the code better
            // 4
            "0-4:24.1.0(010)\r\n" +
            "0-4:96.1.0(5f5f5f5f464f55525f5f5f)\r\n" +
            "0-4:24.2.1(101209112400W)(12785.444*GJ)\r\n" +
            // 1
            "0-1:24.1.0(002)\r\n" +
            "0-1:96.1.0(5f5f5f5f4f4e455f5f5f5f)\r\n" +
            "0-1:24.2.1(101209112100W)(12785.111*kWh)\r\n" +
            // 3
            "0-3:24.1.0(004)\r\n" +
            "0-3:96.1.0(5f5f5f5f54485245455f5f)\r\n" +
            "0-3:24.2.1(101209112300W)(12785.333*GJ)\r\n" +
            // 2
            "0-2:24.1.0(003)\r\n" +
            "0-2:96.1.0(5f5f5f5f54574f5f5f5f5f)\r\n" +
            "0-2:24.2.1(101209112200W)(12785.222*m3)\r\n" +

            "!B053\r\n" +
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
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(VALID);
        assertEquals(1, results.size());

        List<MockFlowFile> invalidCRCresults = runner.getFlowFilesForRelationship(INVALID_CRC);
        assertEquals(0, invalidCRCresults.size());

        List<MockFlowFile> badresults = runner.getFlowFilesForRelationship(BAD_RECORDS);
        assertEquals(0, badresults.size());

        MockFlowFile result = results.get(0);

        assertAttributeEquals(result, "dsmr.validCRC",              "true");
        assertAttributeEquals(result, "dsmr.crc",                   "B053");

        assertAttributeEquals(result, "dsmr.rawIdent",              "/ISK5\\2M550T-1012");
        assertAttributeEquals(result, "dsmr.ident",                 "M550T-1012");
        assertAttributeEquals(result, "dsmr.equipmentBrandTag",     "ISK");
        assertAttributeEquals(result, "dsmr.p1Version",             "5.0");
        assertAttributeEquals(result, "dsmr.timestamp",             "2019-03-24T15:05:41+01:00");

        assertAttributeEquals(result, "dsmr.equipmentId",           "E0044007131650618");
        assertAttributeEquals(result, "dsmr.message",               "");

        assertAttributeEquals(result, "dsmr.electricityTariffIndicator",              "1");
        assertAttributeEquals(result, "dsmr.electricityReceivedLowTariff",     "3432.829");
        assertAttributeEquals(result, "dsmr.electricityReceivedNormalTariff",  "3224.632");
        assertAttributeEquals(result, "dsmr.electricityReturnedLowTariff",          "0.0");
        assertAttributeEquals(result, "dsmr.electricityReturnedNormalTariff",       "0.0");
        assertAttributeEquals(result, "dsmr.electricityPowerReceived",            "0.433");
        assertAttributeEquals(result, "dsmr.electricityPowerReturned",              "0.0");
        assertAttributeEquals(result, "dsmr.powerFailures",                           "5");
        assertAttributeEquals(result, "dsmr.longPowerFailures",                       "3");

        assertAttributeEquals(result, "dsmr.powerFailureEventLog.size",               "2");

        assertAttributeEquals(result, "dsmr.powerFailureEventLog.0.startTime",        "2018-04-17T20:11:02+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.0.endTime",          "2018-04-17T20:14:58+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.0.durationSeconds",  "236");

        assertAttributeEquals(result, "dsmr.powerFailureEventLog.1.startTime",        "2021-03-21T16:18:08+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.1.endTime",          "2021-03-21T16:38:42+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.1.durationSeconds",  "1234");

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


        assertAttributeEquals(result, "dsmr.mBusEvents",                              "4");

        assertAttributeEquals(result, "dsmr.mbus.1.deviceType",                   "2");
        assertAttributeEquals(result, "dsmr.mbus.1.equipmentId",                  "____ONE____");
        assertAttributeEquals(result, "dsmr.mbus.1.timestamp",                    "2010-12-09T11:21:00+01:00");
        assertAttributeEquals(result, "dsmr.mbus.1.timestamp.epochSecond",        "1291890060");
        assertAttributeEquals(result, "dsmr.mbus.1.unit",                         "kWh");
        assertAttributeEquals(result, "dsmr.mbus.1.value",                        "12785.111");

        assertAttributeEquals(result, "dsmr.mbus.2.deviceType",                   "3");
        assertAttributeEquals(result, "dsmr.mbus.2.equipmentId",                  "____TWO____");
        assertAttributeEquals(result, "dsmr.mbus.2.timestamp",                    "2010-12-09T11:22:00+01:00");
        assertAttributeEquals(result, "dsmr.mbus.2.timestamp.epochSecond",        "1291890120");
        assertAttributeEquals(result, "dsmr.mbus.2.unit",                         "m3");
        assertAttributeEquals(result, "dsmr.mbus.2.value",                        "12785.222");

        assertAttributeEquals(result, "dsmr.mbus.3.deviceType",                   "4");
        assertAttributeEquals(result, "dsmr.mbus.3.equipmentId",                  "____THREE__");
        assertAttributeEquals(result, "dsmr.mbus.3.timestamp",                    "2010-12-09T11:23:00+01:00");
        assertAttributeEquals(result, "dsmr.mbus.3.timestamp.epochSecond",        "1291890180");
        assertAttributeEquals(result, "dsmr.mbus.3.unit",                         "GJ");
        assertAttributeEquals(result, "dsmr.mbus.3.value",                        "12785.333");

        assertAttributeEquals(result, "dsmr.mbus.4.deviceType",                   "10");
        assertAttributeEquals(result, "dsmr.mbus.4.equipmentId",                  "____FOUR___");
        assertAttributeEquals(result, "dsmr.mbus.4.timestamp",                    "2010-12-09T11:24:00+01:00");
        assertAttributeEquals(result, "dsmr.mbus.4.timestamp.epochSecond",        "1291890240");
        assertAttributeEquals(result, "dsmr.mbus.4.unit",                         "GJ");
        assertAttributeEquals(result, "dsmr.mbus.4.value",                        "12785.444");


        assertAttributeEquals(result, "dsmr.slaveEMeterEquipmentId",              "____ONE____");
        assertAttributeEquals(result, "dsmr.slaveEMeterkWh",                      "12785.111");
        assertAttributeEquals(result, "dsmr.slaveEMeterTimestamp",                "2010-12-09T11:21:00+01:00");
        assertAttributeEquals(result, "dsmr.slaveEMeterTimestamp.epochSecond",    "1291890060");

        assertAttributeEquals(result, "dsmr.gasEquipmentId",                      "____TWO____");
        assertAttributeEquals(result, "dsmr.gasM3",                               "12785.222");
        assertAttributeEquals(result, "dsmr.gasTimestamp",                        "2010-12-09T11:22:00+01:00");
        assertAttributeEquals(result, "dsmr.gasTimestamp.epochSecond",            "1291890120");

        // Test attributes and content
        result.assertContentEquals(content);
    }

    @Test
    void testInValidCRCRecord() {
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
                "1-0:99.97.0(2)(0-0:96.7.19)(180417201458S)(0000000236*s)(210321163842S)(0000001234*s)\r\n" +
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
                "!0000\r\n" + // The incorrect CRC
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
        List<MockFlowFile> validResults = runner.getFlowFilesForRelationship(VALID);
        assertEquals(0, validResults.size());

        List<MockFlowFile> invalidCRCresults = runner.getFlowFilesForRelationship(INVALID_CRC);
        assertEquals(1, invalidCRCresults.size());

        List<MockFlowFile> badresults = runner.getFlowFilesForRelationship(BAD_RECORDS);
        assertEquals(0, badresults.size());

        MockFlowFile result = invalidCRCresults.get(0);

        assertAttributeEquals(result, "dsmr.validCRC",       "false");
        assertAttributeEquals(result, "dsmr.crc",            "0000");

        assertAttributeEquals(result, "dsmr.rawIdent",          "/ISK5\\2M550T-1012");
        assertAttributeEquals(result, "dsmr.equipmentBrandTag", "ISK");
        assertAttributeEquals(result, "dsmr.ident",             "M550T-1012");
        assertAttributeEquals(result, "dsmr.p1Version",         "5.0");
        assertAttributeEquals(result, "dsmr.timestamp",         "2019-03-24T15:05:41+01:00");

        assertAttributeEquals(result, "dsmr.equipmentId",    "E0044007131650618");
        assertAttributeEquals(result, "dsmr.message",        "");

        assertAttributeEquals(result, "dsmr.electricityTariffIndicator",              "1");
        assertAttributeEquals(result, "dsmr.electricityReceivedLowTariff",     "3432.829");
        assertAttributeEquals(result, "dsmr.electricityReceivedNormalTariff",  "3224.632");
        assertAttributeEquals(result, "dsmr.electricityReturnedLowTariff",          "0.0");
        assertAttributeEquals(result, "dsmr.electricityReturnedNormalTariff",       "0.0");
        assertAttributeEquals(result, "dsmr.electricityPowerReceived",            "0.433");
        assertAttributeEquals(result, "dsmr.electricityPowerReturned",              "0.0");
        assertAttributeEquals(result, "dsmr.powerFailures",                           "5");
        assertAttributeEquals(result, "dsmr.longPowerFailures",                       "3");

        assertAttributeEquals(result, "dsmr.powerFailureEventLog.size",               "2");

        assertAttributeEquals(result, "dsmr.powerFailureEventLog.0.startTime",        "2018-04-17T20:11:02+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.0.endTime",          "2018-04-17T20:14:58+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.0.durationSeconds",  "236");

        assertAttributeEquals(result, "dsmr.powerFailureEventLog.1.startTime",        "2021-03-21T16:18:08+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.1.endTime",          "2021-03-21T16:38:42+02:00");
        assertAttributeEquals(result, "dsmr.powerFailureEventLog.1.durationSeconds",  "1234");

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

    @Test
    void testNoContentRecord() {
        // Test content
        String content = "                                  "; // Big, but no data

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
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(VALID);
        assertEquals(0, results.size());

        List<MockFlowFile> invalidCRCresults = runner.getFlowFilesForRelationship(INVALID_CRC);
        assertEquals(0, invalidCRCresults.size());

        List<MockFlowFile> badresults = runner.getFlowFilesForRelationship(BAD_RECORDS);
        assertEquals(1, badresults.size());
    }

    @Test
    void testTooSmallRecord() {
        // Test content
        String content = "  "; // Almost empty

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
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(VALID);
        assertEquals(0, results.size());

        List<MockFlowFile> invalidCRCresults = runner.getFlowFilesForRelationship(INVALID_CRC);
        assertEquals(0, invalidCRCresults.size());

        List<MockFlowFile> badresults = runner.getFlowFilesForRelationship(BAD_RECORDS);
        assertEquals(1, badresults.size());
    }


    @Test
    void testTooLargeRecord() {
        // Test content
        StringBuilder stringBuilder = new StringBuilder(10000000);
        for (int i = 0; i < 2000; i++) {
            stringBuilder
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ")
                .append("                                                  ");
        }
        String content = stringBuilder.toString();

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
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(VALID);
        assertEquals(0, results.size());

        List<MockFlowFile> badresults = runner.getFlowFilesForRelationship(BAD_RECORDS);
        assertEquals(1, badresults.size());
    }


    void assertAttributeEquals(MockFlowFile flowFile, String attributeName, String expectedValue) {
        assertEquals(expectedValue, flowFile.getAttribute(attributeName),
            "Attribute \"" + attributeName + "\" has the wrong value.");
    }
}
