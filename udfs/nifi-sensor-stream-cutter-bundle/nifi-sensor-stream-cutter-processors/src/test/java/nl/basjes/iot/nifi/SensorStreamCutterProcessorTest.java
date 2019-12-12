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
package nl.basjes.iot.nifi;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.basjes.iot.nifi.SensorStreamCutterProcessor.END_OF_RECORD_REGEX;
import static nl.basjes.iot.nifi.SensorStreamCutterProcessor.FILE_NAME;
import static nl.basjes.iot.nifi.SensorStreamCutterProcessor.MAX_CHARACTERS_PER_RECORD;
import static nl.basjes.iot.nifi.SensorStreamCutterProcessor.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class SensorStreamCutterProcessorTest {

    private TestRunner runner;

    @BeforeEach
    public void init() {
        runner = TestRunners.newTestRunner(SensorStreamCutterProcessor.class);
    }

    @Test
    public void testProcessor() {

        runner.setProperty(END_OF_RECORD_REGEX,       "\\r?\\n");
        runner.setProperty(FILE_NAME,                 "src/test/data/testinput.txt");
        runner.setProperty(MAX_CHARACTERS_PER_RECORD, "20000");

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(5); // We have 4 lines in the file !!

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(SUCCESS);
        assertEquals(5, results.size(), "5 match");
        results.get(0).assertContentEquals("One\n");
        results.get(1).assertContentEquals("Two\n");
        results.get(2).assertContentEquals("Three\n");
        results.get(3).assertContentEquals("\n"); // We have an empty line !!
        results.get(4).assertContentEquals("");
    }

    @Test
    public void testTooLarge() {

        runner.setProperty(END_OF_RECORD_REGEX,       "\\r\\n![0-9A-F]{4}\\r\\n");
        runner.setProperty(FILE_NAME,                 "src/test/data/TooLargeRecord.txt");
        runner.setProperty(MAX_CHARACTERS_PER_RECORD, "10240"); // Slightly smaller than 20k

        // Run the enqueued content, it also takes an int = number of contents queued
        assertThrows(AssertionError.class, () -> runner.run(1));
    }
}
