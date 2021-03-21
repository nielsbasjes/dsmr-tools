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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestMissingSeparator {

    private static final Logger LOG = LoggerFactory.getLogger(TestMissingSeparator.class);

    volatile boolean runWriter = true;

    @Test
    void testMissingSeparatorAbort() throws IOException, InterruptedException {

        final PipedInputStream pipedInputStream = new PipedInputStream();
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();

        /*Connect pipe*/
        pipedInputStream.connect(pipedOutputStream);

        runWriter = true;

        /* Thread for writing data to pipe */
        Thread pipeWriter = new Thread(new Runnable() { // NOTE: This CANNOT be a Lambda !
            @Override
            public void run() {
                byte[] output = "Something that does not contain the end marker".getBytes(UTF_8);
                while (runWriter) {
                    try {
                        pipedOutputStream.write(output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ReadUTF8RecordStream reader = new ReadUTF8RecordStream(pipedInputStream, "This will NOT occur");

        pipeWriter.start();

        IOException exception = assertThrows(IOException.class, reader::read);

        assertTrue(exception.getMessage().matches("After [0-9]+ bytes the end-of-record pattern has not been found yet."));

        runWriter = false;
        pipeWriter.join();

        /*Close stream*/
        pipedOutputStream.close();
        pipedInputStream.close();
    }

}
