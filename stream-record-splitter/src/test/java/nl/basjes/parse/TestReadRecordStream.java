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

package nl.basjes.parse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestReadRecordStream {

    @Test
    void testSingleLineRecords() throws IOException, InterruptedException {
        String[] recordFragments = {
            "on",
            "e\nt",
            "wo\nthre",
            "e\n",
            "four\nfiv",
            "e\nsix\n"
        };

        String[] records = {
            "one\n",
            "two\n",
            "three\n",
            "four\n",
            "five\n",
            "six\n"
        };
        testRecordReassemblyInBurstyStream(recordFragments, records, "\n");
    }

    @Test
    void testSingleLineRecordsMissingEndMarker() throws IOException, InterruptedException {
        String[] recordFragments = {
            "on",
            "e\nt",
            "wo\nthre",
            "e\n",
            "four\nfiv",
            "e\nsix"  // The \n is missing here
        };

        String[] records = {
            "one\n",
            "two\n",
            "three\n",
            "four\n",
            "five\n",
            "six"   // No \n because it is also not in the input data.
        };
        testRecordReassemblyInBurstyStream(recordFragments, records, "\n");
    }

    @Test
    void testMultiLineRecords() throws IOException, InterruptedException {

        String[] recordFragments = {
            "one\n",
            "two\n====\nthree\n",
            "four\n====\n",
            "five\nsix\nseven\n",
            "====\neight\nnine\n====\nten\n",
            "eleven\n====\n"
        };

        String[] records = {
            "one\ntwo\n====\n",
            "three\nfour\n====\n",
            "five\nsix\nseven\n====\n",
            "eight\nnine\n====\n",
            "ten\neleven\n====\n"
        };
        // Use  ====\n  as the separator at the end of the record.
        testRecordReassemblyInBurstyStream(recordFragments, records, "====\n");
    }

    @Test
    void testTooLargeRecords() {

        StringBuilder input = new StringBuilder(10240);

        for (int i = 0; i < 10000; i++) {
            input.append("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        }

        String[] recordFragments = {
            input.toString()
        };

        String[] records = {
            "Ignored, should fail, no record expected."
        };

        IOException exception = assertThrows(IOException.class, () -> testRecordReassemblyInBurstyStream(recordFragments, records, "====\n"));

        assertTrue(exception.getMessage().matches("After [0-9]+ bytes the end-of-record pattern has not been found yet."));
    }

    volatile boolean keepRunning = true;

    void testRecordReassemblyInBurstyStream(String[] recordFragments, String[] records, String endPattern)
        throws IOException, InterruptedException {

        final PipedInputStream  pipedInputStream  = new PipedInputStream();
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        Thread                  pipeWriter        = null;
        keepRunning = true;
        try {

            /*Connect pipe*/
            pipedInputStream.connect(pipedOutputStream);


            /* Thread for writing data to pipe */
            pipeWriter = new Thread(new Runnable() { // NOTE: This CANNOT be a Lambda !
                @Override
                public void run() {
                    for (String fragment : recordFragments) {
                        try {
                            pipedOutputStream.write(fragment.getBytes(UTF_8));
                            Thread.sleep(100); // We periodically wait
                        } catch (IOException | InterruptedException e) {
                            // Ignore
                        }
                        if (!keepRunning) {
                            break;
                        }
                    }
                    try {
                        pipedOutputStream.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            });

            ReadUTF8RecordStream reader = new ReadUTF8RecordStream(pipedInputStream, endPattern, 10000);

            pipeWriter.start();

            /*Thread for reading data from pipe*/
            for (String expectedRecord : records) {
                String record = reader.read();
                if (record == null) {
                    break;
                }
//                LOG.info("Record received: \n{}", record);
                assertEquals(expectedRecord, record, "Got the wrong record back");
            }

            reader.read(); // Can be null or an empty string
            assertNull(reader.read()); // Always null in our tests.
        } finally {
            keepRunning = false;
            Thread.sleep(200);
            if (pipeWriter != null) {
                pipedOutputStream.close();
                pipeWriter.interrupt();
                pipeWriter.join();
            }

            /*Close stream*/
            pipedOutputStream.close();
            pipedInputStream.close();
        }
    }

}
