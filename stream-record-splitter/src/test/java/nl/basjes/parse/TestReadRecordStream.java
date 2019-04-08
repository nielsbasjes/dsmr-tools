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

package nl.basjes.parse;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class TestReadRecordStream {

    private static final Logger LOG = LoggerFactory.getLogger(TestReadRecordStream.class);

@Test
    public void testSingleLineRecords() throws IOException, InterruptedException {
        String[] recordFragments = {
            "on", "e\n" +
            "t", "wo\n" +
            "three", "\n" ,
            "four\n" +
            "five\n" +
            "six\n"
        };

        String[] records = {
            "one\n" ,
            "two\n" ,
            "three\n" ,
            "four\n" ,
            "five\n" ,
            "six\n"
        };
        testRecordReassemblyInBurstyStream(recordFragments, records, "\n");
    }

    @Test
    public void testMultiLineRecords() throws IOException, InterruptedException {

        String[] recordFragments = {
            "one\n" ,

            "two\n" +
            "====\n" +
            "three\n" ,

            "four\n" +
            "====\n" ,

            "five\n" +
            "six\n" +
            "seven\n" ,

            "====\n" +
            "eight\n" +
            "nine\n" +
            "====\n" +
            "ten\n" ,

            "eleven\n" +
            "====\n"
        };

        String[] records = {
            "one\n" +
            "two\n" +
            "====\n" ,

            "three\n" +
            "four\n" +
            "====\n" ,

            "five\n" +
            "six\n" +
            "seven\n" +
            "====\n" ,

            "eight\n" +
            "nine\n" +
            "====\n" ,

            "ten\n" +
            "eleven\n" +
            "====\n"
        };
        testRecordReassemblyInBurstyStream(recordFragments, records, "====\n");
    }


    public void testRecordReassemblyInBurstyStream(String[] recordFragments, String[] records, String endPattern) throws IOException, InterruptedException {

        final PipedInputStream pipedInputStream = new PipedInputStream();
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();

        /*Connect pipe*/
        pipedInputStream.connect(pipedOutputStream);


        /* Thread for writing data to pipe */
        Thread pipeWriter = new Thread(() -> {
            for(String fragment: recordFragments) {
                try {
                    pipedOutputStream.write(fragment.getBytes(UTF_8));
                    Thread.sleep(500); // We periodically wait
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Use  ====\n  as the separator at the end of the record.
        ReadUTF8RecordStream reader = new ReadUTF8RecordStream(endPattern, pipedInputStream);

        pipeWriter.start();

        /*Thread for reading data from pipe*/
        for(String expectedRecord: records) {
            try {
                String record = reader.read();
                while (record == null) {
                    record = reader.read();
                }
                LOG.info("Record received: \n{}", record);
                assertEquals("Got the wrong record back", expectedRecord, record);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pipeWriter.join();

        /*Close stream*/
        pipedOutputStream.close();
        pipedInputStream.close();
    }

}
