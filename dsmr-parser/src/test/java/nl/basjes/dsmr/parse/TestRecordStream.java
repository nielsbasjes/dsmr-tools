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

import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.ParseDsmrTelegram;
import nl.basjes.parse.ReadUTF8RecordStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import static nl.basjes.dsmr.CheckCRC.crcIsValid;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRecordStream {

    private static final Logger LOG = LoggerFactory.getLogger(TestRecordStream.class);

    @Test
    public void runTest() throws IOException {
        // This file has a bad 'first' record and the rest are all good.
        FileInputStream inputStream = new FileInputStream("../testfiles/ttyUSB0-raw.txt");

        ReadUTF8RecordStream reader = new ReadUTF8RecordStream(inputStream, "\r\n![0-9A-F]{4}\r\n");

        String value;
        while ((value = reader.read())!= null) {
            if (value.startsWith("/")) {
                assertTrue(crcIsValid(value));
            }
        }
    }

    @Disabled
    @Test
    public void runTestWithSimulator() throws IOException {
        FileInputStream inputStream = new FileInputStream("../simulator/ttyDSMR");

        ReadUTF8RecordStream reader = new ReadUTF8RecordStream(inputStream, "\r\n![0-9A-F]{4}\r\n");

        String value;
        int count = 0;
        while ((value = reader.read())!= null) {
            if (value.length() < 8) {
                continue;
            }

            DSMRTelegram dsmrTelegram = ParseDsmrTelegram.parse(value);

            count++;
            boolean valid = crcIsValid(value);
            LOG.info("{}: {} {} --> {}",
                count,
                dsmrTelegram.getTimestamp(),
                value.substring(value.length()-7, value.length()-2),
                dsmrTelegram.isValidCRC() ? "Ok" : "BAD");
            if (value.startsWith("/")) {
                if (!valid) {
                    crcIsValid(value);
                }
            }
        }
        LOG.info("---------------------- Done ----------------------");
    }

}
